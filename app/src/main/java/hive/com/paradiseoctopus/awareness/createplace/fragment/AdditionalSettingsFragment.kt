package hive.com.paradiseoctopus.awareness.createplace.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceContracts
import hive.com.paradiseoctopus.awareness.createplace.intervalFromField
import hive.com.paradiseoctopus.awareness.createplace.intervalToField
import hive.com.paradiseoctopus.awareness.createplace.nameField
import rx.subjects.ReplaySubject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by edanylenko on 9/21/16.
 */

class AdditionalSettingsFragment(var presenter : CreatePlaceContracts.PlacePresenter? = null,
                                 var foundName : String? = "", var deviceCode : String? = "",
                                 var selectedFromTime : Pair<Int, Int> = Pair(0,0),
                                 var selectedToTime   : Pair<Int, Int> = Pair(0,0)) : Fragment(), WithProgress {

    lateinit var fromIntervalButton : Button
    lateinit var toIntervalButton : Button
    lateinit var placeNameView : EditText
    lateinit var intervalsEnabledView : CheckBox
    lateinit var placeCodeView : TextView

    var readySubject : ReplaySubject<Boolean> = ReplaySubject.create()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val v : View = inflater.inflate(R.layout.additional_options_fragment, container, false)
        fromIntervalButton = v.findViewById(R.id.from_time_picker) as Button
        toIntervalButton = v.findViewById(R.id.to_time_picker) as Button
        placeNameView = v.findViewById(R.id.place_name) as EditText
        intervalsEnabledView = v.findViewById(R.id.use_intervals) as CheckBox
        placeCodeView = v.findViewById(R.id.place_code) as TextView

        readySubject.onNext(true)
        readySubject.onCompleted()

        return v
    }

    fun load() : Fragment {
        readySubject.subscribe {
            placeNameView.text = SpannableStringBuilder(foundName as String)
            placeNameView.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) { presenter?.placeDetailsRetrieved(hashMapOf(nameField to s.toString()))}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            })

            val intervalEnabled = selectedFromTime != selectedToTime

            with(intervalEnabled) {
                fromIntervalButton.isEnabled = this
                toIntervalButton.isEnabled = this
                intervalsEnabledView.isChecked = this
            }


            intervalsEnabledView.setOnCheckedChangeListener {
                checkbox, selected ->
                fromIntervalButton.isEnabled = selected
                toIntervalButton.isEnabled = selected
                if (!selected) updateTime(Pair(0, 0), Pair(0, 0))
            }

            placeCodeView.text = deviceCode
            setupTimePicker(fromIntervalButton, { h, m -> selectedFromTime = h to m} )
            setupTimePicker(toIntervalButton,   { h, m -> selectedToTime = h to m} )
        }
        return this
    }

    private fun setupTimePicker(button: Button, updateFunc : (Int, Int) -> Unit) {
        button.text = time24to12(selectedFromTime.first, selectedFromTime.second)
        button.setOnClickListener {
            view -> TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener {
                    timePicker, hours, minutes ->
                    button.text = time24to12(hours, minutes)
                    updateFunc(hours, minutes)
                    updateTime(selectedFromTime, selectedToTime)
                },
                selectedFromTime.first, selectedFromTime.second, resources.getBoolean(R.bool.use24hformat)).show()
        }
    }

    private fun updateTime(from : Pair<Int, Int> , to : Pair <Int, Int>) {
        presenter?.placeDetailsRetrieved(hashMapOf(intervalFromField to from, intervalToField to to))
    }

    private fun time24to12 (hours : Int, minutes : Int) : String{
        val s = "$hours:$minutes:00"
        val f1 = SimpleDateFormat("HH:mm:ss", Locale.US)
        val d = f1.parse(s)
        val f2 = SimpleDateFormat("h:mma", Locale.US)
        return f2.format(d).toLowerCase()
    }

    override fun progress(running: Boolean) {
        // nothing to do, all data is available
    }

}
