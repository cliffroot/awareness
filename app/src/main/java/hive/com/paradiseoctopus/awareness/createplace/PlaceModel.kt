package hive.com.paradiseoctopus.awareness.createplace

import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateUtils


/**
 * Created by cliffroot on 14.09.16.
 */

data class PlaceModel(var latitude : Double? = null, var longitude : Double? = null, var timestamp : Long = 0,
                      var ownerId : String = "id",
                      var intervalFrom : Long = 0, var intervalTo : Long = 0,
                      var device : String? = null, var code : String? = null,
                      var name : String = "", var id : String = "id") : Parcelable {
    constructor(source: Parcel):
        this(source.readDouble(), source.readDouble(), source.readLong(), source.readString(), source.readLong(),
                source.readLong(), source.readString(), source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(latitude!!)
        dest?.writeDouble(longitude!!)
        dest?.writeLong(timestamp)
        dest?.writeString(ownerId)
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