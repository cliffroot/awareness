package hive.com.paradiseoctopus.awareness.createplace

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by cliffroot on 14.09.16.
 */

data class PlaceModel(var latitude : Double? = null, var longitude : Double? = null, var timestamp : Long = 0,
                      var ownerId : String = "id", var pathToMap : String = "id",
                      var intervalFrom : Long = 0, var intervalTo : Long = 0,
                      var device : String? = null, var code : String? = null,
                      var name : String = "", var id : String = "id") : Parcelable {
    constructor(source: Parcel):
        this(source.readDouble(), source.readDouble(), source.readLong(), source.readString(), source.readString(), source.readLong(),
                source.readLong(), source.readString(), source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(if (latitude == null)  0.0 else latitude!!)
        dest?.writeDouble(if (longitude == null) 0.0 else longitude!!)
        dest?.writeLong(timestamp)
        dest?.writeString(ownerId)
        dest?.writeString(pathToMap)
        dest?.writeLong(intervalFrom)
        dest?.writeLong(intervalTo)
        dest?.writeString(device)
        dest?.writeString(code)
        dest?.writeString(name)
        dest?.writeString(id)

    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PlaceModel> = object : Parcelable.Creator<PlaceModel> {
            override fun createFromParcel(source: Parcel): PlaceModel = PlaceModel(source)
            override fun newArray(size: Int): Array<PlaceModel?> = arrayOfNulls(size)
        }
    }
}