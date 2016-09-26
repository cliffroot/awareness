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
import android.widget.ProgressBar
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceContracts
import hive.com.paradiseoctopus.awareness.createplace.adapter.DevicesRecyclerAdapter
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject

/**
 * Created by cliffroot on 15.09.16.
 */

class DeviceChooserFragment(var presenter : CreatePlaceContracts.PlacePresenter? = null,
                            var devices: List<ScanResult>? = null, var selectedSsid: String? = null) : Fragment(), WithProgress {

    override fun progress(running: Boolean) {
        readySubject.subscribe {
            progressBar?.visibility = if (running) View.VISIBLE else View.GONE
            recyclerView?.visibility = if (!running) View.VISIBLE else View.INVISIBLE
        }
    }

    var recyclerView : RecyclerView? = null
    var progressBar : ProgressBar? = null

    var readySubject : ReplaySubject<Boolean> = ReplaySubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v :  View = inflater.inflate(R.layout.device_chooser_fragment, container, false)

        recyclerView = v.findViewById(R.id.devices_recycler_view) as RecyclerView
        progressBar = v.findViewById(R.id.progress_bar) as ProgressBar

        readySubject.onNext(true)
        readySubject.onCompleted()
        return v
    }

    fun load() : Fragment {
        readySubject.subscribe {
            setupDevicesView()
        }
        return this
    }

    fun setupDevicesView() {

        val layoutManager : LinearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = layoutManager
        val itemAnimator : RecyclerView.ItemAnimator = DefaultItemAnimator()
        recyclerView?.itemAnimator = itemAnimator

        val filteredDevices = devices?.distinctBy {  device -> device.SSID }
        val observableSelectedDevice : PublishSubject<Int> = PublishSubject.create()
        observableSelectedDevice.subscribe{ selected -> presenter?.deviceRetrieved(filteredDevices!![selected].SSID) }

        recyclerView?.adapter = DevicesRecyclerAdapter(context, filteredDevices!!, observableSelectedDevice,
                            filteredDevices.indexOfFirst { device -> device.SSID == selectedSsid })

    }

}