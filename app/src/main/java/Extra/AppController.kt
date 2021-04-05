package Extra

import LocationTrackingService
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.Fragment
import com.blucor.aoneenterprises.main.MainActivity


import com.vendorsapp.activities.auth.AuthRepo
import com.vendorsapp.activities.auth.AuthVMF

import com.vendorsapp.network.MyApi
import com.vendorsapp.network.NetworkConnectionInterceptors

import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton


class AppController : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@AppController))
        bind() from singleton { MyApi(instance()) }
        bind() from singleton { NetworkConnectionInterceptors(instance()) }

        bind() from singleton { AuthRepo(instance()) }
        bind() from provider { AuthVMF(instance()) }


    }
//    private fun initPreferences() {
//        Prefs.Builder()
//            .setContext(this)
//            .setMode(ContextWrapper.MODE_PRIVATE)
//            .setPrefsName(packageName)
//            .setUseDefaultSharedPreference(true)
//            .build()
//    }

    override fun onCreate() {
        super.onCreate()

       prefHelper = Preferences(this)





   //   loader = CustomLoader(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)


        //initPreferences()

        /* if (!Places.isInitialized()) {
             Places.initialize(this, getString(R.string.map_api_key), Locale.US);
         }*/

        //Fresco.initialize(this)


    }


    companion object {
        const val base_url = "https://example.i-tech.consulting"
        lateinit var prefHelper: Preferences


        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100


        private lateinit var ctx: Context
        private var instance: MainActivity? = null

        // inside a basic activity
        private var locationManager: LocationManager? = null


      //  lateinit var loader: CustomLoader

         var id:String=""
         var mobileno:String=""
         var key:String="shopid"
        /*fun refreshDashboard() {
            if (dashboarInstance != null)
                dashboarInstance!!.showCartItem()
        }*/
    }

}