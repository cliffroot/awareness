package hive.com.paradiseoctopus.awareness.showplaces

import android.support.v4.app.Fragment
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import hive.com.paradiseoctopus.awareness.App

/**
 * Created by edanylenko on 10/5/16.
 */


class ShowPlacesPresenter(var view : ShowPlacesView? = null) : ShowPlacesContracts.ShowPlacePresenter, Fragment() {

    override fun provideView(view: ShowPlacesContracts.ShowPlaceView) {
        
    }

    override fun loadUserImage(imageView: ImageView) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        val database = (context.applicationContext as App).firebaseDatabase
        val ref = database.getReference("places").child(FirebaseAuth.getInstance().currentUser?.uid)
        view?.displayPlaces(ref)
    }

}
