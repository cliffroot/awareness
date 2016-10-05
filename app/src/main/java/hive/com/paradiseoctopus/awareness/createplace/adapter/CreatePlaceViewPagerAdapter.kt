package hive.com.paradiseoctopus.awareness.createplace.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by edanylenko on 9/26/16.
 */


class CreatePlaceViewPagerAdapter(fragmentManager: FragmentManager,
                                  var placePickerFragment : Fragment,
                                  var devicePickerFragment: Fragment,
                                  var otherOptionsFragment: Fragment)
        : FragmentPagerAdapter(fragmentManager) {

    val NUM_OF_FRAGMENTS : Int = 3


    override fun getItem(position: Int): Fragment {
        return  when (position) {
            0 -> placePickerFragment
            1 -> devicePickerFragment
            2 -> otherOptionsFragment
            else -> throw IllegalStateException()
        }
    }

    override fun getCount(): Int {
        return NUM_OF_FRAGMENTS
    }

}