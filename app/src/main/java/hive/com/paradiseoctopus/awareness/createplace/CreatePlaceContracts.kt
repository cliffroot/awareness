package hive.com.paradiseoctopus.awareness.createplace

import android.location.Location
import android.net.wifi.ScanResult
import rx.Observable

/**
 * Created by cliffroot on 14.09.16.
 */

interface CreatePlaceContracts {

    interface PlaceView {
        fun showPlaceChooser(backwards: FragmentTranstion)
        fun showDeviceChooser(backwards: FragmentTranstion)
        fun showAdditionalSettings(backwards: FragmentTranstion)
        fun finishCreation(backwards : Boolean)
    }

    interface PlacePresenter {
        fun getCurrentLocation() : Observable<Location>
        fun getNearbyDevices() : Observable<List<ScanResult>>
        fun setCurrentPlace(placeUpdate : (PlaceModel) -> Unit)
        fun startCreation()
        fun dismiss()
        fun next()
        fun back()
    }

}