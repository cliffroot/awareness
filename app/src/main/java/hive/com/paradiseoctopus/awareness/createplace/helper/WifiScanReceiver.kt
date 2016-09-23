package hive.com.paradiseoctopus.awareness.createplace.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject

/**
 * Created by edanylenko on 9/20/16.
 */


class WifiScanReceiver(val context: Context, val resultSubject: ReplaySubject<List<ScanResult>>) : BroadcastReceiver() {

    val wifiScanManager : WifiManager by lazy {
        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

     override fun onReceive(context : Context, intent : Intent) {
        if (intent.action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            resultSubject.onNext(wifiScanManager.scanResults)
            resultSubject.onCompleted()
        }
    }
}