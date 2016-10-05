package hive.com.paradiseoctopus.awareness.showplaces

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import hive.com.paradiseoctopus.awareness.App
import hive.com.paradiseoctopus.awareness.MainActivity
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import hive.com.paradiseoctopus.awareness.singleplace.OWNER_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.PLACE_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.ShowSinglePlaceView
import hive.com.paradiseoctopus.awareness.singleplace.SubscriptionModel
import hive.com.paradiseoctopus.awareness.utils.BaseValueEventListener
import hive.com.paradiseoctopus.awareness.utils.SimpleDividerItemDecoration
import java.io.File

/**
 * Created by edanylenko on 10/5/16.
 */

class ShowPlacesView : ShowPlacesContracts.ShowPlaceView, Fragment() {

    val PRESENTER_TAG = "ShowPlacesPresenter"
    lateinit var presenter : ShowPlacesContracts.ShowPlacePresenter
    lateinit var recycler : RecyclerView

    override fun displayPlaces(ref: DatabaseReference) {
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)

        recycler.addItemDecoration(SimpleDividerItemDecoration(this))

        val database = (context.applicationContext as App).firebaseDatabase
        val mRef = database.getReference("places").child(FirebaseAuth.getInstance().currentUser?.uid)

        val mAdapter = object : FirebaseRecyclerAdapter<PlaceModel, PlaceViewHolder>
        (PlaceModel::class.java, R.layout.place_row, PlaceViewHolder::class.java, mRef) {
            override fun populateViewHolder(chatMessageViewHolder: PlaceViewHolder, place: PlaceModel, position: Int) {
                chatMessageViewHolder.setName(place.name)
                chatMessageViewHolder.setText("${place.code}")
                chatMessageViewHolder.setImage(context, place.pathToMap)
                chatMessageViewHolder.setupShareButton(context,
                        "${context.resources.getString(R.string.API_PATH)}${place.ownerId}/${place.id}")

                chatMessageViewHolder.setupSubscribers(context, place)

                chatMessageViewHolder.mView.setOnClickListener { view ->
                    startActivity(Intent(context, ShowSinglePlaceView::class.java).apply {
                        putExtras(Bundle().apply{putString(PLACE_UID_KEY, place.id); putString(OWNER_UID_KEY, place.ownerId)})
                    })
                }

            }
        }
        recycler.adapter = mAdapter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v : View? = inflater?.inflate(R.layout.single_place_fragment, container, false)
        recycler = v?.findViewById(R.id.created_places) as RecyclerView
        return v
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (activity.supportFragmentManager.findFragmentByTag(PRESENTER_TAG) == null) {
            presenter = ShowPlacesPresenter(this)
            activity.supportFragmentManager.beginTransaction()
                    .add(presenter as Fragment, PRESENTER_TAG)
                    .commitNow()
            presenter.start()
        } else {
            presenter = activity.supportFragmentManager.findFragmentByTag(PRESENTER_TAG) as ShowPlacesContracts.ShowPlacePresenter
            presenter.provideView(this)
            presenter.start()
        }
    }

    class PlaceViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setName(name: String) {
            val nameView = mView.findViewById(R.id.place_name) as TextView
            nameView.text = name
        }

        fun setText(text: String) {
            val locationView = mView.findViewById(R.id.place_code) as TextView
            locationView.text = text
        }

        fun setImage (context: Context, path: String) {
            val imageView = mView.findViewById(R.id.place_bitmap) as ImageView
            Glide.with(context).load(File(context.applicationInfo.dataDir, path)).into(imageView)
        }

        fun setupShareButton(context : Context, url : String) {
            val shareButton = mView.findViewById(R.id.share_button)

            shareButton.setOnClickListener {
                val shareBody = url
                val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                context.startActivity(Intent.createChooser(sharingIntent, "SHARE"))
            }
        }

        fun setupSubscribers (context : Context, place : PlaceModel) {
            val subsribersView = mView.findViewById(R.id.place_subscribers) as ImageView


            Log.e("Overlay", "~~> ${place.ownerId} , ${place.id}")
            (context.applicationContext as App).firebaseDatabase.getReference("subscriptions")
                    .child(place.ownerId).child(place.id)
                    .addListenerForSingleValueEvent(object : BaseValueEventListener() {
                        override fun onDataChange(p0: DataSnapshot?) {
                            Log.e("Overlay", "got $p0")
                            p0?.children?.map {
                                it.getValue(SubscriptionModel::class.java).let { value -> Glide.with(mView.context).load(value.subscriberPhotoUrl).into(subsribersView) }
                            }
                        }

                    })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}