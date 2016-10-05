package hive.com.paradiseoctopus.awareness.singleplace

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by edanylenko on 10/3/16.
 */

data class SubscriptionModel(var ownerUserId : String= "", var subscriberUserId : String = "",
                             var subscriberPhotoUrl : String = "", var active : Boolean = false,
                             var subscriberName : String = "", var id : String = UUID.randomUUID().toString(),
                             var placeId : String = "") : Parcelable {

    constructor(source: Parcel):
        this(source.readString(), source.readString(), source.readString(), if (source.readInt() == 1) true else false, source.readString(), source.readString(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(ownerUserId)
        dest?.writeString(subscriberUserId)
        dest?.writeString(subscriberPhotoUrl)
        dest?.writeInt(if (active) 1 else 0)
        dest?.writeString(subscriberName)
        dest?.writeString(id)
        dest?.writeString(placeId)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<SubscriptionModel> = object : Parcelable.Creator<SubscriptionModel> {
            override fun createFromParcel(source: Parcel): SubscriptionModel = SubscriptionModel(source)
            override fun newArray(size: Int): Array<SubscriptionModel?> = arrayOfNulls(size)
        }
    }
}