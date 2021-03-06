package hive.com.paradiseoctopus.awareness.utils

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import rx.Observable
import rx.subjects.ReplaySubject

/**
 * Created by cliffroot on 14.09.16.
 */

object PermissionUtility {

    val REQUEST_LOCATION_CODE = 17
    val REQUEST_WIFI_CODE = 19

    var permissionSubject : ReplaySubject<Pair<Int, Boolean>> = ReplaySubject.create()

    fun requestPermission (host : AppCompatActivity, permissions : List<String>,
                           requestCode : Int) : Observable<Pair<Int, Boolean>> {

        Log.e("Request:", "r: " + permissions)
        val toRequest = permissions.filter {
            permission -> !checkPermission(host, permission)
        }
        Log.e("Request:", "r: " + toRequest)
        permissionSubject = ReplaySubject.create()
        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(host,
                    permissions.filter {
                        permission ->
                        !checkPermission(host, permission)
                    }.toTypedArray(), requestCode)
        } else {
            permissionSubject.onNext(Pair(requestCode, true))
            permissionSubject.onCompleted()
        }
        return permissionSubject
    }

    fun checkPermission (host : AppCompatActivity, permission : String) : Boolean =
        ContextCompat.checkSelfPermission(host, permission) == PackageManager.PERMISSION_GRANTED

}