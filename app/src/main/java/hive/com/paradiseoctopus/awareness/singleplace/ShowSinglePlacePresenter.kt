package hive.com.paradiseoctopus.awareness.singleplace

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hive.com.paradiseoctopus.awareness.App
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import rx.Observable
import rx.subjects.ReplaySubject

/**
 * Created by edanylenko on 9/29/16.
 */


class ShowSinglePlacePresenter(var view : SinglePlaceContracts.SinglePlaceView? = null) :
                                                    Fragment(), SinglePlaceContracts.SinglePlacePresenter {

    lateinit var place : PlaceModel
    val canSubscribeSubject : ReplaySubject<Boolean> = ReplaySubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun provideView(singlePlaceView: SinglePlaceContracts.SinglePlaceView) {
        view = singlePlaceView
    }

    override fun start(intent : Intent) {

        var ownerUid : String = ""
        var placeUid : String = ""
        if (intent.data != null) {
            if (intent.data.pathSegments.size > 2) {
                ownerUid = intent.data.pathSegments[1]
                placeUid = intent.data.pathSegments[2]
            }
        } else {
            ownerUid = intent.getStringExtra(OWNER_UID_KEY)
            placeUid = intent.getStringExtra(PLACE_UID_KEY)
        }

        val myRef = (context.applicationContext as App).firebaseDatabase.getReference("places").child(ownerUid).child(placeUid)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) { }

            override fun onDataChange(p0: DataSnapshot?) {
                place = p0?.getValue(PlaceModel::class.java) as PlaceModel
                canSubscribeSubject.onNext((activity.applicationContext as App).firebaseAuth.currentUser?.uid != place.ownerId)
                canSubscribeSubject.onCompleted()
                view?.showPlace(place)
            }
        })
    }


    override fun canSubscribe(): Observable<Boolean> {
        return canSubscribeSubject
    }

    override fun subscribe() {
        Log.e("Overlay", "subscribe")
        val database = (activity.applicationContext as App).firebaseDatabase
        val auth = (activity.applicationContext as App).firebaseAuth
        val subscription : SubscriptionModel = SubscriptionModel(subscriberUserId = auth.currentUser?.uid!!, // or else we shouldn't be logged in
                ownerUserId = place.ownerId, subscriberPhotoUrl = auth.currentUser?.photoUrl.toString(),
                subscriberName = auth.currentUser?.displayName!!, placeId = place.id)
        database.reference.child("subscriptions").child(place.ownerId).child(place.id).child(subscription.id).setValue( subscription )
    }

    override fun back() {

    }

}