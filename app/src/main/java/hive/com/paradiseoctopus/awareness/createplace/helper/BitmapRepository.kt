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

    val PLACE_PREFIX = "place."
    val MAP_PREFIX = "map."

    val SUFFIX : String = ".png"
    val TAG = "BitmapRepository"

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

    fun saveBitmap(context : Context, bitmap: Bitmap, name : String, fromPlaceApi : Boolean) : String {
        if (imageName != null) File(imageName).apply { delete() }
        imageName = (if (fromPlaceApi) PLACE_PREFIX else MAP_PREFIX) + name + SUFFIX
        if (!placeImageExists(context, name)) {
            val fileToSaveTo: File = File(context.applicationInfo.dataDir, imageName)
            FileOutputStream(fileToSaveTo).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            Log.e(TAG, "written to file ~> $fileToSaveTo ")
            return imageName!!
        } else {
            return PLACE_PREFIX + name + SUFFIX
        }
    }

    fun placeImageExists (context : Context, name : String) : Boolean {
        return File(context.applicationInfo.dataDir, PLACE_PREFIX + name + SUFFIX).exists()
    }

    fun imageExists(context : Context, name : String) : Boolean {
        return listOf(PLACE_PREFIX, MAP_PREFIX).map { File(context.applicationInfo.dataDir, it + name + SUFFIX).exists() }.any { it == true }
    }

}