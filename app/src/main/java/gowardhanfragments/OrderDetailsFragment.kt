package gowardhanfragments

import Extra.CustomLoader
import Extra.Utils.Companion.backpressToFragment
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import ModelClass.ProductModel
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import kotlinx.android.synthetic.main.fragment_order_details.*
import kotlinx.android.synthetic.main.fragment_order_details.view.*
import kotlinx.android.synthetic.main.order_item.view.*
import network.EndPoints
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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

        myview = inflater?.inflate(R.layout.fragment_order_details, container, false)

        fm = activity!!.supportFragmentManager

        //SetHeadertext
        DashboardActivity.tvHeaderText.text="Order Details"

        if(C.page.equals("1"))
        {
            //back press
          backpressToFragment(Shop_order(), myview, fm)
        }
        else
        {
            //back press
            backpressToFragment(ShopScheduled(), myview, fm)
        }

        array = ArrayList()
        //loader
        loader = CustomLoader(activity!!, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)



        myview.ivShare.setOnClickListener{


            //store(getScreenShot(myview)!!, "Testtt")

            if (!checkPermissions()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions()
                }
            }
            else {

                ivShare.visibility=View.GONE
                saveMediaToStorage(getScreenShot(myview)!!)

                val thread: Thread = object : Thread() {
                    override fun run() {
                        try {
                            sleep(3000)
                        } catch (e: InterruptedException) {
                        }
                        activity!!.runOnUiThread(Runnable {
                            ivShare.visibility=View.VISIBLE
                        })
                    }
                }
                thread.start()
            }

//            if (activity!!.isConnectedToNetwork()) {
//                loader.show()
//                API_PDF("" + C.order_id, activity!!)
//            } else {
//                Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
//            }


        }




        if (activity!!.isConnectedToNetwork()) {
           loader.show()
           API_GET_ORDERS("" + C.order_id, activity!!)
        } else {
            Toast.makeText(activity, "No Network Available", Toast.LENGTH_SHORT).show()
        }
        //back press
       // Utils.backpressToFragment(MyordersFragment(), myview, fm)
        return myview
    }

    companion object {
        private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        lateinit var fm: FragmentManager
        lateinit var array: ArrayList<ProductModel>
        lateinit var myview :View
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                OrderDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(activity!!,
                                    Manifest.permission.ACCESS_FINE_LOCATION) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        saveMediaToStorage(getScreenShot(myview)!!)
                        Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
    fun getScreenShot(view: View): Bitmap? {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        return returnedBitmap
    }
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }


    private fun startPermissionRequest() {
        ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (shouldProvideRationale) {
            Log.i("permission", "Displaying permission rationale to provide additional context.")
            startPermissionRequest()
//            showSnackbar("Location permission is needed for core functionality", "Okay",
//                    View.OnClickListener {
//
//                    })
        }
        else {
            Log.i("permission request", "Requesting permission")
            startPermissionRequest()
        }


    }


    private fun showSnackbar(
            mainTextStringId: String, actionStringId: String,
            listener: View.OnClickListener
    ) {
        Toast.makeText(activity!!, mainTextStringId, Toast.LENGTH_LONG).show()
    }
    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            activity!!.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                val image = File(imageUri.toString(), filename)
                shareImage(image)
                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            shareImage(image)
        }


        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(activity, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    fun store(bm: Bitmap, fileName: String?) {
        val dirPath: String = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/Screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {

            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        shareImage(file)
    }


    private fun shareImage(file: File) {

        val uri: Uri = FileProvider.getUriForFile(context!!, context!!.applicationContext.packageName.toString() + ".provider", file)


        Log.e("testttt",""+uri)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
       // intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.setType("image/jpg");

        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No App Available", Toast.LENGTH_SHORT).show()
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

                        val pdf_url = obj.getString("pdf")
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
                            model.size = jsonobject.getString("size")
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

                            val url = EndPoints.IMAGE_PATH + jsonobject.getString("prod_image")
                            Glide.with(activity!!)  //2
                                    .load(url) //3
                                    .centerCrop() //4
                                    .placeholder(R.drawable.placeholder) //5
                                    .error(R.drawable.placeholder) //6
                                    .into(addView.ivProductImage)

                            addView.tvProductName.text = jsonobject.getString("prod_name")

                            addView.tvProductqty.text = "Product qty: " + jsonobject.getString("product_quantity") + " (Size:" + jsonobject.getString("size") + " )"

                            myview.llParentview.addView(addView)
                        }


                        // setvalues
                        myview.tvOrderId.text = array[0].m_order_id
                        myview.tvOrderDate.text = array[0].order_date.toString()
                        myview.tvProductname.text = array[0].prod_name.toString()
                        myview.tvOrderTotal.text =
                                array[0].order_total.toString()
                        myview.tvShopName.text = array[0].fullname.toString()
                        myview.tvAddress.text = array[0].address1.toString()
                        myview.tvPhone.text = array[0].mobileno.toString()
                        myview.tvPaymentMode.text = array[0].payment_method.toString()

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