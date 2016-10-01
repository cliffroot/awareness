package hive.com.paradiseoctopus.awareness

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceWithPagerView
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import hive.com.paradiseoctopus.awareness.singleplace.OWNER_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.PLACE_UID_KEY
import hive.com.paradiseoctopus.awareness.singleplace.ShowSinglePlaceView
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler : RecyclerView = findViewById(R.id.created_places) as RecyclerView

        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)

        val database = (applicationContext as App).firebaseDatabase
        val mRef = database.getReference("places").child(FirebaseAuth.getInstance().currentUser?.uid)

        mRef.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                Log.e("Child changed", "p0: $p0, $p1")
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot?) {

            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })

        val mAdapter = object : FirebaseRecyclerAdapter<PlaceModel, PlaceViewHolder>
                (PlaceModel::class.java, R.layout.place_row, PlaceViewHolder::class.java, mRef) {
                    override fun populateViewHolder(chatMessageViewHolder: PlaceViewHolder, place: PlaceModel, position: Int) {
                        chatMessageViewHolder.setName(place.name)
                        chatMessageViewHolder.setText("${place.latitude} ; ${place.longitude}")
                        chatMessageViewHolder.setImage(this@MainActivity, place.pathToMap)
                        chatMessageViewHolder.setupShareButton(this@MainActivity,
                                "https://awareness-281fa.firebaseapp.com/places/${place.ownerId}/${place.id}")

                        chatMessageViewHolder.mView.setOnClickListener { view ->
                            startActivity({
                               val i : Intent = Intent(this@MainActivity, ShowSinglePlaceView::class.java)
                               val b : Bundle = Bundle()
                               b.putString(PLACE_UID_KEY, place.id)
                               b.putSerializable(OWNER_UID_KEY, place.ownerId)
                               i.putExtras(b)
                               i
                            }())
                        }

            }
        }
        recycler.adapter = mAdapter

        findViewById(R.id.add_place).setOnClickListener { startActivity(Intent(this, CreatePlaceWithPagerView::class.java)) }
    }

    class PlaceViewHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setName(name: String) {
            val nameView = mView.findViewById(R.id.place_name) as TextView
            nameView.text = name
        }

        fun setText(text: String) {
            val locationView = mView.findViewById(R.id.place_location) as TextView
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
