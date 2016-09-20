package hive.com.paradiseoctopus.awareness.createplace


/**
 * Created by cliffroot on 14.09.16.
 */

data class PlaceModel(var latitude : Double? = 0.0, var longitude : Double? = 0.0, var timestamp : Long = 0,
                      var ownerId : String = "id", var intervalFrom : Long? = 0, var intervalTo : Long? = 0,
                      var name : String = "Default", var id : String = "id") {
}