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

class CreatePlacePresenter(var view : CreatePlaceView?) : Fragment(),
                             CreatePlaceContracts.PlacePresenter  {
    val TAG = "CreatePlacePresenter"
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

        Log.e("Overlay", "onCreate happened")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.e("Overlay", "attached")
    }

    override fun getCurrentLocation() : Observable<Location> {
        Log.e("Overlay", "selectedLoc: " + selectedLocation)
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
        val publishSubject : PublishSubject<List<ScanResult>> = PublishSubject.create()
        val wifiManager : WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        context.registerReceiver(WifiScanReceiver(context, publishSubject), IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        return publishSubject
    }

    override fun setCurrentPlace(placeUpdate: (PlaceModel) -> Unit) {
        placeUpdate(place)
        selectedLocation = Location("Snapshot")
        selectedLocation?.latitude = place.latitude as Double
        selectedLocation?.longitude = place.longitude as Double

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