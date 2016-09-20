package hive.com.paradiseoctopus.awareness.createplace

import android.content.Context
import android.net.wifi.ScanResult
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.R

/**
 * Created by edanylenko on 9/20/16.
 */

class DevicesRecyclerAdapter(val context: Context, val scans: List<ScanResult>) : RecyclerView.Adapter<DevicesRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.devices_row, parent, false)
        val vh = ViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.ssid?.text = scans[position].SSID
        holder?.icon?.setImageDrawable(context.resources.getDrawable(R.drawable.wifi_black))
    }

    override fun getItemCount(): Int = scans.count()

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v) {
        val icon : ImageView = v.findViewById(R.id.device_icon) as ImageView
        val ssid : TextView = v.findViewById(R.id.device_ssid)  as TextView

    }
}