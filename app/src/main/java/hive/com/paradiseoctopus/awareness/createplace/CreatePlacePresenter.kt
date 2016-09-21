package hive.com.paradiseoctopus.awareness.createplace

import android.content.Context
import android.content.IntentFilter
import android.location.Location
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import hive.com.paradiseoctopus.awareness.utils.WifiScanReceiver
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject

/**
 * Created by cliffroot on 14.09.16.
 *
 */

val PLACE_NAME_EXTRA = "placeName"

class CreatePlacePresenter(var view : CreatePlaceView?) : Fragment(),
                             CreatePlaceContracts.PlacePresenter  {
    val TAG = "CreatePlacePresefdnter"
    var place: PlaceModel = PlaceModel()

    var selectedLocation : Location? = null
    var discoverableNetworks : List<ScanResult>? = null

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
        retainInstance = true
    }

    override fun getCurrentLocation() : Observable<Location> {
        if (selectedLocation == null) {
            val o: ReplaySubject<Location> = ReplaySubject.create()
            return PermissionUtility.requestPermission(activity as AppCompatActivity,
                    mutableListOf(android.Manifest.permission.ACCESS_FINE_LOCATION), PermissionUtility.REQUEST_LOCATION_CODE)
                    .filter { p -> p.first == PermissionUtility.REQUEST_LOCATION_CODE }
                    .flatMap {
                        p ->
                        if (p.second) { // permission was granted
                            if (client?.isConnected == true) {
                                queryAwarenessApi(o)
                            } else {
                                client?.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                                    override fun onConnectionSuspended(p0: Int) {
                                    }

                                    override fun onConnected(p0: Bundle?) = queryAwarenessApi(o)
                                })
                            }
                        } else {
                            o.onNext(Location("NotGranted"))
                            o.onCompleted()
                        }
                        o
                    }
        } else {
            return ReplaySubject.just(selectedLocation)
        }
    }

    fun queryAwarenessApi(p : ReplaySubject<Location>) {
        Awareness.SnapshotApi.getLocation(client).setResultCallback(
                ResultCallback<com.google.android.gms.awareness.snapshot.LocationResult>
                { locationResult ->
                    Log.i(TAG, "loc: " + locationResult.status)
                    if (!locationResult.status.isSuccess) {
                        Log.e(TAG, "Could not get location." + locationResult.status)
                        return@ResultCallback
                    }
                    selectedLocation = locationResult.location
                    Log.e("Overlay", "selectedLocation: " + selectedLocation)
                    p.onNext(locationResult.location)
                    p.onCompleted()
                })
    }

    override fun next() {
        if (stateHandler.currentState != UiStateHandler.State.PLACE_PICKER) {
            stateHandler.next { state -> true }
        } else {
            getNearbyDevices().isEmpty.subscribe{
                empty ->
                    if (empty) stateHandler.next { state -> state == UiStateHandler.State.OTHER_OPTIONS}
                    else stateHandler.next { state -> state == UiStateHandler.State.DEVICE_PICKER }
            }
        }
    }

    override fun back() {
        stateHandler.back()
    }

    override fun getNearbyDevices() : Observable<List<ScanResult>> {
        Log.e("Overlay", "disc.net = $discoverableNetworks")
        if (discoverableNetworks == null) {
            val replaySubject: ReplaySubject<List<ScanResult>> = ReplaySubject.create()
            return PermissionUtility.requestPermission(activity as AppCompatActivity,
                    mutableListOf(android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.CHANGE_WIFI_STATE),
                    PermissionUtility.REQUEST_WIFI_CODE)
                    .filter { p -> p.first == PermissionUtility.REQUEST_WIFI_CODE }
                    .flatMap {
                        p ->
                        if (p.second) { // [permission was granted
                            val wifiManager: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            context.registerReceiver(WifiScanReceiver(context, replaySubject), IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
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

    override fun setCurrentPlace(placeUpdate: (PlaceModel) -> Unit) {
        placeUpdate(place)
        selectedLocation = Location("Snapshot")
        selectedLocation?.latitude = place.latitude as Double
        selectedLocation?.longitude = place.longitude as Double
        if (selectedLocation?.extras == null) selectedLocation?.extras = {
            val b: Bundle = Bundle()
            b.putString(PLACE_NAME_EXTRA, place.name)
            b
        }() else selectedLocation?.extras?.putString(PLACE_NAME_EXTRA, place.name)

    }

    override fun dismiss() {
        throw UnsupportedOperationException("not implemented")
    }

    override fun startCreation() {
        stateHandler.next { state -> true }
    }

    fun restoreState() {
        stateHandler.restore()
    }


}