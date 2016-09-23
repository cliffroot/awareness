package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hive.com.paradiseoctopus.awareness.R
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 15.09.16.
 */

val PLACE_PICKER_REQUEST : Int = 13

class PlaceChooserFragment(val location : LatLng? = null, val name : String? = null) : Fragment() {

    var mapView : MapView? = null
    val locationSubject : PublishSubject<Place> = PublishSubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {

        val v : View = inflater.inflate(R.layout.place_chooser_fragment, container, false)
        return v
    }


    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadMap(v, savedInstanceState)
        setupPicker(v)
    }

    fun loadMap(v: View?, savedInstanceState: Bundle?) {
        mapView = v?.findViewById(R.id.current_location_snapshot) as MapView
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync {
            map ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location!!, 13.5f))
                val marker : Marker = map.addMarker(MarkerOptions().position(location).title(name))
                marker.showInfoWindow()
                map.uiSettings.setAllGesturesEnabled(false)
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
                }
        }
    }

    fun setupPicker(v : View?) {
        val changePlaceButton : Button = v?.findViewById(R.id.change_location_button) as Button
        changePlaceButton.setOnClickListener {
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