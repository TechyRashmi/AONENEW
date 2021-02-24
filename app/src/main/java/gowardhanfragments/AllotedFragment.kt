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
import android.widget.ImageButton
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
import com.blucor.aoneenterprises.ProductAdapter
import com.blucor.aoneenterprises.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_alloted.*
import kotlinx.android.synthetic.main.fragment_alloted.view.*
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
 * Use the [AllotedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllotedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var loader: CustomLoader

    lateinit var array: ArrayList<ProductModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_alloted, container, false)

        array= ArrayList()

        fm = activity!!.supportFragmentManager

        backpressToFragment(DashboardFragment(), view, fm)

        //recyclerview
        view. recyclerView.layoutManager= GridLayoutManager(activity, 1)

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        if (activity!!.isConnectedToNetwork()) {
            Api_getProducts(""+ AppController.prefHelper.get(C.userid))
        } else {
            Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Alloted Material"
        // Inflate the layout for this fragment
        return view
    }

    companion object {
        lateinit var fm: FragmentManager
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AllotedFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
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
                EndPoints.URL_ALLOTEDPRODUCT,
                Response.Listener { response ->
                    val obj = JSONObject(response)
                    loader.cancel()
                    var model: ProductModel

                    if (obj.getString("Success").equals("true")) {

                        Log.e("ResponseProducts",""+obj)

                        var jsonarray :JSONArray= obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length())
                        {
                            model = ProductModel()
                            var jsonobject :JSONObject=jsonarray.getJSONObject(i)
                            model.prod_name=jsonobject.getString("prod_name")
                            model.qty=jsonobject.getString("qty")
                            model.size=jsonobject.getString("size")
                            model.used_qty=jsonobject.getString("used_qty")
                            model.remaining_qty=jsonobject.getString("remaining_qty")
                            array.add(model)
                        }

                        val obj_adapter = ProductAdapter(array,activity!!)
                        recyclerView.setAdapter(obj_adapter)

                    } else {
                    }

                },
                Response.ErrorListener { error ->
                    loader.cancel()
                    Log.i("This is the error", "Error :" + error.toString())

                }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>  {
                val params2 = HashMap<String, String>()
                params2.put("Executive_id", user_id)
                params2.put("return_status", " ")

                return params2
            }

        }
        mRequestQueue!!.add(mStringRequest!!)
    }


    //Adapter
    class ProductAdapter : RecyclerView.Adapter<Holder> {
        var data = java.util.ArrayList<ProductModel>()
        lateinit var image: IntArray
        lateinit var array: Array<String>
        lateinit var ctx: Context

        constructor(favList: java.util.ArrayList<ProductModel>, ctx: Context) {
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
                    LayoutInflater.from(parent.context).inflate(R.layout.alloted_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {

            holder.tvProductName.text=data.get(position).prod_name

            holder.tvSize.text=data.get(position).size

            holder.tvQty.text=data.get(position).qty
            holder.tvLeftqty.text=data.get(position).remaining_qty
            holder.tvQty_used.text=data.get(position).used_qty





           // holder.price.text=data.get(position).price


        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }
    }
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvProductName: TextView
        var tvSize: TextView
        var tvQty: TextView
        var tvQty_used: TextView
        var tvLeftqty: TextView



        init {

            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvSize = itemView.findViewById(R.id.tvSize)
            //price = itemView.findViewById(R.id.price)

            tvQty_used = itemView.findViewById(R.id.tvQty_used)
            tvLeftqty = itemView.findViewById(R.id.tvLeftqty)
            tvQty = itemView.findViewById(R.id.tvQty)
        }
    }

}