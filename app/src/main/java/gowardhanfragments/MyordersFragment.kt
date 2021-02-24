package gowardhanfragments


import Extra.AppController
import Extra.CustomLoader

import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.thesimplycoder.simpledatepicker.DatePickerHelper
import kotlinx.android.synthetic.main.fragment_myorders.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MyordersFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var datePicker: DatePickerHelper
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0
    var myHour: Int = 0
    var myMinute: Int = 0




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

                Log.e("date",""+dayofMonth)
                Log.e("date1",""+month)
                Log.e("date2",""+year )
               // tvDate.text = "${dayStr}-${monthStr}-${year}"
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        datePicker = DatePickerHelper(activity!!)
        val view = inflater?.inflate(R.layout.fragment_myorders, container, false)
        fm = activity!!.supportFragmentManager

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        //initialise
        array = ArrayList()

        view.recycleOrder.layoutManager= GridLayoutManager(activity, 1)



        view.llFilter.setOnClickListener{

            showDatePickerDialog()



        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="My Orders"

        if(C.page.equals("1"))
        {
            //back press
            backpressToFragment(Shop_order(), view, fm)
        }
        else
        {
            //back press
            backpressToFragment(ShopScheduled(), view, fm)
        }


        if (activity!!.isConnectedToNetwork()) {

            loader.show()

            API_GET_ORDERS("" + C.order_id, activity!!,
                    view.recycleOrder)
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
                    LayoutInflater.from(parent.context).inflate(R.layout.order_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {

            val model:ProductModel=data.get(position)
            holder.tvProductName.text=data.get(position).prod_name
            val url=EndPoints.IMAGE_PATH+data.get(position).prod_image
            Glide.with(holder.itemView)  //2
                    .load(url) //3
                    .centerCrop() //4
                    .placeholder(R.drawable.placeholder) //5
                    .error(R.drawable.placeholder) //6
                    .into(holder.image)

            holder.tvOrderDate.text="Deliverd on " +data.get(position).order_date


            holder.itemView.setOnClickListener{
                replaceFragment(OrderDetailsFragment(), fm,model)
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

        init {
            image = itemView.findViewById(R.id.productImage)
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate)
        }
    }

    companion object {
        lateinit var array: ArrayList<ProductModel>
        private lateinit var loader: CustomLoader
        lateinit var fm: FragmentManager
        fun replaceFragment(fragment: Fragment, fm: FragmentManager,model:ProductModel) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()

            val fragmentGet: Fragment = fragment
            val bundle = Bundle()
            bundle.putParcelable("data", model)
            fragmentGet.arguments = bundle
            transaction.commit()
        }

        //web services
        fun API_GET_ORDERS(order_id: String, ctx: Context, recyclerView: RecyclerView) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_GETORDER,
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
                                model.prod_image = jsonobject.getString("prod_image")
                                model.order_total = "\u20b9 " +jsonobject.getString("order_total")

                                val strs = jsonobject.getString("order_date").split(" ")[0]
                                model.order_date = strs.toString()
                                model.m_order_id = jsonobject.getString("m_order_id")
                                model.prod_name = jsonobject.getString("prod_name")
                                model.fullname = jsonobject.getString("fullname")
                                model.address1 = "Address: "+jsonobject.getString("address1") + " " + jsonobject.getString("address1") +"," +jsonobject.getString("city")+ ","+jsonobject.getString("state")+ " "+jsonobject.getString("zipcode")
                                model.mobileno = "\u20b9" + jsonobject.getString("mobileno")
                                model.payment_method = jsonobject.getString("payment_method")
                                model.product_quantity = jsonobject.getString("product_quantity")
                                array.add(model)
                            }

                            Collections.reverse(array);

                            val obj_adapter = ProductAdapter(array, ctx!!)
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
                    params2.put("order_id", order_id)

                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyordersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}