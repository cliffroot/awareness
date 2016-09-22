package hive.com.paradiseoctopus.awareness.createplace

import android.location.Location
import android.net.wifi.ScanResult
import com.google.android.gms.maps.model.LatLng
import rx.Observable

/**
 * Created by cliffroot on 14.09.16.
 */

interface CreatePlaceContracts {

    interface PlaceView {
        fun showPlaceChooser(transition: FragmentTranstion, location: LatLng, name : String)
        fun showDeviceChooser(transition: FragmentTranstion, savedNetwork : List<ScanResult>, selectedSsid : String?)
        fun showAdditionalSettings(transition: FragmentTranstion, intervalFrom : Pair<Int,Int>, intervalTo : Pair<Int, Int>,
                                   placeCode : String, placeName : String)
        fun finishCreation(backwards : Boolean)
        fun progress(running : Boolean)
    }

    interface PlacePresenter {
        fun getCurrentLocation() : Observable<LatLng>
        fun getNearbyDevices() : Observable<List<ScanResult>>

        fun locationRetrieved(latLng: LatLng)
        fun nameRetrieved(name: String)
        fun deviceRetrieved(ssid: String)
        fun intervalsRetrieved(from : Pair<Int, Int>, to : Pair<Int, Int>)

        fun generatePlaceCode() : String
        fun startCreation()
        fun dismiss()
        fun next()
        fun back()
    }

}