package hive.com.paradiseoctopus.awareness.createplace

import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import hive.com.paradiseoctopus.awareness.createplace.helper.BitmapRepository
import hive.com.paradiseoctopus.awareness.createplace.helper.UiStateHandler
import hive.com.paradiseoctopus.awareness.createplace.helper.WifiScanReceiver
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import hive.com.paradiseoctopus.awareness.utils.UiUtils
import rx.Observable
import rx.subjects.ReplaySubject
import java.util.*

/**
 * Created by cliffroot on 14.09.16.
 *
 */

class CreatePlacePresenter(var view : CreatePlaceContracts.PlaceView?) : Fragment(), CreatePlaceContracts.PlacePresenter  {
    val TAG = "CreatePlacePresenter"
    var place: PlaceModel = PlaceModel()

    var discoverableNetworks : List<ScanResult>? = null
    var wifiScanReceiver : WifiScanReceiver? = null

    val client: GoogleApiClient? by lazy {
        GoogleApiClient.Builder(context).addApi(Awareness.API).addApi(Places.GEO_DATA_API).build()
    }

    val stateHandler : UiStateHandler by lazy {
        UiStateHandler(this)
    }

    constructor() : this(null)

    override fun provideView(placeView: CreatePlaceContracts.PlaceView) {
        view = placeView
    }

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        client?.connect()
        if (savedState != null) {
            place = savedState.getParcelable<PlaceModel>(PERSISTED_PLACE_MODEL_NAME)
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
        place.name = name.split("@")[0]

        if (name.split("@").size > 1) {
            Places.GeoDataApi.getPlacePhotos(client, name.split("@")[1]).setResultCallback {
                it.photoMetadata.take(1).map {
                    metadata -> metadata.getScaledPhoto(client, 240, 240).setResultCallback {
                        result -> place.pathToMap = BitmapRepository.saveBitmap(activity, result.bitmap, "${place.latitude};${place.longitude}")
                    }
                }
            }
        }
    }

    override fun deviceRetrieved(ssid: String) {
         place.device = ssid
    }

    override fun intervalsRetrieved(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        place.intervalFrom = from.first * DateUtils.HOUR_IN_MILLIS + from.second * DateUtils.MINUTE_IN_MILLIS
        place.intervalTo = to.first * DateUtils.HOUR_IN_MILLIS + to.second * DateUtils.MINUTE_IN_MILLIS
    }

    override fun mapSnapshotRetrieved(bitmap: Bitmap) {
        place.pathToMap = BitmapRepository.saveBitmap(context, BitmapRepository.cutBitmapCenter(
                bitmap, UiUtils.dpToPx(context, 121), UiUtils.dpToPx(context, 121)), "${place.latitude};${place.longitude}")
    }

    override fun hasPlaceImage(latitude : Double, longitude : Double): Boolean {
        Log.e("Overlay", "hasPlaceImage $latitude;$longitude => ${BitmapRepository.imageExists(activity, "$latitude;$longitude")}")
        return BitmapRepository.imageExists(activity, "$latitude;$longitude")
    }

    override fun dismiss() {
        stateHandler.dismiss()
    }

    override fun startCreation() {
        stateHandler.next { state -> true }
    }

    fun generatePlaceCode(): String {
        val code : String = UUID.randomUUID().toString().substring(IntRange(0, 8))
        place.code = code
        return code
    }

    override fun next() {
        stateHandler.next { state -> true }

        if (stateHandler.currentState == UiStateHandler.State.FINISH) {
            saveModel()
            Log.e("Overlay", "Model after creation: " + place)
            stateHandler.finish()
        }
    }

    fun saveModel() {
        place.timestamp = Calendar.getInstance().timeInMillis
        place.id = UUID.randomUUID().toString()

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("places").child(place.id)

        myRef.setValue(place)

    }

    override fun back() {
        stateHandler.back()
    }

    override fun restoreState() {
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
