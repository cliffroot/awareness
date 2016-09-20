package hive.com.paradiseoctopus.awareness.createplace

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.places.Place
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
    var presenter : CreatePlacePresenter? = null

    override fun showPlaceChooser(transition: FragmentTranstion) = replaceFragmentWithAnimation(PlaceChooserFragment(), PlaceChooserFragment::class.java.name, transition)

    override fun showDeviceChooser(transition: FragmentTranstion) = replaceFragmentWithAnimation(DeviceChooserFragment(), DeviceChooserFragment::class.java.name, transition)

    override fun showAdditionalSettings(transition: FragmentTranstion) {

    }

    override fun finishCreation(backwards : Boolean) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_place_view)
    }

    override fun onStart() {
        super.onStart()

        if (supportFragmentManager.findFragmentByTag(PRESENTER_TAG) == null) {
            presenter = CreatePlacePresenter(this)
            supportFragmentManager.beginTransaction()
                    .add(presenter, PRESENTER_TAG)
                    .commitNow()
            presenter?.startCreation()
        } else {
            presenter = supportFragmentManager.findFragmentByTag(PRESENTER_TAG) as CreatePlacePresenter
            presenter?.view = this
            presenter?.restoreState()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_place_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next -> {
                presenter?.next()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun replaceFragmentWithAnimation(fragment: android.support.v4.app.Fragment, tag: String, transition: FragmentTranstion) {
        val transaction = supportFragmentManager.beginTransaction()
        when (transition) {
            FragmentTranstion.NONE -> {}
            FragmentTranstion.FORWARD -> transaction.setCustomAnimations(
                                            R.anim.from_right, R.anim.to_left,
                                            R.anim.from_left, R.anim.to_right)
            FragmentTranstion.BACKWARD -> transaction.setCustomAnimations(
                                            R.anim.from_left, R.anim.to_right,
                                            R.anim.from_right, R.anim.to_left)
        }
        transaction.replace(R.id.create_place_fragment, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode and 0xFF == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val pickedPlace : Place = PlacePicker.getPlace(this, data)
                (supportFragmentManager.findFragmentById(R.id.create_place_fragment)
                    as PlaceChooserFragment).locationSubject.onNext(pickedPlace)
                presenter?.setCurrentPlace {
                    place ->
                        place.latitude = pickedPlace.latLng.latitude
                        place.longitude = pickedPlace.latLng.longitude
                        place.name = pickedPlace.name.toString()
                }

            }
        }
    }

    override fun onBackPressed() {
        presenter?.back()
    }
}