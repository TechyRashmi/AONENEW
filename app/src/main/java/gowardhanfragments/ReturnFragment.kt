package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R

import kotlinx.android.synthetic.main.fragment_alloted.*
import kotlinx.android.synthetic.main.fragment_alloted.view.recyclerView
import kotlinx.android.synthetic.main.fragment_return.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

var qtyArrayList = ArrayList<EditText>()
var size_list = ArrayList<String>()

class ReturnFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var loader: CustomLoader

    lateinit var array: ArrayList<ProductModel>

    var arrayList = ArrayList<HashMap<String, String>>()

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

        val view = inflater?.inflate(R.layout.fragment_return, container, false)

        array= ArrayList()

        fm = activity!!.supportFragmentManager

        backpressToFragment(DashboardFragment(), view, fm)

        //recyclerview
        view. recyclerView.layoutManager= GridLayoutManager(activity, 1)

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        if (activity!!.isConnectedToNetwork()) {
            Api_getProducts("" + AppController.prefHelper.get(C.userid))
        } else {
            Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Alloted Return Material"

        var map: HashMap<String, String>

        //onclick
        view.btnSubmit.setOnClickListener{



            if(status==false)
            {
                if(view.etRemark.text.toString().trim().length==0)
                {
                    Toast.makeText(activity, "Enter remarks", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    for (item in qtyArrayList.indices) {
                        map= HashMap()

                        // body of loop

                        map.put("return_qty", qtyArrayList[item].text.toString())
                        map.put("size_id", array.get(item).size_id.toString())
                        arrayList.add(map)
                    }

                    val array = JSONArray(arrayList)
                    if (activity!!.isConnectedToNetwork()) {

                        API_RETURN(""+array,view.etRemark.text.toString(),activity!!)

                    } else {

                        Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else
            {
                Toast.makeText(activity, "Invalid entry", Toast.LENGTH_SHORT).show()
            }



        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        lateinit var fm: FragmentManager
         var status:Boolean =false

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
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
                        Log.e("ResponseProducts", "" + obj)
                        var jsonarray: JSONArray = obj.getJSONArray("Product")

                        for (i in 0 until jsonarray.length()) {
                            model = ProductModel()
                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)
                            model.prod_name = jsonobject.getString("prod_name")
                            model.qty = jsonobject.getString("qty")
                            model.size = jsonobject.getString("size")
                            model.size_id = jsonobject.getString("size_id")
                            model.used_qty = jsonobject.getString("used_qty")
                            model.remaining_qty = jsonobject.getString("remaining_qty")
                            array.add(model)



                            if(jsonobject.getString("return_status").equals("pending"))
                            {

                                Log.e("test1","1")
                                if(jsonobject.getString("remark").equals(""))
                                {
                                    Log.e("test2","1")
                                    view!!.btnSubmit.visibility=View.VISIBLE
                                    view!!.tvReturnStatus.visibility=View.GONE
                                }
                                else
                                {
                                    Log.e("test3","1")
                                    view!!.btnSubmit.visibility=View.GONE
                                    view!!.tvReturnStatus.visibility=View.VISIBLE
                                    view!!.etRemark.setText(jsonobject.getString("remark"))
                                }
                            }
                            else
                            {
                                Log.e("test4","1")
                                view!!.btnSubmit.visibility=View.GONE
                                view!!.tvReturnStatus.visibility=View.VISIBLE
                                view!!.etRemark.setText(jsonobject.getString("remark"))
                                view!!.tvReturnStatus.text="Returned Successfully"
                            }



                        }



                        val obj_adapter = ProductAdapter(array, activity!!)
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
                params2.put("return_status", "OTHER")

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
                    LayoutInflater.from(parent.context).inflate(R.layout.return_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {

            holder.tvProductName.text=data.get(position).prod_name

            holder.tvSize.text=data.get(position).size

            holder.tvQty.text=data.get(position).qty

            holder.etQty.setText(data.get(position).remaining_qty)

            holder.tvQty_used.text=data.get(position).used_qty

            holder .etQty.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if(s!!.length>0)
                    {
                        var qty: Int = holder.etQty.text.toString().toInt()
                        var rem_qty: Int = data.get(position).remaining_qty!!.toInt()

                        if (qty > rem_qty) {

                            holder.etQty.setError("Quantity should not be greater than "+rem_qty)
                           holder.etQty.requestFocus()
                            status=true
                        }
                        else
                        {
                            status=false
                        }
                    }

                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


//                    if(isSelected==false)
//                    {
//                        Toast.makeText(ctx, "select size", Toast.LENGTH_SHORT).show()
//
//                    }
//                    else
//                    {
//
//                    }


                }
            })


         //   holder.etQty.setTag(1, data.get(position).size_id)

            qtyArrayList.add(holder.etQty)
            size_list.add(data.get(position).size_id.toString())




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
        var etQty: EditText



        init {

            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvSize = itemView.findViewById(R.id.tvSize)

            tvQty_used = itemView.findViewById(R.id.tvQty_used)
            etQty = itemView.findViewById(R.id.etQty)
            tvQty = itemView.findViewById(R.id.tvQty)

        }
    }

    fun API_RETURN(return_qty: String, remark: String, ctx: Context) {
        loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(ctx)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_RETURN,
                Response.Listener { response ->
                    loader.cancel()
                    val obj = JSONObject(response)
                    Log.e("responseORDER", "" + obj)
                    if (obj.getString("success").equals("1")) {
                        Toast.makeText(ctx, "Return Requested Successfully", Toast.LENGTH_SHORT).show()
                        replaceFragment(DashboardFragment(),fm)

                    } else {
                        Toast.makeText(ctx, "error occured", Toast.LENGTH_SHORT).show()
                    }
                },
                Response.ErrorListener { error ->
                    loader.cancel()
                    Log.i("This is the error", "Error :" + error.toString())

                })
        {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>  {
                val params2 = java.util.HashMap<String, String>()
                params2.put("executive_id", "" + AppController.prefHelper.get(C.userid))
                params2.put("return_qty", return_qty)
                params2.put("remark", remark)

                return params2
            }

        }
        mRequestQueue!!.add(mStringRequest!!)
    }


}