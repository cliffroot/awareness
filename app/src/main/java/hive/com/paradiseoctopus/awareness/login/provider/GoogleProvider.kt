package hive.com.paradiseoctopus.awareness.login.provider

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import hive.com.paradiseoctopus.awareness.R
import rx.subjects.PublishSubject

/**
 * Created by edanylenko on 9/29/16.
 */


object GoogleProvider : GoogleApiClient.OnConnectionFailedListener{

    val GOOGLE_REQUEST_CODE = 12003

    fun setupGoogleLogin(appCompatActivity: AppCompatActivity, mAuth: FirebaseAuth, providerSubject: PublishSubject<FirebaseUser?>,
                         activityResultSubject: PublishSubject<Triple<Int, Int, Intent>>) {
        appCompatActivity.findViewById(R.id.sign_in_button).setOnClickListener { signIn(appCompatActivity) }

        activityResultSubject.filter { it.first == GOOGLE_REQUEST_CODE }.subscribe {
            triple ->
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(triple.third)
                handleSignInResult(result, mAuth, appCompatActivity, providerSubject)
        }
    }

    fun signIn(appCompatActivity: AppCompatActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(appCompatActivity.getString(R.string.google_servers_key)).requestEmail().build()

        val mGoogleApiClient = GoogleApiClient.Builder(appCompatActivity)
                .enableAutoManage(appCompatActivity, this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        appCompatActivity.startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE)
    }

    private fun handleSignInResult(result: GoogleSignInResult, mAuth : FirebaseAuth, appCompatActivity: AppCompatActivity,
                                   providerSubject: PublishSubject<FirebaseUser?>) {
        if (result.isSuccess) {
            val acct = result.signInAccount
            firebaseAuthWithGoogle(acct!!, mAuth, appCompatActivity, providerSubject)
        } else {
            providerSubject.onNext(null)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, mAuth: FirebaseAuth, appCompatActivity: AppCompatActivity,
                                       providerSubject: PublishSubject<FirebaseUser?>) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(appCompatActivity) { task ->
            if (task.isSuccessful) {
                providerSubject.onNext(task.result.user)
            } else {
                providerSubject.onNext(null)
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) { /* return nothing */ }

}
