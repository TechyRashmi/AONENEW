package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import kotlinx.android.synthetic.main.fragment_alloted.*
import kotlinx.android.synthetic.main.fragment_alloted.recyclerView
import kotlinx.android.synthetic.main.fragment_alloted.view.*
import kotlinx.android.synthetic.main.fragment_alloted.view.recyclerView
import kotlinx.android.synthetic.main.fragment_my_alloted.*
import kotlinx.android.synthetic.main.fragment_my_alloted.view.*
import kotlinx.android.synthetic.main.fragment_my_alloted.view.btnSubmit
import kotlinx.android.synthetic.main.fragment_my_alloted.view.etRemark
import kotlinx.android.synthetic.main.fragment_return.view.*
import kotlinx.android.synthetic.main.myslloteditems.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
var sizelist = ArrayList<String>()
var totallist = ArrayList<String>()
var qtyyArrayList = ArrayList<TextView>()
var boxArrayList = ArrayList<TextView>()
class MyAllotedFragment : Fragment() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_my_alloted, container, false)

        array= ArrayList()

        fm = activity!!.supportFragmentManager

        Utils.backpressToFragment(DashboardFragment(), view, fm)

        //recyclerview
        view. recyclerView.layoutManager= GridLayoutManager(activity, 1)

        view.btnSubmit.setOnClickListener{

            arrayList.clear()

            if(view.etRemark.text.toString().trim().length==0)
            {
                Toast.makeText(activity, "Enter remarks", Toast.LENGTH_SHORT).show()
            }
            else
            {
                var map: HashMap<String, String>
                for (item in qtyyArrayList.indices) {
                    map= HashMap()

                    // body of loop
                    map.put("piece_left", qtyyArrayList[item].text.toString())
                    map.put("size_id", sizelist.get(item))
                    map.put("box_left", boxArrayList.get(item).text.toString())
                    map.put("total", totallist.get(item))
                    arrayList.add(map)
                }

                val array = JSONArray(arrayList)

                Log.e("aaaa",""+array)

                if (activity!!.isConnectedToNetwork()) {

                    API_RETURN(""+array,view.etRemark.text.toString(),activity!!)

                } else {

                    Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        if (activity!!.isConnectedToNetwork()) {
//            val obj_adapter =ProductAdapter(activity!!)
//            view. recyclerView.setAdapter(obj_adapter)
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

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyAllotedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MyAllotedFragment().apply {
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

        var mStringRequest = object : StringRequest(
            Request.Method.POST,
            EndPoints.URL_ALLOTEDPRODUCT,
            Response.Listener { response ->
                val obj = JSONObject(response)
                loader.cancel()
                var model: ProductModel

                if (obj.getString("Success").equals("true")) {
                    Log.e("ResponseProducts",""+obj)

                    var jsonarray : JSONArray = obj.getJSONArray("Product")

                    for (i in 0 until jsonarray.length())
                    {
                        model = ProductModel()
                        var jsonobject : JSONObject =jsonarray.getJSONObject(i)
                        model.prod_name=jsonobject.getString("prod_name")
                        model.size_id=jsonobject.getString("size_id")
                        model.prod_image = jsonobject.getString("prod_image")
                        model.qty=jsonobject.getString("qty")
                        model.size=jsonobject.getString("size")
                        model.used_qty=jsonobject.getString("used_qty")
                        model.remaining_qty=jsonobject.getString("remaining_qty")
                        model.alloted_box=jsonobject.getString("alloted_box")
                        model.piece_per_box=jsonobject.getString("piece_per_box")
                        model.left_box=jsonobject.getString("left_box")
                        model.total_qty=jsonobject.getString("total_qty")
                        array.add(model)
                        llMain.visibility=View.VISIBLE
                        if(jsonobject.getString("return_status").equals("pending"))
                        {
                            if(jsonobject.getString("remark").equals(""))
                            {
                                view!!.btnSubmit.visibility=View.VISIBLE
                                view!!.tvReturnstatus.visibility=View.GONE
                            }
                            else
                            {

                                view!!.btnSubmit.visibility=View.GONE
                                view!!.tvReturnstatus.visibility=View.VISIBLE
                                view!!.etRemark.setText(jsonobject.getString("remark"))
                            }
                        }
                        else
                        {
                            view!!.btnSubmit.visibility=View.GONE
                            view!!.tvReturnstatus.visibility=View.VISIBLE

                            view?.tvReturnstatus?.text="Returned Successfully"
                            view!!.etRemark.setText(jsonobject.getString("remark"))

                        }

                    }

                    val obj_adapter =ProductAdapter(array, activity!!)
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
                params2.put("return_status", "pending")

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
                LayoutInflater.from(parent.context).inflate(R.layout.myslloteditems, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            with(holder.itemView)
            {
            tvProductName.text=data.get(position).prod_name
                tvSize.text="Size: "+data.get(position).size
                val url=EndPoints.IMAGE_PATH+data.get(position).prod_image
                Glide.with(holder.itemView)  //2
                    .load(url) //3
                    .centerCrop() //4
                    .placeholder(R.drawable.placeholder) //5
                    .error(R.drawable.placeholder) //6
                    .into(productImage)
               tvLeftqty.text=data.get(position).remaining_qty
                tvQty_used.text=data.get(position).used_qty

                tvBoxes.text=data.get(position).alloted_box
                tvPiece.text=data.get(position).qty
                tvTotal.text=data.get(position).total_qty
                tvBoxleft.text=data.get(position).left_box

                sizelist.add(data.get(position).size_id.toString())

                qtyyArrayList.add(tvLeftqty)
                boxArrayList.add(tvBoxleft)

                var piece:Int=data.get(position).piece_per_box!!.toInt()
                var left:Int= data.get(position).left_box!!.toInt()
                var remaining:Int= data.get(position).remaining_qty!!.toInt()
                var qty=piece*left + remaining
                totallist.add(qty.toString())


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

//        var tvProductName: TextView
//        var tvSize: TextView
//        var tvQty: TextView
//        var tvQty_used: TextView
//        var tvLeftqty: TextView



        init {

//            tvProductName = itemView.findViewById(R.id.tvProductName)
//            tvSize = itemView.findViewById(R.id.tvSize)
//            //price = itemView.findViewById(R.id.price)
//
//            tvQty_used = itemView.findViewById(R.id.tvQty_used)
//            tvLeftqty = itemView.findViewById(R.id.tvLeftqty)
//            tvQty = itemView.findViewById(R.id.tvQty)
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