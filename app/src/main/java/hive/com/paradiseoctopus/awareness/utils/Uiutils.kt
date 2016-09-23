package hive.com.paradiseoctopus.awareness.utils

import android.content.Context
import android.util.TypedValue

/**
 * Created by edanylenko on 9/23/16.
 */


object UiUtils {
    fun dpToPx (context : Context, dp : Int) : Int{
        val r = context.resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics)
        return px.toInt()
    }
}