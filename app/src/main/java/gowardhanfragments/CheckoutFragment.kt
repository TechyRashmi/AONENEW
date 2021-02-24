package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
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
import kotlinx.android.synthetic.main.delivernowpopup.*
import kotlinx.android.synthetic.main.fragment_cart.view.*
import kotlinx.android.synthetic.main.fragment_checkout.view.*
import kotlinx.android.synthetic.main.popup.ivClose
import kotlinx.android.synthetic.main.schedule_popup.*
import kotlinx.android.synthetic.main.schedule_popup.tvSubmit
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

lateinit var loader:CustomLoader


var payment_mode=""
var order_total=""
var advanced_amt=""
var remaining_amt=""

var longitude:Double=0.0
var latitude:Double=0.0


class CheckoutFragment : Fragment() {

    lateinit var vw :View
    lateinit var recyclerview :RecyclerView
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var latitudeLabel: String? = null
    private var longitudeLabel: String? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

    }

    public override fun onStart() {
        super.onStart()
        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
            }
        }
        else {
            getLastLocation()
        }
    }
    private fun showMessage(string: String) {


    }
    private fun showSnackbar(
            mainTextStringId: String, actionStringId: String,
            listener: View.OnClickListener
    ) {
        Toast.makeText(activity, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar("Location permission is needed for core functionality", "Okay",
                    View.OnClickListener {
                        startLocationPermissionRequest()
                    })
        }
        else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>,
            grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission granted.
                    getLastLocation()
                }
                else -> {
                    showSnackbar("Permission was denied", "Settings",
                            View.OnClickListener {
                                // Build intent that displays the App settings screen.
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                        "package",
                                        Build.DISPLAY, null
                                )
                                intent.data = uri
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                    )
                }
            }
        }
    }
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.lastLocation!!.addOnCompleteListener(activity!!) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                longitude=lastLocation!!.longitude
                latitude=lastLocation!!.latitude
                Log.e("lat",""+lastLocation!!.latitude)
                Log.e("latlng",""+lastLocation!!.longitude)
            }
            else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        vw = inflater?.inflate(R.layout.fragment_checkout, container, false)

        recyclerview=vw.findViewById(R.id.recyclerview)

        //initialise
        array = ArrayList()
        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        recyclerview.layoutManager= GridLayoutManager(activity, 1)

        vw.tvShopName.text=AppController.prefHelper.get(C.Shop_name)
        vw.tvAddress.text=AppController.prefHelper.get(C.Address)

        vw.tvScheduleDelivery.setOnClickListener{
            showDialog()
        }

        vw.tvDelivernow.setOnClickListener{
            showDialogDelivery()
        }


        if (activity!!.isConnectedToNetwork()) {
            loader.show()
           API_GETCART("" + AppController.prefHelper.get(C.userid), activity!!, recyclerview, vw.tvTotalAmount)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Checkout"

       fm = activity!!.supportFragmentManager
        //backpress
        backpressToFragment(CartFragment(), vw, fm)
        // Inflate the layout for this fragment
        return vw
    }

    fun showDialogDelivery() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delivernowpopup)

        dialog.tvOrderTotal.text="Order Total: \u20b9" + order_total

      //  ivCOD

       // ivOther

        dialog.rrCod.setOnClickListener{

            dialog.ivCOD.setImageResource(R.drawable.ic_radio_button_checked)
            dialog.ivOther.setImageResource(R.drawable.ic_radio_button_unchecked)

            payment_mode="COD"
        }


        dialog.rrOnline.setOnClickListener{
            dialog. ivCOD.setImageResource(R.drawable.ic_radio_button_unchecked)
            dialog.ivOther.setImageResource(R.drawable.ic_radio_button_checked)
            payment_mode="OTHER"
        }


        dialog.ivClose.setOnClickListener{
            dialog.cancel()
        }

        dialog.tvSubmit.setOnClickListener{
            dialog.cancel()

            if (activity!!.isConnectedToNetwork()) {
                loader.show()
                API_ORDER(""+latitude, ""+longitude,"" + AppController.prefHelper.get(C.userid), payment_mode, order_total, "delivered", "", "", activity!!)
            } else {
                Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
            }


        }

        dialog.show()
    }



    fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.schedule_popup)



        //dialog.etAdvance
       // dialog.etRemaining

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.time
        val dateFormat: DateFormat = SimpleDateFormat("dd-MMM-yyyy")
        val tomorrowAsString = dateFormat.format(tomorrow)

        dialog.tvDate.text="Schedule Delivery for "+ tomorrowAsString



        dialog.etAdvance.addTextChangedListener(object: TextWatcher {override fun afterTextChanged(s: Editable?) {

        }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                if(count>0)
                {

                    val totalamt:Double= order_total.toDouble()
                    val advanced=dialog.etAdvance.text.toString().toDouble()

                    Log.e("advance",""+advanced)

                    advanced_amt= advanced.toString()
                    if(totalamt<advanced)
                    {

                        status=false
                        dialog.etAdvance.setError("Amount is greater than total amount")
                        dialog.etAdvance.requestFocus()
                    }
                    else
                    {
                        status=true
                        val remaining:Double=totalamt-advanced
                        dialog.etRemaining.setText(remaining.toString())
                        remaining_amt=remaining.toString()
                    }
                }



            }

        })


        dialog.ivClose.setOnClickListener{
            dialog.cancel()
        }

        dialog.tvSubmit.setOnClickListener{

            if(status==false)
            {
                Toast.makeText(activity, "Adavance amount should be less than total amount", Toast.LENGTH_SHORT).show()
            }
            else
            {
                if (activity!!.isConnectedToNetwork()) {
                    loader.show()
                    API_ORDER(""+latitude, ""+longitude,"" + AppController.prefHelper.get(C.userid), "COD", order_total, "pending", advanced_amt, remaining_amt, activity!!)
                } else {
                    Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
                }

                dialog.cancel()
            }

          //  Toast.makeText(activity, "Order placed successfully", Toast.LENGTH_SHORT).show()

        }

        dialog.show()

    }

    class ProductsAdapter : RecyclerView.Adapter<Holder> {
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
                    LayoutInflater.from(parent.context).inflate(R.layout.cart_items, parent, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {

            holder.tvProductName.text=data.get(position).prod_name
            val url=EndPoints.IMAGE_PATH+data.get(position).prod_image
            Glide.with(holder.itemView)  //2
                    .load(url) //3
                    .centerCrop() //4
                    .placeholder(R.drawable.placeholder) //5
                    .error(R.drawable.placeholder) //6
                    .into(holder.image)

            holder.tvSize.text=data.get(position).size+" (qty: "+ data.get(position).qty +")"

            holder.cart_item_number.text=data.get(position).qty

            holder.price.text=data.get(position).price

            holder.cart_item_number.visibility=View.GONE
            holder.cart_quant_add.visibility=View.GONE
            holder.cart_quant_minus.visibility=View.GONE

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
        var tvSize: TextView
        var price: TextView
        var cart_item_number: TextView
        var cart_quant_minus: ImageButton
        var cart_quant_add: ImageButton


        init {
            image = itemView.findViewById(R.id.cart_item_image)
            tvProductName = itemView.findViewById(R.id.tvProductName)
            tvSize = itemView.findViewById(R.id.tvSize)
            price = itemView.findViewById(R.id.price)


            cart_quant_minus = itemView.findViewById(R.id.cart_quant_minus)
            cart_quant_add = itemView.findViewById(R.id.cart_quant_add)
            cart_item_number = itemView.findViewById(R.id.cart_item_number)
        }
    }

    companion object {

        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        lateinit var fm: FragmentManager

        lateinit var array: ArrayList<ProductModel>

        var status:Boolean = false

        fun successpopup(ctx: Context) {
            val dialog = Dialog(ctx)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.alert)

            dialog.show()
            val timerThread: Thread = object : Thread() {
                override fun run() {
                    try {
                        sleep(2600)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } finally {
                        dialog.dismiss()
                        val intent =
                                Intent(ctx, DashboardActivity::class.java)
                        ctx.startActivity(intent)

                    }
                }
            }

            timerThread.start()
            Toast.makeText(ctx, "Order placed successfully", Toast.LENGTH_SHORT).show()
        }
        //web services
        fun API_GETCART(user_id: String, ctx: Context, recyclerview: RecyclerView, tvTotalAmount: TextView) {
            // loader.show()
            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_GETCART,
                    Response.Listener { response ->


                        if (loader.isShowing) {
                            loader.cancel()
                        }
                        array.clear()
                        vw.tvFinalprice.text = ""

                        val obj = JSONObject(response)
                        Log.e("responseCART", "" + obj)
                        if (obj.getString("Success").equals("true")) {
                            // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                            var model: ProductModel

                            var jsonarray: JSONArray = obj.getJSONArray("Product")

                            var final_amt: Double = 0.0

                            for (i in 0 until jsonarray.length()) {
                                model = ProductModel()
                                var jsonobject: JSONObject = jsonarray.getJSONObject(i)
//                            var size=jsonobject.getString("size")
                                model.prod_id = jsonobject.getString("prod_id")
                                model.prod_image = jsonobject.getString("prod_image")
                                model.prod_name = jsonobject.getString("prod_name")
                                model.size = jsonobject.getString("size")
                                model.qty = jsonobject.getString("qty")



                                model.size_id = jsonobject.getString("size_id")

                                val qty: Double = jsonobject.getString("qty").toDouble()
                                val price: Double = jsonobject.getString("price").toDouble()

                                final_amt = final_amt + jsonobject.getString("price").toDouble() * qty
                                model.price = "\u20b9" + qty * price
                                //  model.prod_size= size.replace("\\r\\n ","")
                                array.add(model)
                            }

                            order_total = "" + final_amt

                            tvTotalAmount.text = "\u20b9" + final_amt
                            val obj_adapter = ProductsAdapter(array, ctx!!)
                            recyclerview.setAdapter(obj_adapter)
                            //  v.tvFinalprice.text="\u20b9 "+final_amt

                        } else {
                            Toast.makeText(ctx, "Nothing in basket", Toast.LENGTH_SHORT).show()
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
                    params2.put("user_id", user_id)



                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }
        fun API_ORDER(latitude:String,longitude:String,user_id: String, payment_method: String, order_total: String, order_status: String, advanced_payment: String, remaining_payment: String, ctx: Context) {

            //RequestQueue initialized
            var mRequestQueue = Volley.newRequestQueue(ctx)

            //String Request initialized
            var mStringRequest = object : StringRequest(
                    Request.Method.POST,
                    EndPoints.URL_PLACEORDER,
                    Response.Listener { response ->

                        val obj = JSONObject(response)
                        Log.e("responseORDER", "" + obj)
                        if (obj.getString("message").equals("true")) {
                            successpopup(ctx)

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
                    params2.put("user_id", "" + AppController.prefHelper.get(C.c_id))
                    params2.put("payment_method", payment_method)
                    params2.put("order_total", order_total)
                    params2.put("executive_id", user_id)
                    params2.put("order_status", order_status)
                    params2.put("advanced_payment", advanced_payment)
                    params2.put("remaining_payment", remaining_payment)
                    params2.put("latitude", latitude)
                    params2.put("longitude", longitude)

                    return params2
                }

            }
            mRequestQueue!!.add(mStringRequest!!)
        }

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CheckoutFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }





}

