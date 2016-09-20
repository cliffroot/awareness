package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hive.com.paradiseoctopus.awareness.R

/**
 * Created by cliffroot on 15.09.16.
 */

class DeviceChooserFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.i("TAG", "onCreateView")
        return inflater.inflate(R.layout.device_chooser_fragment, container, false)
    }

}