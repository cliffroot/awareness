package hive.com.paradiseoctopus.awareness

import android.app.Application
import com.facebook.FacebookSdk
import hive.com.paradiseoctopus.awareness.login.provider.FacebookProvider

/**
 * Created by cliffroot on 27.09.16.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext, FacebookProvider.FACEBOOK_REQUEST_CODE)
    }
}