package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceContracts
import hive.com.paradiseoctopus.awareness.createplace.mapSnapshotField
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject
import java.util.concurrent.TimeUnit

/**
 * Created by cliffroot on 15.09.16.
 */

val PLACE_PICKER_REQUEST : Int = 13

class PlaceChooserFragment(var presenter : CreatePlaceContracts.PlacePresenter? = null,
                           var location : LatLng? = null, var name : String? = null) : Fragment(), WithProgress {

    lateinit var mapView : MapView
    lateinit var changePlaceButton : Button
    lateinit var progressBar : ProgressBar

    val locationSubject : PublishSubject<Place> = PublishSubject.create()
    var readySubject : ReplaySubject<Boolean> = ReplaySubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.place_chooser_fragment, container, false)

        mapView = v.findViewById(R.id.current_location_snapshot) as MapView
        mapView.onCreate(savedInstanceState)
        changePlaceButton = v.findViewById(R.id.change_location_button) as Button
        progressBar = v.findViewById(R.id.progress_bar) as ProgressBar

        readySubject.onNext(true)
        readySubject.onCompleted()

        return v
    }


    fun load(): Fragment {
        readySubject.subscribe {
            loadMap()
            setupPicker()
        }
        return this
    }

    fun loadMap() {
        mapView.getMapAsync {
            map ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location!!, 13.5f))
                val marker : Marker = map.addMarker(MarkerOptions().position(location!!).title(name))
                marker.showInfoWindow()
                map.uiSettings.setAllGesturesEnabled(false)

                if (presenter?.hasPlaceImage(location?.latitude!!, location?.longitude!!) == false) {
                    Observable.just(1).delay(500, TimeUnit.MILLISECONDS).subscribe {
                        map.snapshot { bitmap -> presenter?.placeDetailsRetrieved(hashMapOf(mapSnapshotField to bitmap)) }
                    }
                }
        }

        locationSubject.subscribe {
            place -> mapView.getMapAsync {
                map ->
                    map.clear()
                    map.moveCamera (CameraUpdateFactory.newLatLngZoom(place.latLng, 13.5f))
                    val marker : Marker = map.addMarker(MarkerOptions().position(place.latLng))

                    presenter?.coordinatesToName(place.latLng, place.name.toString())?.subscribe(
                    {
                        updateMarker(marker, it ?: resources.getString(R.string.new_marker))
                        delayedScreenshotMapIfNecessary(place.latLng.latitude, place.latLng.longitude, map)
                    },
                    {
                        updateMarker(marker, name!!)
                        delayedScreenshotMapIfNecessary(place.latLng.latitude, place.latLng.longitude, map)
                    })
            }
        }
    }

    fun updateMarker (marker : Marker, name : String) {
        marker.title = name
        marker.showInfoWindow()
    }

    fun delayedScreenshotMapIfNecessary(latitude : Double, longitude : Double, map : GoogleMap) {
        if (presenter?.hasPlaceImage(latitude, longitude) == false) {
            Observable.just(1).delay(500, TimeUnit.MILLISECONDS).subscribe {
                map.snapshot { bitmap -> presenter?.placeDetailsRetrieved(hashMapOf(mapSnapshotField to bitmap)) }
            }
        }
    }

    fun setupPicker() {
        changePlaceButton.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
        }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun progress(running: Boolean) {
        readySubject.subscribe {
            progressBar.visibility = if (running) View.VISIBLE else View.GONE
            mapView.visibility = if (!running) View.VISIBLE else View.GONE
            changePlaceButton.visibility = if (!running) View.VISIBLE else View.GONE
        }
    }

}