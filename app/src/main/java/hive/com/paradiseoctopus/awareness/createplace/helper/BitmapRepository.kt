package hive.com.paradiseoctopus.awareness.createplace.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Created by edanylenko on 9/23/16.
 */

object BitmapRepository {

    fun cutBitmapCenter (bitmap : Bitmap, width : Int, height : Int) : Bitmap {

        Log.e("Overlay", "${bitmap.width} , ${bitmap.height}")
        val centerX : Int = bitmap.width / 2
        val centerY : Int = bitmap.height / 2
        val cropped = Bitmap.createBitmap(bitmap, centerX - width / 2, centerY - height / 2, width, height)

        bitmap.recycle()
        return cropped
    }

    fun saveBitmap(context : Context, bitmap: Bitmap) : String {
        val name : String = "${Calendar.getInstance().timeInMillis}.map"
        val fileToSaveTo : File  = File(context.applicationInfo.dataDir, name)
        FileOutputStream(fileToSaveTo).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return name
    }

}