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
            Log.e("Overlay", "failed to create bitmap", ex)
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444)
        }
    }

    fun saveBitmap(context : Context, bitmap: Bitmap, name : String) : String{
        if (imageName != null) File(imageName).delete()
        imageName = name + SUFFIX
        val fileToSaveTo : File  = File(context.applicationInfo.dataDir, imageName)
        FileOutputStream(fileToSaveTo).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Log.e("Overlay", "written to file ~> $fileToSaveTo ")
        return imageName!!
    }

    fun imageExists(context : Context, name : String) : Boolean {
        Log.e("Overlay", "check if ${File(context.applicationInfo.dataDir, name + SUFFIX)} exists")
        return File(context.applicationInfo.dataDir, name + SUFFIX).exists()
    }

}