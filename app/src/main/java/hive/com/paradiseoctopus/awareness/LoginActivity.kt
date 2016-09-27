package hive.com.paradiseoctopus.awareness

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by cliffroot on 27.09.16.
 */

class LoginActivity : AppCompatActivity() {

    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    val callbackManager: CallbackManager by lazy {
        com.facebook.CallbackManager.Factory.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_login)


        val loginButton = findViewById(R.id.login_button) as LoginButton
        loginButton.setReadPermissions("email", "public_profile")

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.i("TAG", "loginRes <~ $loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.i("Error", "Error,eCANCELD")
            }

            override fun onError(exception: FacebookException) {
                Log.i("Error", "Error,error <~ $exception")
            }
        })

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                Log.d("TAG", "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                // User is signed out
                Log.d("TAG", "onAuthStateChanged:signed_out")
            }
        }
        Log.e("Overlay", "$mAuthListener")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        Log.e("Overlay", "$mAuthListener")
        mAuth.addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("TAG", "handleFacebookAccessToken:" + token)

        val credential = FacebookAuthProvider.getCredential(token.getToken())
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            Log.d("TAG", "signInWithCredential:onComplete:" + task.isSuccessful)

            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
                Log.w("TAG", "signInWithCredential", task.getException())
                Toast.makeText(this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
            } else {
                Log.e("Overlay", "user ~> ${task.result.user.email}")
            }

            // ...
        }
    }
}
