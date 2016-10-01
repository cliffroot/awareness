package hive.com.paradiseoctopus.awareness.singleplace

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import hive.com.paradiseoctopus.awareness.App
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel

/**
 * Created by edanylenko on 9/29/16.
 */


class ShowSinglePlacePresenter(var view : SinglePlaceContracts.SinglePlaceView? = null) :
                                                    Fragment(), SinglePlaceContracts.SinglePlacePresenter {

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
                view?.showPlace(p0?.getValue(PlaceModel::class.java) as PlaceModel)
            }
        })
    }


    override fun canSubscribe(): Boolean {
        return false
    }

    override fun subscribe() {

    }

    override fun back() {

    }

}