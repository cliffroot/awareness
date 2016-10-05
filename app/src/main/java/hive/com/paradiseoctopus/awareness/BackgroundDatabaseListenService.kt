package hive.com.paradiseoctopus.awareness

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.support.v7.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import hive.com.paradiseoctopus.awareness.singleplace.ShowSinglePlaceView
import hive.com.paradiseoctopus.awareness.singleplace.SubscriptionModel
import hive.com.paradiseoctopus.awareness.utils.BaseChildEventListener
import hive.com.paradiseoctopus.awareness.utils.BaseValueEventListener
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by edanylenko on 10/4/16.
 */

class BackgroundDatabaseListenService(val name : String = "notificationService") : Service() {

    val PENDING_INTENT_CODE = 119
    val NOTIFICATION_CODE = 2001

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        subscribeToUpdates()
        return START_STICKY
    }

    private fun subscribeToUpdates() {

        val database : FirebaseDatabase = (applicationContext as App).firebaseDatabase
        val auth : FirebaseAuth = (applicationContext as App).firebaseAuth
        val ref = database.getReference("subscriptions").child(auth.currentUser?.uid) // TODO: referencing by non-constant string

        ref.addValueEventListener(object : BaseValueEventListener() {
            override fun onDataChange(p0: DataSnapshot?) {
                p0?.children?.map {
                    child -> child.ref.addChildEventListener(object : BaseChildEventListener() {
                        override fun onChildAdded(p0: DataSnapshot?, p1: String?) { // a new subscription added
                            val model = (p0?.getValue(SubscriptionModel::class.java) as SubscriptionModel)
                            loadUserImage(model.subscriberPhotoUrl).subscribe {
                                bitmap -> showNotification(model, bitmap)
                            }
                        }
                    })
                }
            }
        })
    }

    private fun showNotification (model : SubscriptionModel, bitmap : Bitmap) {
        val mBuilder =
                NotificationCompat.Builder(this@BackgroundDatabaseListenService)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_add_white)
                        .setContentTitle(this@BackgroundDatabaseListenService.resources.getString(R.string.subscription_request))
                        .setContentText(String.format(
                                this@BackgroundDatabaseListenService.resources.getString(R.string.user_subscribed_to_your_places),
                                model.subscriberName
                        ))
                        .setPriority(NotificationCompat.PRIORITY_HIGH).setOnlyAlertOnce(false)
                        .setContentIntent(generatePendingIntent(model.ownerUserId, model.placeId))
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_CODE, mBuilder.build())
    }

    private fun  generatePendingIntent(ownerId : String, placeId : String): PendingIntent? {
        val intent : Intent = Intent(this, ShowSinglePlaceView::class.java)
        intent.data = Uri.parse("${resources.getString(R.string.API_PATH)}$ownerId/$placeId")
        val pendingIntent : PendingIntent = PendingIntent.getActivity(
                this,
                PENDING_INTENT_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT)
        return pendingIntent
    }

    private fun loadUserImage (imageUrl : String) : Observable<Bitmap> {
        return Observable.create<Bitmap> {
            try {
                val theBitmap = Glide.with(this@BackgroundDatabaseListenService).load(imageUrl).asBitmap().into(128, 128).get()
                it.onNext(theBitmap)
                it.onCompleted()
            } catch (e : Exception) {
                //it.onNext() TODO :// show something
                it.onCompleted()
            }
        }.subscribeOn(Schedulers.computation())
    }

}
