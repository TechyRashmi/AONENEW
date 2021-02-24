package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.isConnectedToNetwork

import ModelClass.ProductModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.ListAdapter
import com.blucor.aoneenterprises.R
import com.blucor.aoneenterprises.main.LoginActivity
import kotlinx.android.synthetic.main.fragment_shop_search.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ShopSearchFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null


    lateinit var loader: CustomLoader

    lateinit var list: ArrayList<String>
    lateinit var list_id: ArrayList<String>

    lateinit var listView: ListView

    lateinit var obj_adapter:ListAdapter

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


        val view = inflater?.inflate(R.layout.fragment_shop_search, container, false)
        fm = activity!!.supportFragmentManager

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)


        //call webservice
        if (activity!!.isConnectedToNetwork()) {
            API_GETSHOP()
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }
        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Search Shop"

        //back press
        Utils.backpressToFragment(MyordersFragment(), view, CustomerDetails.fm)




        //array
        list= ArrayList()
        list_id= ArrayList()

        listView=view.findViewById(R.id.list_view)


        view.search.setActivated(true);
        view.search.setQueryHint("Search here...");
        view.search.onActionViewExpanded();
        view.search.setIconified(false);
        view.search.clearFocus();

        view.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                obj_adapter.getFilter().filter(newText)
                obj_adapter.Filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                val search=query
                return false
            }

        })


        listView.setOnItemClickListener { adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->


            for (j in 0 until list_id.size) {



                       if(list.get(j)==listView.getItemAtPosition(i))
                       {
                           AppController.id=list_id.get(j)
                           AppController.key="shopid"

                       }


            }


            replaceFragment(CustomerDetails(),fm)


        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {


        lateinit var fm: FragmentManager
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ShopSearchFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }

    }

    //web services
    fun API_GETSHOP() {
         loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(activity)

        //String Request initialized
        var mStringRequest = object : StringRequest(
            Request.Method.GET,
            EndPoints.URL_SEARCH,
            Response.Listener { response ->

                  loader.cancel()

                val obj = JSONObject(response)
                Log.e("responseCART", "" + obj)
                if (obj.getString("Success").equals("true")) {
                    // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()

                    var model: ProductModel

                    var jsonarray: JSONArray = obj.getJSONArray("Product")

                    var final_amt: Double = 0.0

                    for (i in 0 until jsonarray.length()) {

                        var jsonobject: JSONObject = jsonarray.getJSONObject(i)

                        list.add(jsonobject.getString("fullname"))
                        list_id.add( jsonobject.getString("id"))


                    }


                     obj_adapter = ListAdapter(this.requireActivity(), list)
                     listView.setAdapter(obj_adapter)
//                    tvFinalprice.text="\u20b9 "+final_amt
//                    val obj_adapter = CartFragment.ProductAdapter(array, activity!!)
//                    recycleCart.setAdapter(obj_adapter)

                } else {
                    Toast.makeText(activity, "Nothing in basket", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                 loader.cancel()
                Log.i("This is the error", "Error :" + error.toString())

            })
        {


        }
        mRequestQueue!!.add(mStringRequest!!)
    }




}