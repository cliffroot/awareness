package hive.com.paradiseoctopus.awareness.createplace

import android.location.Location
import rx.Observable

/**
 * Created by cliffroot on 14.09.16.
 */

interface CreatePlaceContracts {

    interface PlaceView {
        fun showPlaceChooser()
        fun showDeviceChooser()
        fun showAdditionalSettings()
        fun finishCreation()
    }

    interface PlacePresenter {
        fun getCurrentLocation() : Observable<Location>
        fun getNearbyDevices()
        fun setCurrentPlace()
        fun startCreation()
        fun dismiss()
    }

}