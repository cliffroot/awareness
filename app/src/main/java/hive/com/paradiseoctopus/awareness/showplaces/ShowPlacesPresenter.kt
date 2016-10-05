package hive.com.paradiseoctopus.awareness.showplaces

import android.support.v4.app.Fragment
import android.widget.ImageView

/**
 * Created by edanylenko on 10/5/16.
 */


class ShowPlacesPresenter(var view : ShowPlacesView? = null) : ShowPlacesContracts.ShowPlacePresenter, Fragment() {

    override fun provideView(view: ShowPlacesContracts.ShowPlaceView) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadUserImage(imageView: ImageView) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
