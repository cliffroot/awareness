package hive.com.paradiseoctopus.awareness.singleplace

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel

/**
 * Created by edanylenko on 9/29/16.
 */


class ShowSinglePlaceView : AppCompatActivity(), SinglePlaceContracts.SinglePlaceView {
    val PRESENTER_TAG = "SinglePlacePresenter"
    var presenter : SinglePlaceContracts.SinglePlacePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.single_place_activity)
    }

    override fun onStart() {
        super.onStart()

        if (supportFragmentManager.findFragmentByTag(PRESENTER_TAG) == null) {
            presenter = ShowSinglePlacePresenter(this)
            supportFragmentManager.beginTransaction()
                    .add(presenter as Fragment, PRESENTER_TAG)
                    .commitNow()
            presenter?.start(intent)
        } else {
            presenter = supportFragmentManager.findFragmentByTag(PRESENTER_TAG) as SinglePlaceContracts.SinglePlacePresenter
            presenter?.provideView(this)
            presenter?.start(intent)
        }
    }

    override fun showPlace(place : PlaceModel) {
        (findViewById(R.id.place_name) as TextView).text = place.name + " <~ " + place.timestamp

        val subscribeButton : Button = findViewById(R.id.subscribe_button) as Button
        presenter?.canSubscribe()?.subscribe {
            Log.e("Overlay", "subscription status pushed")
            if (it) {
                subscribeButton.visibility = View.VISIBLE
                subscribeButton.setOnClickListener {
                    presenter?.subscribe()
                }
            } else {
                subscribeButton.visibility = View.INVISIBLE
            }
        }
    }

    override fun dismiss() {

    }
}