package ModelClass

import android.os.Parcel
import android.os.Parcelable

class ProductModel() :Parcelable {

    var prod_id: String? = null
    var prod_name: String? = null
    var prod_image: String? = null
    var order_date: String? = null
    var m_order_id: String? = null
    var order_total: String? = null
    var fullname: String? = null
    var address1: String? = null
    var mobileno: String? = null
    var payment_method: String? = null
    var product_quantity: String? = null
    var advance_amt: String? = null
    var pending_amt: String? = null
    var used_qty: String? = null
    var remaining_qty: String? = null




    var size_id:String?=null
    var size:String?=null
    var price:String?=null
    var qty:String?=null

    constructor(parcel: Parcel) : this() {
        prod_id = parcel.readString()
        prod_name = parcel.readString()
        prod_image = parcel.readString()
        order_date = parcel.readString()
        m_order_id = parcel.readString()
        order_total = parcel.readString()
        fullname = parcel.readString()
        address1 = parcel.readString()
        mobileno = parcel.readString()
        payment_method = parcel.readString()
        size_id = parcel.readString()
        size = parcel.readString()
        price = parcel.readString()
        qty = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(prod_id)
        parcel.writeString(prod_name)
        parcel.writeString(prod_image)
        parcel.writeString(order_date)
        parcel.writeString(m_order_id)
        parcel.writeString(order_total)
        parcel.writeString(fullname)
        parcel.writeString(address1)
        parcel.writeString(mobileno)
        parcel.writeString(payment_method)
        parcel.writeString(size_id)
        parcel.writeString(size)
        parcel.writeString(price)
        parcel.writeString(qty)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<ProductModel> {
        override fun createFromParcel(parcel: Parcel): ProductModel {
            return ProductModel(parcel)
        }
        override fun newArray(size: Int): Array<ProductModel?> {
            return arrayOfNulls(size)
        }
    }

}