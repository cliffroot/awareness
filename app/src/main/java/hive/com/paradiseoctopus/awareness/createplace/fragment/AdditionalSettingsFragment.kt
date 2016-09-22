package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import java.text.SimpleDateFormat

/**
 * Created by edanylenko on 9/21/16.
 */

class AdditionalSettingsFragment(val foundName : String? = "", val deviceCode : String? = "") : Fragment() {

    var selectedFromTime : Pair<Int, Int> = Pair(21, 0)
    var selectedToTime : Pair<Int, Int> = Pair(1,0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.additional_options_fragment, container, false)

        val fromIntervalButton : Button = v.findViewById(R.id.from_time_picker) as Button
        val toIntervalButton : Button = v.findViewById(R.id.to_time_picker) as Button
        val placeNameView : EditText = v.findViewById(R.id.place_name) as EditText

        placeNameView.text = SpannableStringBuilder(foundName as String)
        placeNameView.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                (activity as CreatePlaceView).presenter?.nameRetrieved((view as TextView).text.toString())
            }
        }
        (v.findViewById(R.id.use_intervals) as CheckBox).setOnCheckedChangeListener {
            checkbox, selected ->
                fromIntervalButton.isEnabled = selected
                toIntervalButton.isEnabled = selected
                if (!selected) updateTime(null, null)
        }

        (v.findViewById(R.id.place_code) as TextView).text = deviceCode

        setupFromPicker(fromIntervalButton)
        setupToPicker(toIntervalButton)
        return v
    }

    fun setupFromPicker(button : Button) =
            button.setOnClickListener {
            view ->
            TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener {
                        timePicker, hours, minutes ->
                        button.text = time24to12(hours, minutes)
                        selectedFromTime = Pair(hours, minutes)
                        updateTime(selectedFromTime, selectedToTime)
                    },
                    selectedFromTime.first, selectedFromTime.second, false).show() // TODO : use 24h format to be moved to resources
    }

    fun setupToPicker (button : Button) =
            button.setOnClickListener {
            view ->
            TimePickerDialog(context,
                    TimePickerDialog.OnTimeSetListener {
                        timePicker, hours, minutes ->
                        button.text = time24to12(hours, minutes)
                        selectedToTime = Pair(hours, minutes)
                        updateTime(selectedFromTime, selectedToTime)
                    },
                selectedToTime.first, selectedToTime.second, false).show()
    }

    fun updateTime(from : Pair<Int, Int>? , to : Pair <Int, Int>?) {
        (activity as CreatePlaceView).presenter?.intervalsRetrieved(from!!, to!!)
//        (activity as CreatePlaceView).presenter?.setCurrentPlace {
//            place ->
//                place.intervalFrom = -1
//                place.intervalTo = -1
//                place.intervalFrom = from?.first?.times(DateUtils.HOUR_IN_MILLIS)?.plus(from?.second?.times(DateUtils.MINUTE_IN_MILLIS))
//                place.intervalTo = to?.first?.times(DateUtils.HOUR_IN_MILLIS)?.plus(to?.second?.times(DateUtils.MINUTE_IN_MILLIS))
//        }
    }

    fun time24to12 (hours : Int, minutes : Int) : String{
        val s = "$hours:$minutes:00"
        val f1 = SimpleDateFormat("HH:mm:ss")
        val d = f1.parse(s)
        val f2 = SimpleDateFormat("h:mma")
        return f2.format(d).toLowerCase() // "12:18am"
    }

}
