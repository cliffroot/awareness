package hive.com.paradiseoctopus.awareness.createplace

import android.os.Parcel
import android.os.Parcelable


/**
 * Created by cliffroot on 14.09.16.
 */

data class PlaceModel(var latitude : Double? = 0.0, var longitude : Double? = 0.0, var timestamp : Long = 0,
                      var ownerId : String = "id", var intervalFrom : Long? = -1, var intervalTo : Long? = -1,
                      var device : String? = null, var code : String? = null,
                      var name : String = "", var id : String = "id") : Parcelable {
    constructor(source: Parcel):
        this(source.readDouble(), source.readDouble(), source.readLong(), source.readString(), source.readLong(),
                source.readLong(), source.readString(), source.readString(), source.readString(), source.readString())

    fun updateFrom (placeModel: PlaceModel) {
        this.latitude = placeModel.latitude
        this.longitude = placeModel.longitude
        this.timestamp = placeModel.timestamp
        this.ownerId = placeModel.ownerId
        this.intervalFrom = placeModel.intervalFrom
        this.intervalTo = placeModel.intervalTo
        this.device = placeModel.device
        this.code = placeModel.code
        this.name = placeModel.name
        this.id = placeModel.id
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeDouble(latitude!!)
        dest?.writeDouble(longitude!!)
        dest?.writeLong(timestamp)
        dest?.writeString(ownerId)
        dest?.writeLong(intervalFrom!!)
        dest?.writeLong(intervalTo!!)
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