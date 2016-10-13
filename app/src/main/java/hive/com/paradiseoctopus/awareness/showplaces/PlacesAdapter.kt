package hive.com.paradiseoctopus.awareness.showplaces

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import hive.com.paradiseoctopus.awareness.App
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import hive.com.paradiseoctopus.awareness.singleplace.OWNER_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.PLACE_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.ShowSinglePlaceView
import hive.com.paradiseoctopus.awareness.singleplace.SubscriptionModel
import hive.com.paradiseoctopus.awareness.utils.BaseValueEventListener
import java.io.File

/**
 * Created by edanylenko on 10/7/16.
 */


class PlacesAdapter( clazzModel : Class<PlaceModel>, layoutId : Int,
                     clazzHolder : Class<PlaceViewHolder>, ref : DatabaseReference, val context: Context)
    : FirebaseRecyclerAdapter<PlaceModel, PlacesAdapter.PlaceViewHolder>(clazzModel, layoutId, clazzHolder, ref) {


    override fun populateViewHolder(viewHolder: PlaceViewHolder, place: PlaceModel, position: Int) {
        viewHolder.setName(place.name)
        viewHolder.setText("${place.code}")
        viewHolder.setImage(context, place.pathToMap)
        viewHolder.setupShareButton(context,  "${context.resources.getString(R.string.API_PATH)}${place.ownerId}/${place.id}")

        viewHolder.setupSubscribers(context, place)

        viewHolder.mView.setOnClickListener {
            context.startActivity(Intent(context, ShowSinglePlaceView::class.java).apply {
                putExtras(Bundle().apply{putString(PLACE_UID_KEY, place.id); putString(OWNER_UID_KEY, place.ownerId)})
            })
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
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                context.startActivity(Intent.createChooser(sharingIntent, "SHARE"))
            }
        }

        fun setupSubscribers (context : Context, place : PlaceModel) {
            val subscribersView = mView.findViewById(R.id.place_subscribers) as ImageView
            (context.applicationContext as App).firebaseDatabase.getReference("subscriptions")
                .child(place.ownerId).child(place.id)
                .addListenerForSingleValueEvent(object : BaseValueEventListener() {
                    override fun onDataChange(p0: DataSnapshot?) {
                        p0?.children?.map {
                            it.getValue(SubscriptionModel::class.java).let {
                                value -> Glide.with(mView.context).load(value.subscriberPhotoUrl).into(subscribersView)
                            }
                        }
                    }

                })
        }
    }
}