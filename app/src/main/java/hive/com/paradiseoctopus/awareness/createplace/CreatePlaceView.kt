package hive.com.paradiseoctopus.awareness.createplace

import android.content.Intent
import android.net.wifi.ScanResult
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.fragment.AdditionalSettingsFragment
import hive.com.paradiseoctopus.awareness.createplace.fragment.DeviceChooserFragment
import hive.com.paradiseoctopus.awareness.createplace.fragment.PLACE_PICKER_REQUEST
import hive.com.paradiseoctopus.awareness.createplace.fragment.PlaceChooserFragment

/**
 * Created by cliffroot on 14.09.16.
 */

class CreatePlaceView : AppCompatActivity(), CreatePlaceContracts.PlaceView {

    val PRESENTER_TAG = "CreatePlacePPresenter"
    var presenter : CreatePlacePresenter? = null
    var menuItemNext : MenuItem? = null

    override fun showPlaceChooser(transition: FragmentTranstion, location: LatLng, name: String) {
        replaceFragmentWithAnimation(PlaceChooserFragment(location, name), PlaceChooserFragment::class.java.name, transition)
        menuItemNext?.title = resources.getString(R.string.next)
    }

    override fun showDeviceChooser(transition: FragmentTranstion, savedNetwork: List<ScanResult>, selectedSsid: String?) {
        replaceFragmentWithAnimation(DeviceChooserFragment(savedNetwork, selectedSsid), DeviceChooserFragment::class.java.name, transition)
        menuItemNext?.title = resources.getString(R.string.next)
    }

    override fun showAdditionalSettings(transition: FragmentTranstion, intervalFrom: Pair<Int, Int>,
                                        intervalTo: Pair<Int, Int>, placeCode: String, placeName: String) {
        replaceFragmentWithAnimation(
                AdditionalSettingsFragment(placeName, placeCode, intervalFrom, intervalTo)
                            if (presenter?.place?.code == null) presenter?.generatePlaceCode() else presenter?.place?.code),
                AdditionalSettingsFragment::class.java.name, transition)
        menuItemNext?.title = resources.getString(R.string.finish)

    }

    override fun progress(running: Boolean) {
        findViewById(R.id.create_place_fragment).visibility = if (running) View.INVISIBLE else View.VISIBLE
        findViewById(R.id.progress_bar).visibility = if (running) View.VISIBLE else View.INVISIBLE
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
        menuItemNext = menu.findItem(R.id.action_next)
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
                presenter?.nameRetrieved(pickedPlace.name.toString())
                presenter?.locationRetrieved(latLngToLocation(pickedPlace.latLng))

            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isChangingConfigurations)
            supportFragmentManager.beginTransaction().remove(
                    supportFragmentManager.findFragmentById(R.id.create_place_fragment)).commitNow()
    }

    override fun onBackPressed() {
        presenter?.back()
    }
}

