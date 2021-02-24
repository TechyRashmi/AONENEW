package gowardhanfragments

import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_myorders.view.*
import kotlinx.android.synthetic.main.fragment_order_details.*
import kotlinx.android.synthetic.main.fragment_order_details.view.*
import kotlinx.android.synthetic.main.order_item.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class OrderDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var model: ProductModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment

        vww = inflater?.inflate(R.layout.fragment_order_details, container, false)
        fm = activity!!.supportFragmentManager

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Order Details"

        if(C.page.equals("1"))
        {
            //back press
          backpressToFragment(Shop_order(), vww, fm)
        }
        else
        {
            //back press
            backpressToFragment(ShopScheduled(), vww, fm)
        }

        array = ArrayList()
        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)



        vww.ivShare.setOnClickListener{


            if (activity!!.isConnectedToNetwork()) {
                loader.show()
                API_PDF("" + C.order_id, activity!!)
            } else {
                Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
            }


        }

        if (activity!!.isConnectedToNetwork()) {
           loader.show()
           API_GET_ORDERS("" + C.order_id, activity!!)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }
        //back press
       // Utils.backpressToFragment(MyordersFragment(), vww, fm)
        return vww
    }

    companion object {
        lateinit var fm: FragmentManager
        lateinit var array: ArrayList<ProductModel>
        lateinit var vww :View
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                OrderDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    //web services
    fun API_PDF(order_id: String, ctx: Context) {
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(ctx)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETPDF,
                Response.Listener { response ->
                    loader.cancel()
                    val obj = JSONObject(response)
                    Log.e("responseORDERS1", "" + obj)
                    if (obj.getString("success").equals("success")) {

                        val pdf_url=obj.getString("pdf")
                        val whatsappIntent = Intent(Intent.ACTION_SEND)
                        whatsappIntent.type = "text/plain"
                        whatsappIntent.setPackage("com.whatsapp")
                        whatsappIntent.putExtra(
                                Intent.EXTRA_TEXT,
                                pdf_url
                        )
                        try {
                            activity!!.startActivity(whatsappIntent)
                        } catch (ex: ActivityNotFoundException) {
                            Toast.makeText(activity, "Whatsapp have not been installed.\"", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(ctx, "Problem in genrating pdf", Toast.LENGTH_SHORT).show()
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

    //web services
    fun API_GET_ORDERS(order_id: String, ctx: Context) {
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(ctx)

        //String Request initialized
        var mStringRequest = object : StringRequest(
            Request.Method.POST,
            EndPoints.URL_GETORDER,
            Response.Listener { response ->

                loader.cancel()

                val obj = JSONObject(response)
                Log.e("responseORDERS1", "" + obj)
                if (obj.getString("Success").equals("true")) {
                    // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                    var model: ProductModel

                    var jsonarray: JSONArray = obj.getJSONArray("Product")

                    for (i in 0 until jsonarray.length()) {
                        model = ProductModel()
                        var jsonobject: JSONObject = jsonarray.getJSONObject(i)

                        model.prod_image = jsonobject.getString("prod_image")
                        model.order_total = "\u20b9 " + jsonobject.getString("order_total")
                        val strs = jsonobject.getString("order_date").split(" ")[0]
                        model.order_date = strs
                        model.size =jsonobject.getString("size")
                        model.m_order_id = jsonobject.getString("m_order_id")
                        model.prod_name = jsonobject.getString("prod_name")
                        model.fullname = jsonobject.getString("fullname")
                        model.address1 =
                            "Address: " + jsonobject.getString("address1") + " " + jsonobject.getString(
                                "address1"
                            ) + "," + jsonobject.getString("city") + "," + jsonobject.getString("state") + " " + jsonobject.getString(
                                "zipcode"
                            )
                        model.mobileno = "\u20b9" + jsonobject.getString("mobileno")
                        model.payment_method = jsonobject.getString("payment_method")
                        model.product_quantity = jsonobject.getString("product_quantity")
                        array.add(model)


                        //inflating layout dynamically
                        val layoutInflater =
                            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val addView: View = layoutInflater.inflate(R.layout.order_item, null)

                        val url=EndPoints.IMAGE_PATH+jsonobject.getString("prod_image")
                        Glide.with(activity!!)  //2
                            .load(url) //3
                            .centerCrop() //4
                            .placeholder(R.drawable.placeholder) //5
                            .error(R.drawable.placeholder) //6
                            .into(addView.ivProductImage)

                        addView.tvProductName.text=jsonobject.getString("prod_name")

                        addView.tvProductqty.text="Product qty: "+jsonobject.getString("product_quantity") +" (Size:" +jsonobject.getString("size") +" )"

                        vww.llParentview.addView(addView)
                    }


                    // setvalues
                    vww.tvOrderId.text = array[0].m_order_id
                    vww.tvOrderDate.text = array[0].order_date.toString()
                    vww.tvProductname.text = array[0].prod_name.toString()
                    vww.tvOrderTotal.text =
                        array[0].order_total.toString()
                    vww.tvShopName.text = array[0].fullname.toString()
                    vww.tvAddress.text = array[0].address1.toString()
                    vww.tvPhone.text = array[0].mobileno.toString()
                    vww.tvPaymentMode.text = array[0].payment_method.toString()

                    // Collections.reverse(array);

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
}