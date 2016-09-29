package hive.com.paradiseoctopus.awareness.login.provider

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import hive.com.paradiseoctopus.awareness.R
import rx.subjects.PublishSubject

/**
 * Created by edanylenko on 9/28/16.
 */

object FacebookProvider {

    val FACEBOOK_REQUEST_CODE = 1201

    val callbackManager: CallbackManager by lazy {
        com.facebook.CallbackManager.Factory.create()
    }

    fun setupFacebookLogin(activity: AppCompatActivity, mAuth: FirebaseAuth, resultObservable: PublishSubject<FirebaseUser?>, activityResultSubject: PublishSubject<Triple<Int, Int, Intent>>) {
        val loginButton = activity.findViewById(R.id.login_button) as LoginButton

        loginButton.setReadPermissions("email", "public_profile")

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(activity, loginResult.accessToken, mAuth, resultObservable)
            }

            override fun onCancel() {
                resultObservable.onNext(null)
            }

            override fun onError(exception: FacebookException) {
                resultObservable.onNext(null)
            }
        })

        activityResultSubject.filter{ it.first == FACEBOOK_REQUEST_CODE }.subscribe {
            activityResult -> callbackManager.onActivityResult(activityResult.first, activityResult.second, activityResult.third)
        }
    }

    private fun handleFacebookAccessToken(activity :AppCompatActivity, token: AccessToken,
                                          mAuth : FirebaseAuth , resultObservable : PublishSubject<FirebaseUser?>) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity) { task ->
            if (!task.isSuccessful) {
                resultObservable.onNext(null)
            } else {
                resultObservable.onNext(task.result.user)
            }
        }
    }

}