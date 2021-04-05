package gowardhanfragments

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_add_shop_address.*
import kotlinx.android.synthetic.main.fragment_add_shop_address.view.*
import network.EndPoints
import org.json.JSONObject
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddShopAddress : Fragment() {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    lateinit var loader: CustomLoader

    var emailPattern :String= "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    var longitude:Double=0.0
    var latitude:Double=0.0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        // Inflate the layout for this fragment
        val view = inflater?.inflate(R.layout.fragment_add_shop_address, container, false)
        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

        fm = activity!!.supportFragmentManager

        //back press
        if(C.page.equals("3"))
        {
            backpressToFragment(MyCustomers(), view, fm)
        }
        else
        {
            backpressToFragment(CustomerDetails(), view, fm)
        }


        //onclick
        view.tvAddShop.setOnClickListener{
            if(view.etName.text.isEmpty()){
                view.etName.setError("Enter Shop Name")
                view.etName.requestFocus()
            }
            else if(view.etPhone.text.isEmpty() || view.etPhone.text.toString().trim().length < 10)
            {
                view.etPhone.setError("Enter 10 digit Phone number")
            }
            else if(view.etEmail.text.isEmpty())
            {
                view.etEmail.setError("valid email")

            }
            else  if(!view.etEmail.text.toString().trim().matches(emailPattern.toRegex()))
            {
                etEmail.requestFocus()
                etEmail.setError("Enter valid email")
            }
            else
            {
                //call api
                if (activity!!.isConnectedToNetwork()) {
                    Api_AddShop("" + latitude, "" + longitude, view.etName.text.toString(), view.etPhone.text.toString(), view.etEmail.text.toString(), view.etAdd1.text.toString(), view.etAdd2.text.toString(), view.etCity.text.toString(), view.etState.text.toString(), view.etPin.text.toString(), "" + AppController.prefHelper.get(C.userid))
                } else {
                    Toast.makeText(activity, "No network connection", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Add Shop"

        return view
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
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                        activity!!,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        activity!!,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                Log.e("log", "" + longitude)
                Log.e("lng", "" + latitude)
            }
            else {
                Log.w(TAG, "getLastLocation:exception", task.exception)
                showMessage("No location detected. Make sure location is enabled on the device.")
            }
        }
    }
    private fun showMessage(string: String) {

    }
    private fun showSnackbar(
            mainTextStringId: String, actionStringId: String,
            listener: View.OnClickListener
    ) {
        Toast.makeText(activity!!, mainTextStringId, Toast.LENGTH_LONG).show()
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
    companion object {
        private val TAG = "LocationProvider"
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        fun replaceFragment(fragment: Fragment, fm: FragmentManager) {
            val transaction = fm.beginTransaction()
            transaction.replace(R.id.main_fragment_container, fragment)
            transaction.disallowAddToBackStack()
            transaction.commit()
        }

        lateinit var fm: FragmentManager
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddShopAddress().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    //Api
    fun Api_AddShop(latitude: String, longitude: String, shop_nm: String, mobileno: String, email: String, address1: String, address2: String, city: String, state: String, zipcode: String, add_by: String) {

        loader.show()

        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(activity)

        //String Request initialized
        var mStringRequest = object : StringRequest(
                Request.Method.POST,
                EndPoints.URL_ADDSHOP,
                Response.Listener { response ->
                    val obj = JSONObject(response)
                    loader.cancel()
                    var model: ProductModel

                    Log.e("obj", "" + obj)

                    if (obj.getString("message").equals("true")) {

                        Toast.makeText(activity, "Shop Added Successfully", Toast.LENGTH_SHORT).show()
                        AppController.id = mobileno
                        AppController.key = "mobileno"

                        if(C.page.equals("3"))
                        {
                            replaceFragment(MyCustomers(), fm)
                        }
                        else
                        {
                            replaceFragment(CustomerDetails(), fm)
                        }

                    } else {
                        Toast.makeText(activity, obj.getString("success_msg"), Toast.LENGTH_SHORT).show()
                    }

                },
                Response.ErrorListener { error ->
                    loader.cancel()
                    Log.i("This is the error", "Error :" + error.toString())

                }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>  {
                val params2 = HashMap<String, String>()
                params2.put("fullname", shop_nm)
                params2.put("mobileno", mobileno)
                params2.put("email", email)
                params2.put("address1", address1)
                params2.put("address2", address2)
                params2.put("city", city)
                params2.put("state", state)
                params2.put("zipcode", zipcode)
                params2.put("add_by", add_by)
                params2.put("latitude", latitude)
                params2.put("longitude", longitude)
                Log.e("params", "" + params2)


                return params2
            }

        }
        mRequestQueue!!.add(mStringRequest!!)
    }
}