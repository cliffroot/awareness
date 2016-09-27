package hive.com.paradiseoctopus.awareness.createplace

import android.graphics.Bitmap
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

        fun locationRetrieved(latLng: LatLng)
        fun nameRetrieved(name: String)
        fun deviceRetrieved(ssid: String)
        fun intervalsRetrieved(from : Pair<Int, Int>, to : Pair<Int, Int>)

        fun mapSnapshotRetrieved(bitmap : Bitmap)
        fun hasPlaceImage(latitude : Double, longitude : Double) : Boolean

        fun restoreState()
        fun startCreation()
        fun dismiss()
        fun next()
        fun back()

    }

}