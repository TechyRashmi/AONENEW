package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.bumptech.glide.Glide
import gowardhanfragments.CheckoutFragment.Companion.successpopup
import kotlinx.android.synthetic.main.fragment_my_customers.view.*
import kotlinx.android.synthetic.main.fragment_myorders.view.*
import kotlinx.android.synthetic.main.fragment_myorders.view.recycleOrder
import kotlinx.android.synthetic.main.fragment_scheduled_order.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ScheduledOrder : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
         val view=inflater.inflate(R.layout.fragment_scheduled_order, container, false)

        //loader



        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)


        view.recycleScheduleOrder.layoutManager= GridLayoutManager(activity, 1)

        //initialise
        array = ArrayList()

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Credit order"

        if (activity!!.isConnectedToNetwork()) {
            loader.show()

            API_GET_ORDERS("" + AppController.prefHelper.get(C.userid), activity!!,
                    view.recycleScheduleOrder)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }


        return view
    }

    companion object {

        lateinit var array: ArrayList<ProductModel>
        lateinit var loader: CustomLoader
        //web services
        fun API_GET_ORDERS(user_id: String, ctx: Context, recyclerView: RecyclerView) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_GETSHEDULEDORDER,
                    Response.Listener { response ->

                        loader.cancel()
                    // array.clear()

                        val obj = JSONObject(response)
                        Log.e("responseORDERS", "" + obj)
                        if (obj.getString("Success").equals("true")) {
                            // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                            var model: ProductModel

                            var jsonarray: JSONArray = obj.getJSONArray("Product")

                            for (i in 0 until jsonarray.length()) {
                                model = ProductModel()
                                var jsonobject: JSONObject = jsonarray.getJSONObject(i)
                                model.prod_image = jsonobject.getString("prod_image")
                                model.order_total = "\u20b9 " +jsonobject.getString("order_total")

                                val strs = jsonobject.getString("order_date").split(" ")[0]
                                model.order_date = strs.toString()
                                model.m_order_id = jsonobject.getString("m_order_id")
                                model.size = jsonobject.getString("size")
                                model.prod_name = jsonobject.getString("prod_name")
                                model.fullname = jsonobject.getString("fullname")
                                model.address1 = "Address: "+jsonobject.getString("address1") + " " + jsonobject.getString("address1") +"," +jsonobject.getString("city")+ ","+jsonobject.getString("state")+ " "+jsonobject.getString("zipcode")
                                model.mobileno = "\u20b9" + jsonobject.getString("mobileno")
                                model.payment_method = jsonobject.getString("payment_method")
                                model.advance_amt = jsonobject.getString("advanced_payment")
                                model.pending_amt = jsonobject.getString("remaining_payment")
                                model.product_quantity = jsonobject.getString("product_quantity")
                               array.add(model)
                            }

                            Collections.reverse(array);
                            val obj_adapter = ProductAdapter(array, ctx!!)
                            recyclerView.setAdapter(obj_adapter)

                        } else {
                            Toast.makeText(ctx, "No Scheduled order", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener { error ->

                        if (loader.isShowing) {
                            loader.cancel()
                        }
                        Log.i("This is the error", "Error :" + error.toString())

                    })
            {

                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>  {
                    val params2 = HashMap<String, String>()
                    params2.put("executive_id", user_id)

                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }

        fun API_ORDER(order_status: String, order_id: String, ctx: Context) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_SCHEDULECHECKOUT,
                Response.Listener { response ->

                    val obj = JSONObject(response)
                    Log.e("responseORDER", "" + obj)
                    if (obj.getString("success").equals("true")) {
                        successpopup(ctx)

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

                    params2.put("order_status", order_status)
                    params2.put("order_id", order_id)

                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }


        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ScheduledOrder().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    fun successpopup(ctx: Context) {
        val dialog = Dialog(ctx)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert)

        dialog.show()
        val timerThread: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(2600)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    dialog.dismiss()
                    val intent =
                        Intent(ctx, DashboardActivity::class.java)
                    ctx.startActivity(intent)

                }
            }
        }

        timerThread.start()
    }


    class ProductAdapter : RecyclerView.Adapter<Holder> {
        var data = ArrayList<ProductModel>()
        lateinit var image: IntArray
        lateinit var array: Array<String>
        lateinit var ctx: Context


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
                    LayoutInflater.from(parent.context).inflate(R.layout.schedules_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {


            val model: ProductModel =data.get(position)
            holder.tvProductName.text=data.get(position).prod_name + " (" + data.get(position).size +")"
            val url= EndPoints.IMAGE_PATH+data.get(position).prod_image
            Glide.with(holder.itemView)  //2
                    .load(url) //3
                    .centerCrop() //4
                    .placeholder(R.drawable.placeholder) //5
                    .error(R.drawable.placeholder) //6
                    .into(holder.image)

            holder.tvOrderDate.text="Order generated on " +data.get(position).order_date

            holder.tvAdvancePayment.text="Advance \u20b9 "+data.get(position).advance_amt

            holder.tvPendingAmount.text="Pending \u20b9 " +data.get(position).pending_amt

            holder.tvSubmit.setOnClickListener{

                if (ctx.isConnectedToNetwork()) {
                    loader.show()
                    API_ORDER("delivered",""+data.get(position).m_order_id,ctx)
                } else {
                    Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
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
        var tvOrderDate: TextView
        var tvAdvancePayment: TextView
        var tvPendingAmount: TextView
        var tvSubmit: TextView

        init {
            image = itemView.findViewById(R.id.ivProductImage)
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate)
            tvAdvancePayment = itemView.findViewById(R.id.tvAdvancePayment)
            tvPendingAmount = itemView.findViewById(R.id.tvPendingAmount)
            tvSubmit = itemView.findViewById(R.id.tvSubmit)
        }
    }
}