package hive.com.paradiseoctopus.awareness.createplace

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.location.places.ui.PlacePicker
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.fragment.DeviceChooserFragment
import hive.com.paradiseoctopus.awareness.createplace.fragment.PLACE_PICKER_REQUEST
import hive.com.paradiseoctopus.awareness.createplace.fragment.PlaceChooserFragment

/**
 * Created by cliffroot on 14.09.16.
 */

class CreatePlaceView : AppCompatActivity(), CreatePlaceContracts.PlaceView {

    val PRESENTER_TAG = "CreatePlacePresenter"
    val presenter : CreatePlacePresenter by lazy {
        CreatePlacePresenter(this)
    }
    var currentScreen : CurrentScreen = CurrentScreen.PLACE

    enum class CurrentScreen {
        PLACE, DEVICE, ADDITIONAL
    }


    override fun showPlaceChooser() {
        replaceFragmentWithAnimation(PlaceChooserFragment(), PlaceChooserFragment::class.java.name)
    }

    override fun showDeviceChooser() {
        replaceFragmentWithAnimation(DeviceChooserFragment(), DeviceChooserFragment::class.java.name)
    }

    override fun showAdditionalSettings() {

    }

    override fun finishCreation() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_place_view)

        supportFragmentManager.beginTransaction()
            .add(presenter, PRESENTER_TAG)
            .commitNow()
        presenter.startCreation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next -> {
                showDeviceChooser()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun replaceFragmentWithAnimation(fragment: android.support.v4.app.Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
                R.anim.from_right, R.anim.to_left,
                R.anim.from_left, R.anim.to_right)
        transaction.replace(R.id.create_place_fragment, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode and 0xFF == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                (supportFragmentManager.findFragmentById(R.id.create_place_fragment)
                    as PlaceChooserFragment).locationSubject.onNext(PlacePicker.getPlace(this, data))

            }
        }
    }
}