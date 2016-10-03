package hive.com.paradiseoctopus.awareness

import android.app.Application
import com.facebook.FacebookSdk
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import hive.com.paradiseoctopus.awareness.login.provider.FacebookProvider

/**
 * Created by cliffroot on 27.09.16.
 */

class App : Application() {

    val firebaseAuth : FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    lateinit var firebaseDatabase : FirebaseDatabase

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext, FacebookProvider.FACEBOOK_REQUEST_CODE)
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
}