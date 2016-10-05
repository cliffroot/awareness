package hive.com.paradiseoctopus.awareness.createplace

import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.adapter.CreatePlaceViewPagerAdapter
import hive.com.paradiseoctopus.awareness.createplace.fragment.*
import hive.com.paradiseoctopus.awareness.createplace.helper.FragmentTranstion
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 14.09.16.
 */

class CreatePlaceWithPagerView : AppCompatActivity(), CreatePlaceContracts.PlaceView {

    val PAGE_SELECTED_KEY = "pageSelectedKey"

    val PRESENTER_TAG = "CreatePlacePresenter"
    var presenter : CreatePlaceContracts.PlacePresenter? = null
    var menuItemNext : MenuItem? = null

    var adapter : CreatePlaceViewPagerAdapter? = null
    var currentPageSelected : Int = 0

    var placePickerFragment : PlaceChooserFragment = PlaceChooserFragment()
    var devicePickerFragment: DeviceChooserFragment = DeviceChooserFragment()
    var otherOptionsFragment: AdditionalSettingsFragment = AdditionalSettingsFragment()

    override fun showPlaceChooser(transition: FragmentTranstion, location: LatLng, name: String) {
        placePickerFragment.presenter = presenter
        placePickerFragment.location = location
        placePickerFragment.name = name
        placePickerFragment.load()

    }

    override fun showDeviceChooser(transition: FragmentTranstion, savedNetwork: List<ScanResult>, selectedSsid: String?) {
        devicePickerFragment.presenter = presenter
        devicePickerFragment.devices = savedNetwork
        devicePickerFragment.selectedSsid = selectedSsid
        devicePickerFragment.load()
    }

    override fun showAdditionalSettings(transition: FragmentTranstion, intervalFrom: Pair<Int, Int>, intervalTo: Pair<Int, Int>, placeCode: String, placeName: String) {
        menuItemNext?.isVisible = true
        menuItemNext?.setTitle(R.string.finish)

        otherOptionsFragment.presenter = presenter
        otherOptionsFragment.selectedFromTime = intervalFrom
        otherOptionsFragment.selectedToTime = intervalTo
        otherOptionsFragment.deviceCode = placeCode
        otherOptionsFragment.foundName = placeName
        otherOptionsFragment.load()
    }

    override fun progress(running: Boolean) {
        if (adapter?.getItem(currentPageSelected) != null) {
            (adapter?.getItem(currentPageSelected) as WithProgress).progress(running)
        }
    }

    override fun dismiss(resultObservable: PublishSubject<Boolean>) {
        AlertDialog.Builder(this).setTitle(R.string.are_you_sure)
                .setMessage(R.string.dismiss_new_place)
                .setPositiveButton(R.string.yes,
                        { dialogInterface, i ->
                            resultObservable.onNext(true)
                            resultObservable.onCompleted()
                            finish()})
                .setNegativeButton(R.string.no,
                        { dialogInterface, i ->
                            resultObservable.onNext(false)
                            resultObservable.onCompleted()
                            dialogInterface.dismiss()})
                .show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_place_view_with_pager)
        if (savedInstanceState != null) {
            currentPageSelected = savedInstanceState.getInt(PAGE_SELECTED_KEY, 0)
        }

        //restore fragments used by adapter
        val f1 : Fragment? = supportFragmentManager.findFragmentByTag( "android:switcher:${R.id.create_place_pager}:0" )
        val f2 : Fragment? = supportFragmentManager.findFragmentByTag( "android:switcher:${R.id.create_place_pager}:1" )
        val f3 : Fragment? = supportFragmentManager.findFragmentByTag( "android:switcher:${R.id.create_place_pager}:2" )

        if (f1 != null) placePickerFragment =  f1 as PlaceChooserFragment
        if (f2 != null) devicePickerFragment = f2 as DeviceChooserFragment
        if (f3 != null) otherOptionsFragment = f3 as AdditionalSettingsFragment

        adapter = CreatePlaceViewPagerAdapter(supportFragmentManager, placePickerFragment, devicePickerFragment, otherOptionsFragment)
        val viewPager : ViewPager = (findViewById(R.id.create_place_pager) as ViewPager)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
            override fun onPageScrollStateChanged(state: Int) { }

            override fun onPageSelected(position: Int) {
                menuItemNext?.isVisible = false
                if (currentPageSelected < position) {
                    currentPageSelected = position
                    presenter?.next()
                } else if (currentPageSelected > position) {
                    currentPageSelected = position
                    presenter?.back()
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(PAGE_SELECTED_KEY, currentPageSelected)
    }

    override fun onStart() {
        super.onStart()

        if (supportFragmentManager.findFragmentByTag(PRESENTER_TAG) == null) {
            presenter = CreatePlacePresenter(this)
            supportFragmentManager.beginTransaction()
                    .add(presenter as Fragment, PRESENTER_TAG)
                    .commitNow()
            presenter?.startCreation()
        } else {
            presenter = supportFragmentManager.findFragmentByTag(PRESENTER_TAG) as CreatePlaceContracts.PlacePresenter
            presenter?.provideView(this)
            presenter?.restoreState()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode and 0xFF == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val pickedPlace : Place = PlacePicker.getPlace(this, data)
                (supportFragmentManager.findFragmentByTag("android:switcher:${R.id.create_place_pager}:0")
                    as PlaceChooserFragment).locationSubject.onNext(pickedPlace)

                presenter?.placeDetailsRetrieved(hashMapOf(latitudeField to pickedPlace.latLng.latitude,
                                                           longitudeField to pickedPlace.latLng.longitude,
                                                           nameField to pickedPlace.name,
                                                           placeIdField to pickedPlace.id))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.create_place_menu, menu)
        menuItemNext = menu.findItem(R.id.action_next)
        menuItemNext?.isVisible = false
        menu.findItem(R.id.action_cancel).isVisible = false
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

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        presenter?.dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        PermissionUtility.permissionSubject.onNext(Pair(requestCode,
                grantResults.all { res -> res == PackageManager.PERMISSION_GRANTED } ))
        PermissionUtility.permissionSubject.onCompleted()
    }
}

