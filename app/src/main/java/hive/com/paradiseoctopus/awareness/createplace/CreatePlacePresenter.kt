package hive.com.paradiseoctopus.awareness.createplace

import android.content.Context
import android.content.IntentFilter
import android.location.Location
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.maps.model.LatLng
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import hive.com.paradiseoctopus.awareness.utils.WifiScanReceiver
import rx.Observable
import rx.subjects.ReplaySubject
import java.util.*

/**
 * Created by cliffroot on 14.09.16.
 *
 */

class CreatePlacePresenter(var view : CreatePlaceView?) : Fragment(), CreatePlaceContracts.PlacePresenter  {
    val TAG = "CreatePlacePresenter"
    var place: PlaceModel = PlaceModel()

    var discoverableNetworks : List<ScanResult>? = null
    var wifiScanReceiver : WifiScanReceiver? = null

    val client: GoogleApiClient? by lazy {
        GoogleApiClient.Builder(context).addApi(Awareness.API).build()
    }

    val stateHandler : UiStateHandler by lazy {
        UiStateHandler(this)
    }

    constructor() : this(null)

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        client?.connect()
        if (savedState != null) {
            place = savedState?.getParcelable<PlaceModel>(PERSISTED_PLACE_MODEL_NAME)
        }
        retainInstance = true
    }

    override fun getCurrentLocation() : Observable<LatLng> {
        if (place.latitude == null && place.longitude == null) {
            val locationSubject: ReplaySubject<LatLng> = ReplaySubject.create()
            return PermissionUtility.requestPermission(activity as AppCompatActivity,
                    mutableListOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PermissionUtility.REQUEST_LOCATION_CODE)
                    .filter { pair -> pair.first == PermissionUtility.REQUEST_LOCATION_CODE }
                    .flatMap {
                        pair ->
                        if (pair.second) { // permission was granted
                            if (client?.isConnected == true) {
                                queryAwarenessApi(locationSubject)
                            } else {
                                client?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                                    override fun onConnectionSuspended(p0: Int) { }

                                    override fun onConnected(p0: Bundle?) = queryAwarenessApi(locationSubject)
                                })
                            }
                        } else {
                            locationSubject.onNext(null)
                            locationSubject.onCompleted()
                        }
                        locationSubject
                    }
        } else {
            return ReplaySubject.just(LatLng(place.latitude!!, place.longitude!!))
        }
    }

    fun queryAwarenessApi(p : ReplaySubject<LatLng>) {
        Awareness.SnapshotApi.getLocation(client).setResultCallback(
                ResultCallback<com.google.android.gms.awareness.snapshot.LocationResult>
                { locationResult ->
                    Log.i(TAG, "loc: " + locationResult.status)
                    if (!locationResult.status.isSuccess) {
                        Log.e(TAG, "Could not get location." + locationResult.status)
                        return@ResultCallback
                    }
                    place.latitude = locationResult.location.latitude
                    place.longitude = locationResult.location.longitude
                    p.onNext(LatLng(place.latitude!!, place.longitude!!))
                    p.onCompleted()
                })
    }

    override fun getNearbyDevices() : Observable<List<ScanResult>> {
        if (discoverableNetworks == null) {
            val replaySubject: ReplaySubject<List<ScanResult>> = ReplaySubject.create()
            return PermissionUtility.requestPermission(activity as AppCompatActivity,
                    mutableListOf(android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE),
                    PermissionUtility.REQUEST_WIFI_CODE)
                    .filter { p -> p.first == PermissionUtility.REQUEST_WIFI_CODE }
                    .flatMap {
                        p ->
                        if (p.second) {
                            val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            wifiScanReceiver = WifiScanReceiver(context, replaySubject)
                            context.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                            wifiManager.startScan()
                            replaySubject.subscribe {
                                result ->
                                discoverableNetworks = result
                            }
                            replaySubject
                        } else {
                            replaySubject.onNext(mutableListOf())
                            replaySubject.onCompleted()
                            replaySubject
                        }
                    }
        } else {
            return ReplaySubject.just(discoverableNetworks)
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (wifiScanReceiver != null)
            context.unregisterReceiver(wifiScanReceiver)
        wifiScanReceiver = null
    }

    override fun locationRetrieved(latLng: LatLng) {
        place.latitude = latLng.latitude
        place.longitude = latLng.longitude
    }

    override fun nameRetrieved(name: String) {
        place.name = name
    }

    override fun deviceRetrieved(ssid: String) {
         place.device = ssid
    }

    override fun intervalsRetrieved(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        place.intervalFrom = from.first * DateUtils.HOUR_IN_MILLIS + from.second * DateUtils.MINUTE_IN_MILLIS
        place.intervalTo = to.first * DateUtils.HOUR_IN_MILLIS + to.second * DateUtils.MINUTE_IN_MILLIS
    }

    override fun dismiss() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startCreation() {
        stateHandler.next { state -> true }
    }

    override fun generatePlaceCode(): String {
        val code : String = UUID.randomUUID().toString().substring(IntRange(0, 8))
        place.code = code
        return code
    }

    override fun next() {
        view?.progress(true)
        if (stateHandler.currentState != UiStateHandler.State.PLACE_PICKER) {
            stateHandler.next { state -> true }
        } else {
            getNearbyDevices().isEmpty.subscribe{
                empty ->
                if (empty) stateHandler.next { state -> state == UiStateHandler.State.OTHER_OPTIONS}
                else stateHandler.next { state -> state == UiStateHandler.State.DEVICE_PICKER }
            }
        }

        if (stateHandler.currentState == UiStateHandler.State.FINISH) {
            saveModel()
            Log.e("Overlay", "Model after creation: " + place)
            stateHandler.finish()
        }
    }

    fun saveModel() {
        place.timestamp = SystemClock.currentThreadTimeMillis()
        place.id = UUID.randomUUID().toString()
    }

    override fun back() {
        view?.progress(true)
        stateHandler.back()
    }

    fun restoreState() {
        view?.progress(true)
        stateHandler.restore()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(PERSISTED_PLACE_MODEL_NAME, place)

    }

}

val PERSISTED_PLACE_MODEL_NAME = "placeModelName"

fun timeMillisToHoursMinutesPair (millis : Long) :Pair<Int,Int> {
    return Pair ( ((millis / DateUtils.MINUTE_IN_MILLIS) / 60).toInt(),
                  ((millis / DateUtils.MINUTE_IN_MILLIS) % 60).toInt())
}
