package hive.com.paradiseoctopus.awareness

import android.app.Application
import com.facebook.FacebookSdk

/**
 * Created by cliffroot on 27.09.16.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
    }
}