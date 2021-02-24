package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import kotlinx.android.synthetic.main.fragment_customer_details.*
import kotlinx.android.synthetic.main.fragment_customer_details.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CustomerDetails.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomerDetails : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

     var shopname: String? = null
     var address: String? = null


    lateinit var cardView: CardView
    lateinit var tvShopName: TextView
    lateinit var tvAddress: TextView


    lateinit var loader: CustomLoader

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
        val view = inflater?.inflate(R.layout.fragment_customer_details, container, false)

        view.ivAddnew.setOnClickListener{


        }

        fm = activity!!.supportFragmentManager

        cardView=view.findViewById(R.id.cardView)
        tvShopName=view.findViewById(R.id.tvShopName)
        tvAddress=view.findViewById(R.id.tvAddress)


        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        view.tvGetdetails.setOnClickListener{


            if(etMobile.text.toString().trim().length==0)
            {
                Toast.makeText(activity, "Enter Mobile Number", Toast.LENGTH_SHORT).show()
            }
            else
            {
                if (activity!!.isConnectedToNetwork()) {
                    API_GETSHOPDetails("mobileno",view.etMobile.text.toString())
                } else {
                    Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (activity!!.isConnectedToNetwork()) {
            API_GETSHOPDetails(AppController.key,AppController.id)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }

        view.spSelect?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {

                if(position==1)
                {
                    view.llPhone.visibility=View.VISIBLE
                    view.tvProceed.visibility=View.VISIBLE
                    //show(view.llPhone,true)
                }
                else if(position==2)
                {
                    view.llPhone.visibility=View.GONE
                    view.tvProceed.visibility=View.GONE
                    replaceFragment(ShopSearchFragment(), fm)
                }
                else
                {
                    view.llPhone.visibility=View.GONE
                }
            }

        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Select Address"

        view.ivAddnew.setOnClickListener{

            C.page="4"

            replaceFragment(AddShopAddress(), fm)

        }

        view.tvProceed.setOnClickListener{
            replaceFragment(CheckoutFragment(), fm)
        }

        //backpress
        backpressToFragment(CartFragment(), view, fm)
        return view
    }


    companion object {

        lateinit var fm: FragmentManager

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.setCustomAnimations(
                R.anim.enter_from_left,
                R.anim.exit_to_right,
                R.anim.enter_from_right,
                R.anim.exit_to_left
            )

            transaction.commit()
        }
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomerDetails.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CustomerDetails().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


    //web services
    fun API_GETSHOPDetails(key:String,param:String) {
        loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(activity)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETSHOPDETAILS,
                Response.Listener { response ->
                    loader.cancel()

                    val obj = JSONObject(response)

                    Log.e("nnnn",obj.toString())
                    if (obj.getString("Success").equals("true")) {

                        var jsonarray: JSONArray = obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length()) {

                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)


                            shopname=jsonobject.getString("fullname")
                            address=jsonobject.getString("address1")+" "+jsonobject.getString("address2")+", "+jsonobject.getString("city")+", "+jsonobject.getString("state")+", "+jsonobject.getString("zipcode")


                            AppController.prefHelper.set(C.Shop_name,shopname)
                            AppController.prefHelper.set(C.Address,address)
                            AppController.prefHelper.set(C.c_id,jsonobject.getString("id"))

                        }

                        cardView.visibility=View.VISIBLE

                        tvAddress.text=address
                        tvShopName.text=shopname

                        if(!param.isEmpty())
                        {
                            tvProceed.visibility=View.VISIBLE
                        }
                        else
                        {
                            tvProceed.visibility=View.GONE
                        }


                        AppController.id=""

                    }

                    else {
                        cardView.visibility=View.GONE
                        Toast.makeText(activity, "No data found", Toast.LENGTH_SHORT).show()
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
                params2.put(key,param)
                return params2
            }
        }


        mRequestQueue!!.add(mStringRequest!!)
    }
}