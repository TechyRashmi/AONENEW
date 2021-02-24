package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.R.attr.x
import android.R.attr.y
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.avatarfirst.avatargenlib.AvatarConstants
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.bumptech.glide.Glide
import com.thesimplycoder.simpledatepicker.DatePickerHelper
import gowardhanfragments.ScheduledOrder.Companion.API_ORDER
import kotlinx.android.synthetic.main.fragment_myorders.*
import kotlinx.android.synthetic.main.fragment_myorders.view.*

import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ShopScheduled : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var search_text:String =""

    lateinit var datePicker: DatePickerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)


        }
    }
    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val m = cal.get(Calendar.MONTH)
        val y = cal.get(Calendar.YEAR)
        datePicker.showDialog(d, m, y, object : DatePickerHelper.Callback {
            override fun onDateSelected(dayofMonth: Int, month: Int, year: Int) {
                val dayStr = if (dayofMonth < 10) "0${dayofMonth}" else "${dayofMonth}"
                val mon = month + 1
                val monthStr = if (mon < 10) "0${mon}" else "${mon}"


                var month_selected: String = ""
                if(month<10)
                {
                    month_selected="0"+(month+1).toString()
                }
                else
                {
                    month_selected=(month+1).toString()
                }

                date=dayofMonth.toString() + "-"+ month_selected+"-" +year.toString()




                Log.e("date2",""+date )
                if (activity!!.isConnectedToNetwork()) {
                   loader.show()
                    API_GET_ORDERS("" + AppController.prefHelper.get(C.userid), activity!!, view!!.recycleOrder, date,search_text)
                } else {
                    Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
                }
                // tvDate.text = "${dayStr}-${monthStr}-${year}"
            }
        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view=inflater.inflate(R.layout.fragment_myorders, container, false)

        fm = activity!!.supportFragmentManager

        datePicker = DatePickerHelper(activity!!)

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        //initialise
        array = ArrayList()

        view.rlDate.setOnClickListener{
            showDatePickerDialog()
        }


        view.spSelect?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {

                if(position==1)
                {
                    view.rlDate.visibility=View.VISIBLE
                    view.llPhone.visibility=View.GONE
                    view.tvGetdetail.visibility=View.GONE
                    //show(view.llPhone,true)
                }
                else if(position==2)
                {
                    view.rlDate.visibility=View.GONE
                    view.llPhone.visibility=View.VISIBLE
                    view.tvGetdetail.visibility=View.VISIBLE


                }
                else
                {
                    view.rlDate.visibility=View.GONE
                    view.llPhone.visibility=View.GONE
                    view.tvGetdetail.visibility=View.GONE
                }
            }

        }

        view.tvGetdetail.setOnClickListener{
            search_text=etSearch.text.toString()
            if (search_text.equals(" "))
            {
                Toast.makeText(activity, "Enter location to search..", Toast.LENGTH_SHORT).show()
            }
            else
            {
                if (activity!!.isConnectedToNetwork()) {
                   loader.show()
                    API_GET_ORDERS("" + AppController.prefHelper.get(C.userid), activity!!, view.recycleOrder, "", search_text
                    )
                } else {
                    Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
                }
            }




        }

        view.recycleOrder.layoutManager= GridLayoutManager(activity, 1)


        view.llFilter.setOnClickListener{
            showDatePickerDialog()
        }


        //SetHeadertext
        DashboardActivity.tvHeaderText.text="All Orders"

        //back press
        Utils.backpressToFragment(DashboardFragment(), view,fm)

        if (activity!!.isConnectedToNetwork()) {
            loader.show()
            API_GET_ORDERS(
                "" + AppController.prefHelper.get(C.userid), activity!!, view.recycleOrder,
            "",search_text)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }

        // Inflate the layout for this fragment
        return view
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
                LayoutInflater.from(parent.context).inflate(R.layout.sheduled_shops_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {

            val model:ProductModel=data.get(position)
            holder.itemView.setOnClickListener{
                C.order_id=data.get(position).m_order_id.toString()
                C.page="2"
                replaceFragment(OrderDetailsFragment(), fm)
              //  replaceFragment(MyordersFragment(),fm)
            }


            holder.tvSubmit.setOnClickListener{
                if (ctx.isConnectedToNetwork()) {
                    loader.show()
                    API_ORDER("delivered", "" + data.get(position).m_order_id, ctx)
                } else {
                    Toast.makeText(ctx, "No Network Available", Toast.LENGTH_SHORT).show()
                }
            }


            holder.tvProductName.text=data.get(position).fullname
            holder.tvAdvancePayment.text="Advance: \u20b9" +data.get(position).advance_amt
            holder.tvPendingAmount.text="Pending: \u20b9"+data.get(position).pending_amt
            holder.tvOrderDate.text="Order generated on " +data.get(position).order_date

           // holder.tvOrder.text="ORDER ID: "+data.get(position).m_order_id

            holder.image.setImageDrawable(
                AvatarGenerator.avatarImage(
                    ctx,
                    200,
                    AvatarConstants.RECTANGLE,
                    data.get(position).fullname.toString()
                )
            )
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

    companion object {
        lateinit var array: ArrayList<ProductModel>
        lateinit var loader: CustomLoader
        lateinit var fm: FragmentManager

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
                        CheckoutFragment.successpopup(ctx)

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


        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
        //web services
        fun API_GET_ORDERS(user_id: String, ctx: Context, recyclerView: RecyclerView,selected_date:String,keywords:String) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETSHOPS,
                Response.Listener { response ->

                    loader.cancel()
                    array.clear()

                    val obj = JSONObject(response)
                    Log.e("responseORDERS", "" + obj)
                    if (obj.getString("Success").equals("true")) {
                        // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                        var model: ProductModel

                        var jsonarray: JSONArray = obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length()) {
                            model = ProductModel()
                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)

                            val strs = jsonobject.getString("order_date").split(" ")[0]
                            model.order_date = strs
                            model.m_order_id = jsonobject.getString("m_order_id")
                            model.fullname = jsonobject.getString("fullname")
                            model.advance_amt = jsonobject.getString("advanced_payment")
                            model.pending_amt = jsonobject.getString("remaining_payment")

                            array.add(model)
                        }

                        Collections.reverse(array);

                        val obj_adapter =
                            ProductAdapter(array, ctx!!)
                        recyclerView.setAdapter(obj_adapter)

                    } else {
                        Toast.makeText(ctx, "No orders", Toast.LENGTH_SHORT).show()
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
                    params2.put("order_status", "pending")
                    params2.put("date", selected_date)
                    params2.put("keywords", keywords)

                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Shop_order.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Shop_order().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}