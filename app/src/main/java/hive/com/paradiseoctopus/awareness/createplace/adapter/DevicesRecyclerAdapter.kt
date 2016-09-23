package hive.com.paradiseoctopus.awareness.createplace.adapter

import android.content.Context
import android.net.wifi.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.R
import rx.subjects.PublishSubject

/**
 * Created by edanylenko on 9/20/16.
 */

class DevicesRecyclerAdapter(val context: Context, val scans: List<ScanResult>, val resultObservable : PublishSubject<Int>,
                             var selectedPosition : Int)
        : RecyclerView.Adapter<DevicesRecyclerAdapter.ViewHolder>() {

    var previousRadioButton : RadioButton? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.devices_row, parent, false)
        val vh = ViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.ssid?.text = scans[position].SSID
        holder?.icon?.setImageDrawable(context.resources.getDrawable(R.drawable.wifi_black))
        if (position == selectedPosition) {
            holder?.radioButton?.isChecked = true
            previousRadioButton = holder?.radioButton
        }
        holder?.radioButton?.setOnCheckedChangeListener {
            compoundButton, selected ->
            if (selected) {
                selectedPosition = position
                previousRadioButton?.isChecked = false
                previousRadioButton = holder.radioButton
                resultObservable.onNext(position)
            }
        }
        (holder?.itemView)?.setOnClickListener {
            view -> holder?.radioButton?.isChecked = true
        }
    }

    override fun getItemCount(): Int = scans.count()

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val icon : ImageView = v.findViewById(R.id.device_icon) as ImageView
        val ssid : TextView = v.findViewById(R.id.device_ssid)  as TextView
        val radioButton : RadioButton = v.findViewById(R.id.selected_device) as RadioButton

    }
}