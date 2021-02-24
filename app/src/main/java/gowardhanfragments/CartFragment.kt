package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader

import Extra.Utils
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.API_ADDtoCart
import com.blucor.aoneenterprises.DashboardActivity


import com.blucor.aoneenterprises.R
import com.bumptech.glide.Glide

import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.fragment_cart.view.*
import kotlinx.android.synthetic.main.fragment_checkout.view.*
import kotlinx.android.synthetic.main.fragment_return.view.*
import kotlinx.coroutines.withContext
import network.EndPoints
import org.jetbrains.anko.find
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

lateinit var vw :View

class CartFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

         vw = inflater?.inflate(R.layout.fragment_cart, container, false)
        //initialise
        array= ArrayList()
        //SetHeadertext
        DashboardActivity.tvHeaderText.text="List"
        fm = activity!!.supportFragmentManager
        backpressToFragment(DashboardFragment(), vw, fm)
        vw.recycleCart.layoutManager= GridLayoutManager(activity, 1)
         vw.tvProceed.setOnClickListener{
             replaceFragment(CustomerDetails(), fm)
         }

        //loader

        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        //call cart api

        if (activity!!.isConnectedToNetwork()) {
            loader.show()
            API_GETCART("" + AppController.prefHelper.get(C.userid),activity!!)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }

        // Inflate the layout for this fragment
        return vw
    }

    companion object {

        fun Context.vibrate(milliseconds:Long = 500){
            val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            // Check whether device/hardware has a vibrator
            val canVibrate:Boolean = vibrator.hasVibrator()

            if(canVibrate){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    // void vibrate (VibrationEffect vibe)
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    milliseconds,
                                    // The default vibration strength of the device.
                                    VibrationEffect.DEFAULT_AMPLITUDE
                            )
                    )
                }else{
                    // This method was deprecated in API level 26
                    vibrator.vibrate(milliseconds)
                }
            }
        }


        // Extension property to check whether device has Vibrator
        val Context.hasVibrator:Boolean
            get() {
                val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                return vibrator.hasVibrator()
            }
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CartFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        lateinit var array: ArrayList<ProductModel>

        lateinit var fm: FragmentManager


        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }

        fun API_UPDATECART(user_id: String,size_id: String,qty :String,ctx:Context) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_UPDATECART,
                    Response.Listener { response ->

                        val obj = JSONObject(response)
                        Log.e("responseUpdate", "" + obj)
                       if (obj.getString("success").equals("1")) {
                             Toast.makeText(ctx, "Successfully updated", Toast.LENGTH_SHORT).show()
                           if (ctx!!.isConnectedToNetwork()) {

                               API_GETCART("" + AppController.prefHelper.get(C.userid),ctx)
                           } else {
                               Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                           }


                        } else {
                            Toast.makeText(ctx, "error occured", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->

                        Log.i("This is the error", "Error :" + error.toString())

                    })
            {

                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>  {
                    val params2 = HashMap<String, String>()
                    params2.put("user_id", user_id)
                    params2.put("qty", qty)
                    params2.put("size_id", size_id)


                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }

        fun API_DELETECART(user_id: String,size_id: String,ctx:Context) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_DELETECART,
                    Response.Listener { response ->

                        val obj = JSONObject(response)
                        Log.e("responseUpdate", "" + obj)
                        if (obj.getString("success").equals("1")) {
                            Toast.makeText(ctx, "Removed from list", Toast.LENGTH_SHORT).show()

                            if (ctx!!.isConnectedToNetwork()) {
                                API_GETCART("" + AppController.prefHelper.get(C.userid),ctx)
                            } else {
                                Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                            }



                        } else {
                            Toast.makeText(ctx, "error occured", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->
                        Log.i("This is the error", "Error :" + error.toString())

                    })
            {

                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>  {
                    val params2 = HashMap<String, String>()
                    params2.put("user_id", user_id)
                    params2.put("size_id", size_id)


                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }
        //web services
        fun API_GETCART(user_id: String,ctx:Context) {
            // loader.show()
            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_GETCART,
                    Response.Listener { response ->
                        if(loader.isShowing)
                        {
                            loader.cancel()
                        }
                        array.clear()
                        vw.tvFinalprice.text=""

                        val obj = JSONObject(response)
                        Log.e("responseCART", "" + obj)
                        if (obj.getString("Success").equals("true")) {
                            // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                            var model: ProductModel
                            vw.llProceed.visibility=View.VISIBLE
                            var jsonarray: JSONArray = obj.getJSONArray("Product")

                            var final_amt:Double=0.0

                            for (i in 0 until jsonarray.length()) {
                                model = ProductModel()
                                var jsonobject: JSONObject = jsonarray.getJSONObject(i)
//                            var size=jsonobject.getString("size")
                                model.prod_id = jsonobject.getString("prod_id")
                                model.prod_image = jsonobject.getString("prod_image")
                                model.prod_name = jsonobject.getString("prod_name")
                                model.size = jsonobject.getString("size")
                                model.qty = jsonobject.getString("qty")
                                model.price = "\u20b9" +jsonobject.getString("price")
                                model.size_id = jsonobject.getString("size_id")
                                model.remaining_qty = jsonobject.getString("remaining_qty")

                                val qty:Double=jsonobject.getString("qty").toDouble()
                                final_amt=final_amt+jsonobject.getString("price").toDouble()*qty

                                //  model.prod_size= size.replace("\\r\\n ","")
                                array.add(model)
                            }


                            val obj_adapter = ProductAdapter(array, ctx!!)
                            vw.recycleCart.setAdapter(obj_adapter)
                            vw.tvFinalprice.text="\u20b9 "+final_amt

                        } else {
                            vw.llProceed.visibility=View.GONE
                            Toast.makeText(ctx, "Nothing in basket", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->

                        if(loader.isShowing)
                        {
                            loader.cancel()
                        }
                        Log.i("This is the error", "Error :" + error.toString())

                    })
            {

                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>  {
                    val params2 = HashMap<String, String>()
                    params2.put("user_id", user_id)


                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }
    }


    class ProductAdapter : RecyclerView.Adapter<Holder> {
        var data = ArrayList<ProductModel>()
        lateinit var image: IntArray
        lateinit var array: Array<String>
        lateinit var ctx:Context


        constructor(favList: ArrayList<ProductModel>, ctx: Context) {
            data = favList
            this.ctx=ctx

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
                    LayoutInflater.from(parent.context).inflate(R.layout.cart_items, parent, false)
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

            holder.tvSize.text=data.get(position).size

            holder.cart_item_number.text=data.get(position).qty

            holder.price.text=data.get(position).price

            holder.cart_quant_add.setOnClickListener{

                var count = holder.cart_item_number.text.toString().toInt()
                count = count + 1
                if(count > data.get(position).remaining_qty?.toInt()!!)
                {
                    Toast.makeText(ctx, "You can't add more than"+ data.get(position).remaining_qty +" ", Toast.LENGTH_SHORT).show()

                }
                else
                {
                    holder.cart_item_number.text = count.toString()

                    if (ctx!!.isConnectedToNetwork()) {
                        API_UPDATECART(""+AppController.prefHelper.get(C.userid),""+data.get(position).size_id,holder.cart_item_number.text.toString() ,ctx)
                        ctx.vibrate()
                    } else {
                        Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                    }
                }

            }

            holder.cart_quant_minus.setOnClickListener {
                var count = holder.cart_item_number.text.toString().toInt()
                if (count > 1) {
                    count = count - 1
                    holder.cart_item_number.text = count.toString()

                    if (ctx!!.isConnectedToNetwork()) {
                        API_UPDATECART(""+AppController.prefHelper.get(C.userid),""+data.get(position).size_id,holder.cart_item_number.text.toString() ,ctx)
                        ctx.vibrate()
                    } else {
                        Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                    }
                } else {

                    //call remove from cart api here
                    if (ctx!!.isConnectedToNetwork()) {
                        API_DELETECART(""+AppController.prefHelper.get(C.userid),""+data.get(position).size_id ,ctx)
                    } else {
                        Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                    }
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


        var image: ImageView
        var tvProductName: TextView
        var tvSize: TextView
        var price: TextView
        var cart_item_number: TextView
        var cart_quant_minus: ImageButton
        var cart_quant_add: ImageButton


        init {
            image = itemView.findViewById(R.id.cart_item_image)
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvSize = itemView.findViewById(R.id.tvSize)
            price = itemView.findViewById(R.id.price)


            cart_quant_minus = itemView.findViewById(R.id.cart_quant_minus)
            cart_quant_add = itemView.findViewById(R.id.cart_quant_add)
            cart_item_number = itemView.findViewById(R.id.cart_item_number)
        }
    }
    fun vibratePhone() {
        val vibrator = context?.getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
    override fun onStart() {
        super.onStart()

    }





}



