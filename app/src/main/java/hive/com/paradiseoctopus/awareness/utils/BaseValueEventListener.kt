package hive.com.paradiseoctopus.awareness.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * Created by edanylenko on 10/5/16.
 */


open class BaseValueEventListener() : ValueEventListener {

    override fun onCancelled(p0: DatabaseError?) {

    }

    override fun onDataChange(p0: DataSnapshot?) {

    }

}