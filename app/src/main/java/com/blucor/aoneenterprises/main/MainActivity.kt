
package com.blucor.aoneenterprises.main

import Extra.Utils
import Extra.Utils.Companion.toast
import LocationTrackingService
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blucor.aoneenterprises.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ctx = this@MainActivity

        // Create persistent LocationManager reference
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        getLocation();

        startService(Intent(applicationContext, LocationTrackingService::class.java))
    }


    fun getLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getLocation()
                PackageManager.PERMISSION_DENIED -> Log.e("test","hiiii")//Tell to user the need of grant permission
            }
        }
    }

    companion object {

        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100


        private lateinit var ctx: Context
        private var instance: MainActivity? = null

        // inside a basic activity
        private var locationManager: LocationManager? = null

        fun myFun(context: Context) {
            ctx=context
            context.toast("Location changed")
        }

        private val locationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("testttttt",""+location.longitude)
                Log.e("testttttt",""+location.latitude)

                myFun(ctx)


                // thetext.text = ("" + location.longitude + ":" + location.latitude)
            }
        }

    }
}
