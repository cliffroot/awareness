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
import com.google.firebase.database.*
import hive.com.paradiseoctopus.awareness.singleplace.ShowSinglePlaceView
import hive.com.paradiseoctopus.awareness.singleplace.SubscriptionModel
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by edanylenko on 10/4/16.
 */

class BackgroundDatabaseListenService(val name : String = "notificationService") : Service() {

    val PENDING_INTENT_CODE = 119

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

        val ref = database.getReference("subscriptions").child(auth.currentUser?.uid)

        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) { }

            override fun onDataChange(p0: DataSnapshot?) {
                p0?.children?.map {
                    child ->
                        child.ref.addChildEventListener(object : ChildEventListener {
                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) { }
                    override fun onChildChanged(p0: DataSnapshot?, p1: String?) { }
                    override fun onChildAdded(p0: DataSnapshot?, p1: String?) { // a new subscription added
                        val model = (p0?.getValue(SubscriptionModel::class.java) as SubscriptionModel)
                        loadUserImage(model.subscriberPhotoUrl).subscribe {
                            bitmap -> val mBuilder =
                                NotificationCompat.Builder(this@BackgroundDatabaseListenService)
                                        .setLargeIcon(bitmap)
                                        .setSmallIcon(R.drawable.ic_add_white)
                                        .setContentTitle("New subscription request!") // TODO: string resources
                                        .setContentText("User ${model.subscriberName}  wants to subscribe to one of your places")
                                        .setPriority(NotificationCompat.PRIORITY_HIGH).setOnlyAlertOnce(false)
                                        .setContentIntent(generatePendingIntent(model.ownerUserId, model.placeId))
                            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            mNotificationManager.notify(2001, mBuilder.build())
                        }
                    }
                    override fun onChildRemoved(p0: DataSnapshot?) { }
                    override fun onCancelled(p0: DatabaseError?) { }
                    private fun  generatePendingIntent(ownerId : String, placeId : String): PendingIntent? {
                        val intnt : Intent = Intent(this@BackgroundDatabaseListenService, ShowSinglePlaceView::class.java)
                        intnt.data = Uri.parse("https://awareness-281fa.firebaseapp.com/places/$ownerId/$placeId")
                        val pendingIntent : PendingIntent = PendingIntent.getActivity(
                                this@BackgroundDatabaseListenService,
                                PENDING_INTENT_CODE,
                                intnt, PendingIntent.FLAG_CANCEL_CURRENT)
                        return pendingIntent
                    }
                    })
                }
            }
        })
    }

    private fun loadUserImage (imageUrl : String) : Observable<Bitmap> {
        return Observable.create<Bitmap> {
            val theBitmap = Glide.with(this@BackgroundDatabaseListenService).load(imageUrl).asBitmap().into(128, 128).get()
            it.onNext(theBitmap)
            it.onCompleted()
        }.subscribeOn(Schedulers.computation())
    }

}
