package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.android.synthetic.main.fragment_checkout.view.*
import kotlinx.android.synthetic.main.fragment_my_customers.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


lateinit var loaderr:CustomLoader

class MyCustomers : Fragment() {
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
        val view=inflater.inflate(R.layout.fragment_my_customers, container, false)

        //initialise
        array = ArrayList()

        view.recyclerviewC.layoutManager= GridLayoutManager(activity, 1)

        //loader
        loaderr = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="My Customers"

       fm = activity!!.supportFragmentManager


        view.ivAddnew.setOnClickListener{
            C.page="3"
            replaceFragment(AddShopAddress(), fm)
        }

        //backpress

      backpressToFragment(DashboardFragment(), view, fm)

        if (activity!!.isConnectedToNetwork()) {
            loaderr.show()
            API_GET_CUSTOMERS("" + AppController.prefHelper.get(C.userid), activity!!, view.recyclerviewC)
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
                LayoutInflater.from(parent.context).inflate(R.layout.customer_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.tvShopName.text=data.get(position).fullname
            holder.tvAddress.text= data.get(position).address1
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var tvShopName: TextView
        var tvAddress: TextView

        init {

            tvShopName = itemView.findViewById(R.id.tvShopName)
            tvAddress = itemView.findViewById(R.id.tvAddress)
        }
    }

    companion object {
        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
        lateinit var fm: FragmentManager
        lateinit var array: ArrayList<ProductModel>
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MyCustomers().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


    //web services
    fun API_GET_CUSTOMERS(user_id: String, ctx: Context, recyclerView: RecyclerView) {

        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(ctx)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETCUSTOMER,
                Response.Listener { response ->

                    loaderr.cancel()

                    val obj = JSONObject(response)
                    Log.e("responseORDERS", "" + obj)
                    if (obj.getString("Success").equals("true")) {
                        // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                        var model: ProductModel

                        var jsonarray: JSONArray = obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length()) {
                            model = ProductModel()
                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)


                            model.fullname = jsonobject.getString("fullname")
                            model.address1 = "Address: "+jsonobject.getString("address1") + " " + jsonobject.getString("address1") +"," +jsonobject.getString("city")+ ","+jsonobject.getString("state")+ " "+jsonobject.getString("zipcode")
                            model.mobileno = "\u20b9" + jsonobject.getString("mobileno")
                            array.add(model)
                        }

                        val obj_adapter = ProductAdapter(array, ctx!!)
                        recyclerView.setAdapter(obj_adapter)

                    } else {
                        Toast.makeText(ctx, "Nothing in basket", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    loaderr.cancel()
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