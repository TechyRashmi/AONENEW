package admin

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.Model
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.blucor.aoneenterprises.main.LoginActivity
import com.thesimplycoder.simpledatepicker.DatePickerHelper
import kotlinx.android.synthetic.main.admin_items.view.*
import kotlinx.android.synthetic.main.alertyesno.*
import kotlinx.android.synthetic.main.app_bar_main1.*
import kotlinx.android.synthetic.main.app_bar_main1.view.*
import kotlinx.android.synthetic.main.fragment_admin_dashboard.*
import kotlinx.android.synthetic.main.fragment_admin_dashboard.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

lateinit var loader: CustomLoader


class AdminDashboard : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var datePicker: DatePickerHelper

    lateinit var array: ArrayList<Model>

    lateinit var vw :View
    var date_selected=""
    var executive_selected=""

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

        vw = inflater?.inflate(R.layout.fragment_admin_dashboard, container, false)

        datePicker = DatePickerHelper(activity!!)

        array= ArrayList()

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="  Admin Dashboard"
        DashboardActivity.ivCart.visibility=View.VISIBLE
        DashboardActivity.iv_menu.visibility=View.GONE

        DashboardActivity.ivCart.setOnClickListener{
            showLogoutDialog()
        }

        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        date_selected=currentDateAndTime

        vw.rlDate.setOnClickListener{
            showDatePickerDialog()
        }

        vw.spExecutive.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                executive_selected= array.get(p2).id.toString()
                //listOfItems[adapterPosition].gender = p0?.selectedItem.toString()
                Log.d("TAG", "" + array.get(p2).id)
                if (activity!!.isConnectedToNetwork()) {
                   API_Location(executive_selected, activity!!)
                } else {
                    Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (activity!!.isConnectedToNetwork()) {
            Api_getExecutiveId()
        } else {
            Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
        }
        // Inflate the layout for this fragment
        return vw
    }


    fun showLogoutDialog() {
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alertyesno)

        dialog.tvAlerttext.text="Are your sure want to Logout?"

        dialog.tvOk.setOnClickListener{
            dialog.cancel()

            Toast.makeText(activity, "Successfully logged out..!!", Toast.LENGTH_SHORT).show()
            AppController.prefHelper.set(C.userid, " ")

            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)

               activity!!.finish()
        }

        dialog.tvcancel.setOnClickListener{
            dialog.cancel()
        }
        dialog.show()

    }


    class SpinnerAdapter(context: Context?, textViewResourceId: Int, list: ArrayList<Model>) :
        ArrayAdapter<Model>
        (context!!, textViewResourceId, list as List<Model?>) {
        var list: ArrayList<Model>
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }


         fun getCustomView(
                 position: Int, convertView: View?,
                 parent: ViewGroup
         ): View {
            // It is used to set our custom view.
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.spinner_adapter,
                        parent,
                        false
                )
            }
            val textViewName = convertView!!.findViewById<TextView>(R.id.tvSpinnerText)
            val currentItem: Model? = getItem(position)

            // It is used the name to the TextView when the
            // current item is not null.
            if (currentItem != null) {
                textViewName.setText(currentItem.fullname)
            }
            return convertView
        }



        init {
            this.list = list
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
                Log.e("date", "" + dayofMonth)
                Log.e("date1", "" + month)
                Log.e("date2", "" + year)
                vw.tvDate.text = "${dayStr}-${monthStr}-${year}"

                date_selected = "${dayStr}-${monthStr}-${year}"

                if (activity!!.isConnectedToNetwork()) {
                    API_Location(executive_selected, activity!!)
                } else {
                    Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    //Api integration
    fun API_Location(excecutive_id: String, ctx: Context) {
          loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(ctx)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_GETLOCATION,
                Response.Listener { response ->
                    loader.cancel()
                    vw.llMain.removeAllViews()
                    val obj = JSONObject(response)
                    Log.e("responseORDERS", "" + obj)
                    if (obj.getString("Success").equals("true")) {
                        // Toast.makeText(activity, "Successfully added to cart", Toast.LENGTH_SHORT).show()


                        var jsonarray: JSONArray = obj.getJSONArray("result")
                        var geocoder: Geocoder
                        var addresses: List<Address>

                        var longitude = 0.0
                        var latitude = 0.0
                        geocoder = Geocoder(activity, Locale.getDefault())
                        for (i in 0 until jsonarray.length()) {

                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)
                            var count = i + 1
                            if (!jsonobject.getString("latitude").equals("0")) {
                                latitude = jsonobject.getString("latitude").toDouble()
                            }

                            if (!jsonobject.getString("latitude").equals("0")) {
                                longitude = jsonobject.getString("longitude").toDouble()
                            }


                            addresses = geocoder.getFromLocation(
                                    latitude,
                                    longitude,
                                    1
                            )


                            // Here 1 represent max location result to returned, by documents it recommended 1 to 5


//                        val address: String =
//                            addresses[0].getAddressLine(addresses[0].MaxAddressLineIndex - 1) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                            val full_address = addresses[0].locality + " " + addresses[0].subLocality + " " + addresses[0].adminArea + " " + addresses[0].postalCode + " " + addresses[0].countryName
                            val layoutInflater =
                                    activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val addView: View = layoutInflater.inflate(R.layout.admin_items, null)
                            addView.tvNo.text = " " + count + ". ";
                            addView.tvAddress.text = full_address
                            addView.tvTime.text = "At " + jsonobject.getString("order_date")
                                    .split(" ")[1] + jsonobject.getString(
                                    "order_date"
                            ).split(" ")[2]


                            vw.llMain.addView(addView)

                        }

                        map.visibility = View.GONE


                    } else {
                        map.visibility = View.VISIBLE
                        val aniSlide: Animation = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
                        map.startAnimation(aniSlide)

                        Toast.makeText(ctx, "No record found", Toast.LENGTH_SHORT).show()
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
                params2.put("excecutive", excecutive_id)
                params2.put("todate", date_selected)
                Log.e("param", "" + params2)
                return params2
            }

        }
        mRequestQueue!!.add(mStringRequest!!)
    }

    //Api integration
    fun Api_getExecutiveId() {

        loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(activity)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.GET,
                EndPoints.URL_GETEXECUTIVE,
                Response.Listener { response ->
                    val obj = JSONObject(response)
                    loader.cancel()
                    var model: Model
                    Log.e("ResponseExecutive", "" + obj)
                    if (obj.getString("Success").equals("true")) {

                        var jsonarray: JSONArray = obj.getJSONArray("executive")

                        for (i in 0 until jsonarray.length()) {
                            model = Model()
                            var jsonobject: JSONObject = jsonarray.getJSONObject(i)
                            model.id = jsonobject.getString("id")
                            model.fullname = jsonobject.getString("fullname")
                            array.add(model)
                        }

                        val obj_adapter: SpinnerAdapter
                        obj_adapter = SpinnerAdapter(activity, R.layout.spinner_adapter, array)
                        vw.spExecutive.adapter = obj_adapter
                    } else {
                        Toast.makeText(activity, "No record found", Toast.LENGTH_SHORT).show()
                    }

                },
                Response.ErrorListener { error ->
                    loader.cancel()
                    Log.i("This is the error", "Error :" + error.toString())

                }) {

        }
        mRequestQueue!!.add(mStringRequest!!)
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AdminDashboard().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}