package hive.com.paradiseoctopus.awareness

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import hive.com.paradiseoctopus.awareness.createplace.CreatePlacePresenter
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import rx.Observable
import rx.Scheduler
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    var loc : Observable<Location>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, CreatePlaceView::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray) {
        PermissionUtility.permissionSubject.onNext(Pair(requestCode,
                grantResults.all { res -> res == PackageManager.PERMISSION_GRANTED } ))
        PermissionUtility.permissionSubject.onCompleted()
    }
}
