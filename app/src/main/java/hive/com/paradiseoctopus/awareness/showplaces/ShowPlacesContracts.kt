package hive.com.paradiseoctopus.awareness.showplaces

import android.widget.ImageView
import com.google.firebase.database.DatabaseReference

/**
 * Created by edanylenko on 9/23/16.
 */


interface ShowPlacesContracts {

    interface ShowPlaceView {
        fun displayPlaces (ref : DatabaseReference)
    }

    interface ShowPlacePresenter {
        fun loadUserImage(imageView : ImageView)
        fun start()
        fun provideView (view : ShowPlaceView)
    }
}