package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 15.09.16.
 */

val PLACE_PICKER_REQUEST : Int = 13

class PlaceChooserFragment : Fragment() {

    var mapView : MapView? = null
    val locationSubject : PublishSubject<Place> = PublishSubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {

        val v : View = inflater.inflate(R.layout.place_chooser_fragment, container, false)

        loadMap(v, savedInstanceState)
        setupPicker(v)
        return v
    }

    fun loadMap(v: View, savedInstanceState: Bundle?) {
        mapView = v.findViewById(R.id.current_location_snapshot) as MapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync {
            map -> (activity as CreatePlaceView).presenter.getCurrentLocation()
                .subscribe {
                    location ->
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 13.5f))
                    map.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude))
                            .title(resources.getString(R.string.my_location)))
                    map.uiSettings.setAllGesturesEnabled(false)
                }

        }

        locationSubject.subscribe {
            place ->
                mapView?.getMapAsync {
                    map ->
                        map.clear()
                        map.moveCamera (CameraUpdateFactory.newLatLngZoom(place.latLng, 13.5f))
                        map.addMarker(MarkerOptions().position(place.latLng)
                                .title( if (place.name == null)
                                    resources.getString(R.string.new_marker) else place.name.toString()))
                }
        }
    }

    fun setupPicker(v : View) {
        val changePlaceButton : Button = v.findViewById(R.id.change_location_button) as Button
        changePlaceButton.setOnClickListener {
            view ->
                val builder = PlacePicker.IntentBuilder()
                Log.e("Overlay", "Place: " + PLACE_PICKER_REQUEST)
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