package hive.com.paradiseoctopus.awareness.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import hive.com.paradiseoctopus.awareness.MainActivity
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.login.provider.FacebookProvider
import hive.com.paradiseoctopus.awareness.login.provider.GoogleProvider
import rx.subjects.PublishSubject

/**
 * Created by cliffroot on 27.09.16.
 */

class LoginActivity : AppCompatActivity(){

    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    val activityResultSubject : PublishSubject<Triple<Int, Int, Intent>> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_login)
        setupAuthlistener()
    }


    fun setupAuthlistener() {
        if (mAuth.currentUser == null) {
            val providerSubject : PublishSubject<FirebaseUser?> = PublishSubject.create()
            FacebookProvider.setupFacebookLogin(this, mAuth, providerSubject, activityResultSubject)
            GoogleProvider.setupGoogleLogin(this, mAuth, providerSubject, activityResultSubject)
            providerSubject.filter{ it != null }.first().subscribe{
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultSubject.onNext(Triple(requestCode, resultCode, data))
    }

}