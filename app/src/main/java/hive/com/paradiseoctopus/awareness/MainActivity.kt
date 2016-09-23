package hive.com.paradiseoctopus.awareness

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceView
import hive.com.paradiseoctopus.awareness.utils.PermissionUtility

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, CreatePlaceView::class.java))

        val recycler : RecyclerView = findViewById(R.id.created_places) as RecyclerView

        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)

       // val database = FirebaseDatabase.getInstance()
       // val mRef = database.getReference("places")

//        val mAdapter =
//                object : FirebaseRecyclerAdapter<PlaceModel, ChatHolder>
//                (PlaceModel::class.java, android.R.layout.two_line_list_item, ChatHolder::class.java, mRef) {
//
//                    override fun populateViewHolder(chatMessageViewHolder: ChatHolder, chatMessage: PlaceModel, position: Int) {
//                chatMessageViewHolder.setName(chatMessage.name)
//                chatMessageViewHolder.setText("${chatMessage.latitude} ; ${chatMessage.longitude}")
//            }
//        }
//        recycler.setAdapter(mAdapter)
    }

    class ChatHolder(internal var mView: View) : RecyclerView.ViewHolder(mView) {

        fun setName(name: String) {
            val field = mView.findViewById(android.R.id.text1) as TextView
            field.setText(name)
        }

        fun setText(text: String) {
            val field = mView.findViewById(android.R.id.text2) as TextView
            field.setText(text)
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
