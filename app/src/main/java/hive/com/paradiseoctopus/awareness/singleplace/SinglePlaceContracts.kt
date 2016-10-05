package hive.com.paradiseoctopus.awareness.singleplace

import android.content.Intent
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import rx.Observable

/**
 * Created by edanylenko on 9/29/16.
 */

val PLACE_UID_KEY : String = "PlaceUidKey"
val OWNER_UID_KEY : String = "OwnerUidKey"

interface SinglePlaceContracts {

    interface SinglePlaceView {
        fun showPlace(place : PlaceModel)
        fun dismiss()
    }

    interface SinglePlacePresenter {
        fun canSubscribe() : Observable<Boolean>
        fun subscribe()
        fun start(intent : Intent)
        fun back()
        fun provideView (view : SinglePlaceView)
    }

}
