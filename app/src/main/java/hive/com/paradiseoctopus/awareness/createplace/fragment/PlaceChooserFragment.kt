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
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceContracts
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

    var mapView : MapView? = null
    var changePlaceButton : Button? = null
    var progressBar : ProgressBar? = null

    val locationSubject : PublishSubject<Place> = PublishSubject.create()
    var readySubject : ReplaySubject<Boolean>? = ReplaySubject.create()

    override fun progress(running: Boolean) {
        progressBar?.visibility = if (running) View.VISIBLE else View.GONE
        mapView?.visibility = if (!running) View.VISIBLE else View.GONE
        changePlaceButton?.visibility = if (!running) View.VISIBLE else View.GONE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.place_chooser_fragment, container, false)

        mapView = v.findViewById(R.id.current_location_snapshot) as MapView
        mapView?.onCreate(savedInstanceState)
        changePlaceButton = v.findViewById(R.id.change_location_button) as Button
        progressBar = v.findViewById(R.id.progress_bar) as ProgressBar

        readySubject?.onNext(true)
        readySubject?.onCompleted()

        return v
    }


    fun load() : Fragment {
        if (mapView == null) {
            readySubject?.subscribe {
                loadMap()
                setupPicker()
            }
        } else {
            loadMap()
            setupPicker()
        }
        return this
    }

    fun loadMap() {
        mapView?.getMapAsync {
            map ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location!!, 13.5f))
                val marker : Marker = map.addMarker(MarkerOptions().position(location!!).title(name))
                marker.showInfoWindow()
                map.uiSettings.setAllGesturesEnabled(false)

                if (presenter?.hasPlaceImage(location?.latitude!!, location?.longitude!!) == false) {
                    Observable.just(1).delay(500, TimeUnit.MILLISECONDS).subscribe {
                        map.snapshot { bitmap -> presenter?.mapSnapshotRetrieved(bitmap) }
                    }
                }
        }

        locationSubject.subscribe {
            place ->
                mapView?.getMapAsync {
                    map ->
                        map.clear()
                        map.moveCamera (CameraUpdateFactory.newLatLngZoom(place.latLng, 13.5f))
                        val marker : Marker = map.addMarker(MarkerOptions().position(place.latLng)
                                .title( if (place.name == null)
                                    resources.getString(R.string.new_marker) else place.name.toString()))
                        marker.showInfoWindow()

                        if (presenter?.hasPlaceImage(place.latLng.latitude, place.latLng.longitude) == false) {
                            Observable.just(1).delay(500, TimeUnit.MILLISECONDS).subscribe {
                                map.snapshot { bitmap -> presenter?.mapSnapshotRetrieved(bitmap) }
                            }
                        }
                }
        }
    }

    fun setupPicker() {
        changePlaceButton?.setOnClickListener {
            view ->
                val builder = PlacePicker.IntentBuilder()
                startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
        }
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

}