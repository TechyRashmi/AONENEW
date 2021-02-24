package Extra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blucor.aoneenterprises.DashboardActivity
import com.blucor.aoneenterprises.R


class Utils {

    companion object{

        fun Context.toast(message: CharSequence) =
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
           
          fun backpressToFragment(fragment: Fragment, view: View, fm: FragmentManager)
          {
              //Back

              //Back
              view.setFocusableInTouchMode(true)
              view.requestFocus()
              view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                  if (event.action == KeyEvent.ACTION_DOWN) {
                      if (keyCode == KeyEvent.KEYCODE_BACK) {


                          val transaction = fm.beginTransaction()
                          transaction.replace(R.id.main_fragment_container, fragment)
                          transaction.disallowAddToBackStack()
                          transaction.commit()

                          return@OnKeyListener true
                      }
                  }
                  false
              })

          }

        fun backpressToActivity(activity: Activity, view: View, fm: FragmentManager)
        {
            //Back

            //Back
            view.setFocusableInTouchMode(true)
            view.requestFocus()
            view.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        return@OnKeyListener true
                    }
                }
                false
            })

        }

         fun Context.isConnectedToNetwork(): Boolean {
            val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting() ?: false
        }
    }
}