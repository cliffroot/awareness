package hive.com.paradiseoctopus.awareness.createplace

import android.net.wifi.ScanResult
import com.google.android.gms.maps.model.LatLng
import hive.com.paradiseoctopus.awareness.createplace.helper.FragmentTranstion
import rx.Observable
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 14.09.16.
 */

interface CreatePlaceContracts {

    interface PlaceView {
        fun showPlaceChooser(transition: FragmentTranstion, location: LatLng, name : String)
        fun showDeviceChooser(transition: FragmentTranstion, savedNetwork : List<ScanResult>, selectedSsid : String?)
        fun showAdditionalSettings(transition: FragmentTranstion, intervalFrom : Pair<Int,Int>, intervalTo : Pair<Int, Int>,
                                   placeCode : String, placeName : String)

        fun progress(running : Boolean)
        fun dismiss(resultObservable : PublishSubject<Boolean>)
        fun finish()
    }

    interface PlacePresenter {
        fun provideView (placeView : PlaceView)

        fun getCurrentLocation() : Observable<LatLng>
        fun getNearbyDevices() : Observable<List<ScanResult>>

        fun placeDetailsRetrieved (updated : Map<String, Any>)


        fun hasPlaceImage(latitude : Double, longitude : Double) : Boolean
        fun coordinatesToName(latLng: LatLng, default : String) : Observable<String>

        fun restoreState()
        fun startCreation()
        fun dismiss()
        fun next()
        fun back()

    }
}

val latitudeField = "latitude"
val longitudeField = "longitude"
val nameField = "name"
val intervalFromField = "intervalFrom"
val intervalToField = "intervalTo"
val placeIdField = "placeId"
val deviceField = "device"
val mapSnapshotField = "mapSnapshot"