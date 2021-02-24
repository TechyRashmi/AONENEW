package com.blucor.aoneenterprises

import Extra.AppController
import Extra.CustomLoader
import Helpers.C
import ModelClass.Model
import ModelClass.ProductModel
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.ProductAdapter.Companion.loader
import com.blucor.aoneenterprises.SizeHolder.Companion.isSelected
import com.blucor.aoneenterprises.SizeHolder.Companion.left_qty
import com.blucor.aoneenterprises.SizeHolder.Companion.price
import com.blucor.aoneenterprises.SizeHolder.Companion.size_id
import com.blucor.aoneenterprises.SizeHolder.Companion.status
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import gowardhanfragments.CartFragment
import kotlinx.android.synthetic.main.alertyesno.view.*
import kotlinx.android.synthetic.main.popup.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ProductAdapter : RecyclerView.Adapter<Holder> {
    var data = ArrayList<ProductModel>()
    lateinit var image: IntArray
    lateinit var array: Array<String>
    lateinit var ctx:Context

    companion object{
         lateinit var loader: CustomLoader
     }

    constructor(favList: ArrayList<ProductModel>, ctx: Context) {
        data = favList
        this.ctx=ctx
        //loader
        loader = CustomLoader(ctx, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
    }

    constructor(image: IntArray, array: Array<String>) {
        this.image = image
        this.array = array
    }

    constructor(ctx: Context)
    {
        this.ctx=ctx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.product_items, parent, false)
        )
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.tvProductName.text=data.get(position).prod_name
        val url=EndPoints.IMAGE_PATH+data.get(position).prod_image
        Glide.with(holder.itemView)  //2
                .load(url) //3
                .centerCrop() //4
                .placeholder(R.drawable.placeholder) //5
                .error(R.drawable.placeholder) //6
                .into(holder.image)

        holder.rlAddtocart.setOnClickListener {
            try {
                Api_getQty("" + data.get(position).prod_id, ctx, "" + data.get(position).prod_name, url, "" + AppController.prefHelper.get(C.userid))
            }
            catch (e: Exception)
            {
                Log.e("exxx", e.toString());
            }

        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}
class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var llMain: LinearLayout
    var rlAddtocart: RelativeLayout
    var image: ImageView
    var tvProductName: TextView


    init {
        image = itemView.findViewById(R.id.image)
        tvProductName = itemView.findViewById(R.id.tvProductName)
        llMain = itemView.findViewById(R.id.llMain)
        rlAddtocart = itemView.findViewById(R.id.rlAddtocart)
    }
}


fun showdialog(ctx: Context, size: String, name: String, url: String, product_id: String)
{
    val dialog = Dialog(ctx)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.popup)
   val jsonarray =JSONArray(size)

  var arraylist : ArrayList<Model>

    var model: Model
    arraylist=ArrayList()

    for (i in 0 until jsonarray.length())
    {
        var jsonobject : JSONObject =jsonarray.getJSONObject(i)
       model=Model()

        val used:Int =jsonobject.getString("used_qty").toInt()
        val total_qty:Int =jsonobject.getString("qty").toInt()


        if(used<total_qty)
        {
            model.size=jsonobject.getString("size")
            model.size_id=jsonobject.getString("size_id")
            model.qty=jsonobject.getString("qty")
            model.price=jsonobject.getString("price")
            model.remaining=jsonobject.getString("remaining_qty")
            arraylist.add(model)
        }

        Log.e("userd",""+arraylist.size)

        if(arraylist.size==0)
        {
            dialog.cancel()
            Toast.makeText(ctx, "Sold out", Toast.LENGTH_SHORT).show()
        }
        else
        {


            var ivClose: ImageView=dialog.findViewById(R.id.ivClose)
            var ivProductImage: ImageView=dialog.findViewById(R.id.ivProductImage)
            var tvMovetocart: TextView=dialog.findViewById(R.id.tvMovetocart)
            var tvProductName: TextView=dialog.findViewById(R.id.tvProductName)
            var tvPrice: TextView=dialog.findViewById(R.id.tvPrice)
            var etQty: EditText=dialog.findViewById(R.id.etQty)

            etQty.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


//                    if(isSelected==false)
//                    {
//                        Toast.makeText(ctx, "select size", Toast.LENGTH_SHORT).show()
//
//                    }
//                    else
//                    {
//
//                    }

                    if (count > 0) {
                        if (size_id.isEmpty()) {
                            Toast.makeText(ctx, "select size", Toast.LENGTH_SHORT).show()
                        } else {
                            var qty: Int = etQty.text.toString().toInt()
                         //   Log.e("print", ""+left_qty)
                            if (qty > left_qty) {
                                etQty.setError("Entered value should be less than left quantity")
                                etQty.requestFocus()
                                status = false
                            } else {
                                status = true
                            }
                        }
                    }
                }
            })

            tvProductName.text=name

            var recyclerView:RecyclerView=dialog.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = GridLayoutManager(ctx, 3)
            val obj_adapter = TimeSlotAdapter(ctx, recyclerView, arraylist)
            recyclerView.adapter=obj_adapter

            Glide.with(ctx)  //2
                .load(url) //3
                .centerCrop() //4
                .placeholder(R.drawable.placeholder) //5
                .error(R.drawable.placeholder) //6
                .into(ivProductImage)

            ivClose.setOnClickListener {
                dialog.cancel()
                size_id=""
                left_qty=0
            }
            tvMovetocart.setOnClickListener {
                if(size_id.isEmpty())
                {
                    Toast.makeText(ctx, "Select Size", Toast.LENGTH_SHORT).show()
                }
                else if(etQty.text.length==0)
                {
                    Toast.makeText(ctx, "Enter quantity", Toast.LENGTH_SHORT).show()
                }
                else
                {

                    if(status==true){

                        var  total_price : Double =etQty.text.toString().toDouble() * price.toDouble()
                        API_ADDtoCart("" + AppController.prefHelper.get(C.userid), ctx, product_id, etQty.text.toString(), price, "" + total_price, size_id, it,dialog)



                    }
                    else
                    {
                        Toast.makeText(ctx, "Entered value is greater than quantity left", Toast.LENGTH_SHORT).show()
                    }

                }






            }
            dialog.show()
        }

    }

}

//*Recyclerview Adapter*//
private class TimeSlotAdapter(var ctx: Context, var recyclerView: RecyclerView, var arraylist: ArrayList<Model>) : RecyclerView.Adapter<SizeHolder>() {
    private var last_position = 0
    private var present_position = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeHolder {
        return SizeHolder(LayoutInflater.from(parent.context).inflate(R.layout.size_items, parent, false))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SizeHolder, position: Int) {
        holder.tvTime.text=arraylist.get(position).size
        holder.tvLeftqty.text=arraylist.get(position).remaining + " left"
       // arraylist.get(position).remaining

        if (last_position != -1 && present_position == position) {
            holder.tvTime.setBackgroundResource(R.drawable.capsule)
            holder.tvTime.setTextColor(Color.WHITE)
            Log.i("TAG", "$last_position--lastPosition--$position--position--$present_position--presentPosition in if")
        } else {
            // holder.tvTime.setVisibility(View.GONE);
            holder.tvTime.setBackgroundResource(R.drawable.timecapsule)

            holder.tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.blue_900))
            Log.i("TAG", "$last_position--lastPosition--$position--position--$present_position--presentPosition in else")
        }

        holder.itemView.setOnClickListener { view ->
            present_position = recyclerView.getChildAdapterPosition(view)
            if (present_position != RecyclerView.NO_POSITION) {
                if (present_position != -1) {

                    holder.tvTime.setBackgroundResource(R.drawable.timecapsule)
                    holder.tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.blue_900))
                    notifyItemChanged(last_position)
                    holder.tvTime.setBackgroundResource(R.drawable.capsule)
                    holder.tvTime.setTextColor(Color.WHITE)


                    //Log.e("testtt",""+arraylist.get(position).size_id)
                    price=""+arraylist.get(position).price
                    size_id=""+arraylist.get(position).size_id
                    left_qty=arraylist.get(position).remaining!!.toInt()

                    notifyItemChanged(present_position)
                    System.err.println("$last_position -- myParent")
                    last_position = present_position
                    System.err.println("$last_position -- myParent2")
                }
            } else {



                holder.tvTime.setBackgroundResource(R.drawable.capsule)
                holder.tvTime.setTextColor(Color.WHITE)
                notifyItemChanged(present_position)

            }
        }
        //            Typeface typeface = ResourcesCompat.getFont(TimeSlot.this, R.font.nunitosemibold);
//            holder.tvCategoryName.setTypeface(typeface);
    }

    override fun getItemCount(): Int {
        return arraylist.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}

private class SizeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTime: TextView
    var tvLeftqty: TextView
    init {
        tvTime = itemView.findViewById(R.id.tvTime)
        tvLeftqty = itemView.findViewById(R.id.tvLeftqty)
    }

    companion object{
        public var price :String=""
        public var size_id :String=""
        public var left_qty:Int = 0

        var status:Boolean = false
        var isSelected:Boolean = false

    }

}

//Web services
fun API_ADDtoCart(user_id: String, ctx: Context, prod_id: String, qty: String, price: String, subtotal: String, sizeid: String, llmain: View,dialog: Dialog) {
    loader.show()
    //RequestQueue initialized
    var mRequestQueue = Volley.newRequestQueue(ctx)

    //String Request initialized
    var mStringRequest = object : StringRequest(
            Request.Method.POST,
            EndPoints.URL_ADDCART,
            Response.Listener { response ->

                loader.cancel()


                val obj = JSONObject(response)
                Log.e("response", "" + obj)
                if (obj.getString("message").equals("true")) {
                   // Toast.makeText(ctx, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                       dialog.cancel()
                    val mSnackbar = Snackbar.make((ctx as DashboardActivity).findViewById(R.id.layout), "Item added in list", Snackbar.LENGTH_INDEFINITE).setAction("View List", View.OnClickListener {

                        val myFragment: Fragment = CartFragment()
                        ctx.supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, myFragment).addToBackStack(null).commit()

                    }).setActionTextColor(Color.RED).show()

                    size_id=""
                    left_qty=0
                } else {
                    Toast.makeText(ctx, obj.getString("success_msg"), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                loader.cancel()
                Log.i("This is the error", "Error :" + error.toString())

            })
    {

        @Throws(AuthFailureError::class)
        override fun getParams(): Map<String, String>  {
            val params2 = HashMap<String, String>()
            params2.put("user_id", user_id)
            params2.put("prod_id", prod_id)
            params2.put("qty", qty)
            params2.put("price", price)
            params2.put("subtotal", subtotal)
            params2.put("size_id", sizeid)

            Log.e("printtt",""+params2)

            return params2
        }

    }
    mRequestQueue!!.add(mStringRequest!!)

}

fun Api_getQty(product_id: String, ctx: Context, name: String, url: String, executive_id: String) {


    loader.show()
    //RequestQueue initialized
    var mRequestQueue = Volley.newRequestQueue(ctx)

    //String Request initialized
    var mStringRequest = object : StringRequest(
            Request.Method.POST,
            EndPoints.URL_GETSIZE,
            Response.Listener { response ->


                Log.e("tgfggdfg",""+response)
                val jsonarray = JSONArray(response)
                loader.cancel()
                var model: ProductModel


                showdialog(ctx, "" + jsonarray, name, url, product_id)

//            if (obj.getString("Success").equals("true")) {
//
//                Log.e("ResponseSize",""+obj)
//
//                var jsonarray :jsonarray= obj.getJSONArray("Product")
//
////                for (i in 0 until jsonarray.length())
////                {
////                    model = ProductModel()
////                    var jsonobject :JSONObject=jsonarray.getJSONObject(i)
//////                            var size=jsonobject.getString("size")
////                    model.size_id = jsonobject.getString("size_id")
////                    model.size=jsonobject.getString("size")
////                    model.price=jsonobject.getString("price")
////                    model.qty=jsonobject.getString("qty")
////                    //  model.prod_size= size.replace("\\r\\n ","")
////                    sizearray.add(model)
////                }
//
//
//
//            } else {
//            }

            },
            Response.ErrorListener { error ->

                loader.cancel()
                Log.i("This is the error", "Error :" + error.toString())

            }) {

        @Throws(AuthFailureError::class)
        override fun getParams(): Map<String, String>  {
            val params2 = HashMap<String, String>()
            params2.put("product_id", product_id)
            params2.put("executive_id", executive_id)

            return params2
        }

    }
    mRequestQueue!!.add(mStringRequest!!)


}

