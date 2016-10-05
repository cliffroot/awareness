package hive.com.paradiseoctopus.awareness.createplace.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Created by edanylenko on 9/23/16.
 */

object BitmapRepository {

    val SUFFIX : String = ".png"
    val TAG = "BitmapRepository"

    val PLACE_API_PREFIX = "p"
    val MAP_SCREENSHOT_PREFIX = "m"

    var imageName : String? = null

    fun cutBitmapCenter (bitmap : Bitmap, width : Int, height : Int) : Bitmap {
        Log.e("Overlay", "${bitmap.width} , ${bitmap.height}")
        val centerX : Int = bitmap.width / 2
        val centerY : Int = bitmap.height / 2
        try {
            val cropped = Bitmap.createBitmap(bitmap, centerX - width / 2, centerY - height / 2, width, height)
            bitmap.recycle()
            return cropped
        } catch (ex : IllegalArgumentException) {
            Log.e(TAG, "failed to create bitmap", ex)
            bitmap.recycle()
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
        }
    }

    fun saveBitmap(context : Context, bitmap: Bitmap, name : String, isPlaceApiImage : Boolean) : String{
        if (imageName != null) File(imageName).apply { delete() }
        imageName = (if (isPlaceApiImage) PLACE_API_PREFIX else MAP_SCREENSHOT_PREFIX) + name + SUFFIX
        val fileToSaveTo : File  = File(context.applicationInfo.dataDir, imageName)
        FileOutputStream(fileToSaveTo).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.e(TAG, "written to file ~> $fileToSaveTo ")
        return imageName!!
    }

    fun imageExists(context : Context, name : String) : Boolean {
        val nameToCheck = PLACE_API_PREFIX + name + SUFFIX
        return File(context.applicationInfo.dataDir, nameToCheck).exists()
    }

}