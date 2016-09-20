package hive.com.paradiseoctopus.awareness.createplace

import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import rx.Observable
import rx.subjects.ReplaySubject

/**
 * Created by cliffroot on 14.09.16.
 */
class CreatePlacePresenter(val view : CreatePlaceView?) : Fragment(),
                             CreatePlaceContracts.PlacePresenter  {
    val TAG = "CreatePlacePresenter"
    val place: PlaceModel = PlaceModel()

    val client: GoogleApiClient? by lazy {
        GoogleApiClient.Builder(context).addApi(Awareness.API).build()
    }

    constructor() : this(null)

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        client?.connect()
        retainInstance = true

        Log.i(TAG, "showPlaceChooser()")
    }

    override fun getCurrentLocation() : Observable<Location> {
        val o : ReplaySubject<Location> = ReplaySubject.create()
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
                            override fun onConnectionSuspended(p0: Int) { }
                            override fun onConnected(p0: Bundle?) = queryAwarenessApi(o)
                        })
                    }
                } else {
                    o.onNext(Location("NotGranted"))
                    o.onCompleted()
                }
                o
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
                    p.onNext(locationResult.location)
                    p.onCompleted()
                })
    }

    override fun getNearbyDevices() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCurrentPlace() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dismiss() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startCreation() {
        view?.showPlaceChooser()
    }

}