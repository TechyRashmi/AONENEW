package com.blucor.aoneenterprises.main

import Extra.AppController
import Extra.CustomLoader
import Extra.Utils.Companion.isConnectedToNetwork
import Helpers.C
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult

import com.vendorsapp.activities.auth.AuthVM
import com.vendorsapp.activities.auth.AuthVMF
import network.EndPoints
import network.VolleySingleton
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class LoginActivity : AppCompatActivity(),View.OnClickListener, KodeinAware {

    override val kodein by kodein()

    lateinit var ivLogo:ImageView
    lateinit var llMain:LinearLayout
    lateinit var btnLogin:TextView

    lateinit var etphone :EditText
    lateinit var etPassword :EditText

    lateinit var token :String



    private val factory: AuthVMF by instance<AuthVMF>()

    private lateinit var viewModel: AuthVM

    lateinit var loader: CustomLoader

//    lateinit var lightProgress: LightProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

         setUi()

        onclicklistener()

        startanimation(ivLogo)

        startanimating(llMain)

        getToken()


    }

    fun setUi()
    {
        ivLogo=findViewById(R.id.ivLogo)
        llMain=findViewById(R.id.llMain)
        btnLogin=findViewById(R.id.btnLogin)

        etphone=findViewById(R.id.etphone)
        etPassword=findViewById(R.id.etPassword)

        viewModel = ViewModelProvider(this, factory).get(AuthVM::class.java)

        //loader
        loader = CustomLoader(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)

    }

    fun startanimation(imageView: ImageView)
    {
        val animZoomOut = AnimationUtils.loadAnimation(
                this,
                R.anim.uptodown
        )
        // assigning that animation to
        // the image and start animation
        imageView.startAnimation(animZoomOut)
    }

    fun startanimating(layout: LinearLayout)
    {
        val animZoomOut = AnimationUtils.loadAnimation(
                this,
                R.anim.downtoup
        )
        // assigning that animation to
        // the image and start animation
        layout.startAnimation(animZoomOut)
    }

    fun onclicklistener()
    {
        btnLogin.setOnClickListener(this)
    }
    override fun onClick(v: View?) {

        if (v != null) {
            when (v.id) {
                R.id.btnLogin ->
                    if (etphone.text.toString().trim().isEmpty() || etphone.text.toString().trim().length < 10) {
                        etphone.error = "10 digit Phone Number required"
                        etphone.requestFocus()
                    } else if (etPassword.text.toString().trim().isEmpty()) {
                        etPassword.error = "Password required"
                        etPassword.requestFocus()

                    } else {

                        if (applicationContext.isConnectedToNetwork()) {
                            Api_Login()
                        } else {
                            toast("No Network Available")
                        }
//                    val request = RequestLogin(
//                        etphone.text.toString().trim(),
//                        etPassword.text.toString().trim(),

                        //login(request)
                    }

                else -> { // Note the block
                    print("x is neither 1 nor 2")
                }
            }
        }
    }


    fun Api_Login() {
      loader.show()
        //RequestQueue initialized
        var mRequestQueue = Volley.newRequestQueue(this)
        val mobileno =  etphone.text.toString()
        val password = etPassword.text.toString()
        //String Request initialized
       var mStringRequest = object : StringRequest(
               Request.Method.POST,
               EndPoints.URL_LOGIN,
               Response.Listener { response ->
                   val obj = JSONObject(response)

                   Log.e("ResponseLogin",""+obj)
                   loader.cancel()
                   if (obj.getString("Success").equals("true")) {

                       Log.e("LoginResponse", "" + obj)
                       AppController.prefHelper.set(C.userid, obj.getString("id"))
                       AppController.prefHelper.set(C.name, obj.getString("fullname"))
                       AppController.prefHelper.set(C.profile, obj.getString("profile"))
                       AppController.prefHelper.set(C.mobileno, obj.getString("mobileno"))
                       AppController.prefHelper.set(C.role, obj.getString("role"))
                       val i = Intent(this, DashboardActivity::class.java
                       ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                       startActivity(i)
//                   Prefs.putBoolean(C.isLoggedIn, true)
//                   Prefs.putString(C.email,obj.getString("email"))


//                   Prefs.putString(C.userid,obj.getString("id"))
//                   Prefs.putString(C.name, obj.getString("fullname"))

                       overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)

                       Toast.makeText(applicationContext, "Logged In Successfully", Toast.LENGTH_SHORT)
                               .show()
                       toast("Logged In Successfully")
                   } else {
                       toast("Invalid Credentials")
                   }


               },
               Response.ErrorListener { error ->
                   loader.cancel()
                   Log.i("This is the error", "Error :" + error.toString())
                   Toast.makeText(
                           applicationContext,
                           "Please make sure you enter correct password and username",
                           Toast.LENGTH_SHORT
                   ).show()
               }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>  {
                val params2 = HashMap<String, String>()
                params2.put("mobileno", mobileno)
                params2.put("password", password)
                params2.put("token", token)

                Log.e("param",""+params2)
                return params2
            }

        }
        mRequestQueue!!.add(mStringRequest!!)
    }

    private fun LoginApi() {


        //getting the record values
        val mobileno =  etphone.text.toString()
        val password = etPassword.text.toString()

        //creating volley string request
        val stringRequest = object : StringRequest(
                Request.Method.POST, EndPoints.URL_LOGIN,
                Response.Listener<String> { response ->
                    try {
                        val obj = JSONObject(response)

                        Log.e("testt", "hiiiiiiiii")
                        // Toast.makeText(applicationContext, obj.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {

                        Log.e("cae", e.toString())
                        e.printStackTrace()
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(volleyError: VolleyError) {
                        Toast.makeText(applicationContext, volleyError.message, Toast.LENGTH_LONG)
                                .show()
                    }
                }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("mobileno   ", mobileno)
                params.put("password", password)
                return params
                Log.e("testtttttt", "" + params)
            }
        }
        //adding request to queue
        VolleySingleton.instance?.addToRequestQueue(stringRequest)
    }


    private fun login(data: RequestLogin) {


        viewModel.loginResponse = MutableLiveData()


        viewModel.loginResponse.observe(this, Observer {

            //    Log.e("message", it.message)


//            if (it.success == true) {
//
//                Log.e("testtttt","bye")
//                val i = Intent(this, DashboardActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//               startActivity(i)
//            } else {
//              Log.e("testtttt","hii")
//            }
        })
        viewModel.login(data)
    }


    fun getToken()
    {
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener(object : OnSuccessListener<InstanceIdResult> {
                override fun onSuccess(instanceIdResult: InstanceIdResult) {
                    val fcm_token = instanceIdResult.token //Token

                    token=fcm_token

                    Log.e("testtt", token)

                    // Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show()

                }
            })
    }
}


