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
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceContracts
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import java.text.SimpleDateFormat

/**
 * Created by edanylenko on 9/21/16.
 */

class AdditionalSettingsFragment(val presenter : CreatePlaceContracts.PlacePresenter? = null,
                                 val foundName : String? = "", val deviceCode : String? = "",
                                 var selectedFromTime : Pair<Int, Int> = Pair(0,0),
                                 var selectedToTime   : Pair<Int, Int> = Pair(0,0)) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.additional_options_fragment, container, false)

        val fromIntervalButton : Button = v.findViewById(R.id.from_time_picker) as Button
        val toIntervalButton : Button = v.findViewById(R.id.to_time_picker) as Button
        val placeNameView : EditText = v.findViewById(R.id.place_name) as EditText
        val intervalsEnabledView : CheckBox = v.findViewById(R.id.use_intervals) as CheckBox

        placeNameView.text = SpannableStringBuilder(foundName as String)
        placeNameView.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) { presenter?.nameRetrieved((view as TextView).text.toString()) }
        }

        val intervalEnabled = selectedFromTime != selectedToTime
        with (intervalEnabled) {
            fromIntervalButton.isEnabled = this
            toIntervalButton.isEnabled = this
            intervalsEnabledView.isChecked = this
        }


        intervalsEnabledView.setOnCheckedChangeListener {
            checkbox, selected ->
                fromIntervalButton.isEnabled = selected
                toIntervalButton.isEnabled = selected
                if (!selected) updateTime(Pair(0,0), Pair(0,0))
        }

        (v.findViewById(R.id.place_code) as TextView).text = deviceCode

        setupFromPicker(fromIntervalButton)
        setupToPicker(toIntervalButton)
        return v
    }

    fun setupFromPicker(button : Button) {
        button.text = time24to12(selectedFromTime.first, selectedFromTime.second)
        button.setOnClickListener {
            view -> TimePickerDialog(context,
                        TimePickerDialog.OnTimeSetListener {
                            timePicker, hours, minutes ->
                            button.text = time24to12(hours, minutes)
                            selectedFromTime = Pair(hours, minutes)
                            updateTime(selectedFromTime, selectedToTime)
                        },
                        selectedFromTime.first, selectedFromTime.second, false).show() // TODO : use 24h format to be moved to resources
        }
    }

    fun setupToPicker (button : Button) {
        button.text = time24to12(selectedToTime.first, selectedToTime.second)
        button.setOnClickListener {
            view -> TimePickerDialog(context,TimePickerDialog.OnTimeSetListener {
                        timePicker, hours, minutes ->
                        button.text = time24to12(hours, minutes)
                        selectedToTime = Pair(hours, minutes)
                        updateTime(selectedFromTime, selectedToTime)
                    },
                    selectedToTime.first, selectedToTime.second, false).show()
        }
    }

    fun updateTime(from : Pair<Int, Int> , to : Pair <Int, Int>) {
        (activity as CreatePlaceView).presenter?.intervalsRetrieved(from, to)
    }

    fun time24to12 (hours : Int, minutes : Int) : String{
        val s = "$hours:$minutes:00"
        val f1 = SimpleDateFormat("HH:mm:ss")
        val d = f1.parse(s)
        val f2 = SimpleDateFormat("h:mma")
        return f2.format(d).toLowerCase()
    }

}
