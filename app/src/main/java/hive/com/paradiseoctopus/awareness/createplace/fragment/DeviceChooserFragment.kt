package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.net.wifi.ScanResult
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import hive.com.paradiseoctopus.awareness.createplace.DevicesRecyclerAdapter
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 15.09.16.
 */

class DeviceChooserFragment(val devices: List<ScanResult>? = null, val selectedSsid: String? = null) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v :  View = inflater.inflate(R.layout.device_chooser_fragment, container, false)
        setupDevicesView(v)
        return v
    }

    fun setupDevicesView(v: View) {
        val recyclerView : RecyclerView = v.findViewById(R.id.devices_recycler_view) as RecyclerView
        val layoutManager : LinearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val itemAnimator : RecyclerView.ItemAnimator = DefaultItemAnimator()
        recyclerView.itemAnimator = itemAnimator

        val filteredDevices = devices?.distinctBy {  device -> device.SSID }
        val observableSelectedDevice : PublishSubject<Int> = PublishSubject.create()
        observableSelectedDevice.subscribe{ selected ->
            (activity as CreatePlaceView).presenter?.deviceRetrieved(filteredDevices!![selected].SSID)
        }

        recyclerView.adapter =
                DevicesRecyclerAdapter(context, filteredDevices!!, observableSelectedDevice,
                        filteredDevices.indexOfFirst
                        { device -> device.SSID == selectedSsid })

    }

}