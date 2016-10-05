package hive.com.paradiseoctopus.awareness.utils

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * Created by edanylenko on 10/5/16.
 */

open class BaseChildEventListener : ChildEventListener {

    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

    }

    override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

    }

    override fun onChildAdded(p0: DataSnapshot?, p1: String?) {

    }

    override fun onChildRemoved(p0: DataSnapshot?) {

    }

    override fun onCancelled(p0: DatabaseError?) {

    }

}