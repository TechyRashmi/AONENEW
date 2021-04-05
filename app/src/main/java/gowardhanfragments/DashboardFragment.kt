package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.ProductAdapter
import com.blucor.aoneenterprises.R
import com.blucor.steamersindia.adapter.CardPagerAdapter
import com.google.android.material.tabs.TabLayout

import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {

    //Int
    var currentPage = 0

    //timer
    var timer: Timer? = null

    //long
    val DELAY_MS: Long = 1000
    val PERIOD_MS: Long = 3000

    lateinit var loader: CustomLoader

    lateinit var productRecyclerView :RecyclerView



    lateinit var array: ArrayList<ProductModel>
    lateinit var sizearray: ArrayList<ProductModel>


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = inflater?.inflate(R.layout.fragment_dashboard, container, false)

        var mViewPager: ViewPager? =view.findViewById(R.id.myviewpager)

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Dashboard"

        fm= activity!!.supportFragmentManager


        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        array= ArrayList()
        sizearray= ArrayList()

        //Tablayout
        val tabLayout: TabLayout? = view.findViewById(R.id.tabDots)

        productRecyclerView =view.findViewById(R.id.productRecyclerView)
        productRecyclerView.layoutManager = GridLayoutManager(activity, 2)



        tabLayout!!.setupWithViewPager(mViewPager, true)
        val NUM_PAGES = 3
        val handler = Handler()
        val Update = Runnable {
            if (currentPage == NUM_PAGES - 1) {
                currentPage = 0
            }
            mViewPager!!.setCurrentItem(currentPage++, true)
        }

        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post(Update)
            }
        }, DELAY_MS, PERIOD_MS)


        val mCardPagerAdapter: CardPagerAdapter = object : CardPagerAdapter(
                activity!!,
                mResources
        ) {
            override fun onCategoryClick(view: View?, str: String?) {}
        }


        mViewPager!!.adapter = mCardPagerAdapter
        mViewPager!!.offscreenPageLimit = 2
        mViewPager!!.clipToPadding = false
        mViewPager!!.setCurrentItem(1, true)
        mViewPager!!.pageMargin = 10


        if (activity!!.isConnectedToNetwork()) {
            Api_getProducts(""+AppController.prefHelper.get(C.userid))
        } else {

            Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
        }

        // Inflate the layout for this fragment
        return view
    }

    @SuppressLint("RestrictedApi")

    companion object {

        lateinit var fm: FragmentManager

        fun newInstance() = DashboardFragment()

        //Images
        private val mResources = intArrayOf(
                R.drawable.page1,
                R.drawable.page3
        )

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }



    //Api integration
    fun Api_getProducts(user_id: String) {

        loader.show()

        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(activity)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETPRODUCTS,
                Response.Listener { response ->
                    val obj = JSONObject(response)
                    loader.cancel()
                    var model: ProductModel
                    Log.e("ResponseProducts", "" + obj)
                    if (obj.getString("Success").equals("true")) {

                        var jsonarray: JSONArray = obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length()) {
                            model = ProductModel()
                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)
//                            var size=jsonobject.getString("size")
                            model.prod_id = jsonobject.getString("prod_id")
                            model.prod_image = jsonobject.getString("prod_image")
                            model.prod_name = jsonobject.getString("prod_name")
                            //  model.prod_size= size.replace("\\r\\n ","")
                            array.add(model)
                        }

                        val obj_adapter = ProductAdapter(array, activity!!)
                        productRecyclerView.setAdapter(obj_adapter)

                    } else {
                        Toast.makeText(activity, "No products alloted today", Toast.LENGTH_SHORT).show()
                    }

                },
                Response.ErrorListener { error ->
                    loader.cancel()
                    Log.i("This is the error", "Error :" + error.toString())

                }) {

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