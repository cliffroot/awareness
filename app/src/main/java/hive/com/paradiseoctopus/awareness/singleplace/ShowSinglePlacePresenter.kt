package hive.com.paradiseoctopus.awareness.singleplace

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.firebase.database.DataSnapshot
import hive.com.paradiseoctopus.awareness.App
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import hive.com.paradiseoctopus.awareness.utils.BaseValueEventListener
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
        val (ownerUid, placeUid) = parseDataFromIntent(intent)
        val ref = (context.applicationContext as App).firebaseDatabase.getReference("places").child(ownerUid).child(placeUid)
        ref.addListenerForSingleValueEvent(object : BaseValueEventListener() {
            override fun onDataChange(p0: DataSnapshot?) {
                place = p0?.getValue(PlaceModel::class.java) as PlaceModel
                canSubscribeSubject.onNext((activity.applicationContext as App).firebaseAuth.currentUser?.uid != place.ownerId)
                canSubscribeSubject.onCompleted()
                view?.showPlace(place)
            }
        })
    }

    private fun parseDataFromIntent (intent : Intent) : Pair<String, String> {
        if (intent.data != null) {
            val uri = intent.data
            if (uri.pathSegments.size > 2) {
                return uri.pathSegments[1] to uri.pathSegments[2]
            }
            else {
                return "" to "" // TODO: notify that URI is invalid
            }
        } else {
            return intent.getStringExtra(OWNER_UID_KEY) to intent.getStringExtra(PLACE_UID_KEY)
        }
    }


    override fun canSubscribe(): Observable<Boolean> {
        return canSubscribeSubject
    }

    override fun subscribe() {
        val database = (activity.applicationContext as App).firebaseDatabase
        val auth = (activity.applicationContext as App).firebaseAuth
        val subscription : SubscriptionModel = SubscriptionModel(
                subscriberUserId = auth.currentUser?.uid!!, // or else we shouldn't be logged in
                ownerUserId = place.ownerId, subscriberPhotoUrl = auth.currentUser?.photoUrl.toString(),
                subscriberName = auth.currentUser?.displayName!!, placeId = place.id)
        database.reference.child("subscriptions").child(place.ownerId).child(place.id).child(subscription.id).setValue( subscription )
    }

    override fun back() {

    }

}