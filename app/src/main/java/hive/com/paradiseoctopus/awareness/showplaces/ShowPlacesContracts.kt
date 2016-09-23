package hive.com.paradiseoctopus.awareness.showplaces

import com.google.firebase.database.DatabaseReference

/**
 * Created by edanylenko on 9/23/16.
 */


interface ShowPlacesContracts {

    interface ShowPlaceView {
        fun displayPlaces (ref : DatabaseReference)
    }
}