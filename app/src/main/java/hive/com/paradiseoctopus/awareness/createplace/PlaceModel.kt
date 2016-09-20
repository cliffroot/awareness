package hive.com.paradiseoctopus.awareness.createplace

import java.util.*

/**
 * Created by cliffroot on 14.09.16.
 */

data class PlaceModel(val latitude : Float?, val longitude : Float?, val timestamp : Long,
                      val ownerId : String, val intervalFrom : Long?, val intervalTo : Long?,
                      val name : String, val id : String) {
    constructor(): this(0f, 0f, 0, "id", 0, 0, "Default", "id")
}