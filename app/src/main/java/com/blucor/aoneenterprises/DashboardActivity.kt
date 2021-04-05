package com.blucor.aoneenterprises

import Extra.AppController
import Helpers.C
import admin.AdminDashboard
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.blucor.aoneenterprises.main.LoginActivity
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import gowardhanfragments.*
import kotlinx.android.synthetic.main.alertyesno.*
import kotlinx.android.synthetic.main.nav_header_main1.view.*
import kotlinx.android.synthetic.main.popup.*
import kotlinx.android.synthetic.main.schedule_popup.*
import network.EndPoints

class DashboardActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {


    lateinit var navigationView: NavigationView
    lateinit var drawer: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setUI()

        //Load dashboard fragment
        if (savedInstanceState == null) {
            if(AppController.prefHelper.get(C.role)=="g_admin")
            {
                replaceFragment(AdminDashboard())
            }
            else
            {
                replaceFragment(DashboardFragment())
            }
        }
    }

    fun setUI()
    {
        iv_menu=findViewById(R.id.iv_menu)
        navigationView=findViewById(R.id.nav_view)
        drawer=findViewById(R.id.drawer_layout);
        ivCart=findViewById(R.id.ivCart);
        tvHeaderText=findViewById(R.id.tvHeaderText);


        iv_menu.setOnClickListener(this)
        ivCart.setOnClickListener(this)
        navigationView.setNavigationItemSelectedListener(this)


        val menu = navigationView.menu
         navigationView.getHeaderView(0).tvName.text=AppController.prefHelper.get(C.name)
        navigationView.getHeaderView(0).etMobile.text=AppController.prefHelper.get(C.mobileno)
        val url= EndPoints.IMAGE_PATH+AppController.prefHelper.get(C.profile)

        Glide.with(this)  //2
                .load(url) //3
                .centerCrop() //4
                .placeholder(R.drawable.userr) //5
                .error(R.drawable.userr) //6
                .into(navigationView.getHeaderView(0).ivImage)

    }


    fun showLogoutDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alertyesno)

       dialog.tvAlerttext.text="Are your sure want to Logout?"

        dialog.tvOk.setOnClickListener{
            dialog.cancel()

            Toast.makeText(this, "Successfully logged out..!!", Toast.LENGTH_SHORT).show()
            AppController.prefHelper.set(C.userid, " ")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            finish()
        }

        dialog.tvcancel.setOnClickListener{
            dialog.cancel()
        }
        dialog.show()

    }

    override fun onClick(v: View?) {

        if (v != null) {
            when (v.id) {

                R.id.iv_menu -> drawer.openDrawer(Gravity.LEFT)
                R.id.ivCart -> replaceFragment(CartFragment())


                else -> { // Note the block
                    print("x is neither 1 nor 2")
                }
            }
        }

    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.cart) {
            replaceFragment(CartFragment())
        }
        else if(id==R.id.order)
        {
            replaceFragment(Shop_order())
        }
        else if(id==R.id.logout)
        {
            showLogoutDialog()
        }
        else if(id==R.id.alloted)
        {
            replaceFragment(MyAllotedFragment())
        }
        else if(id==R.id.home)
        {
            replaceFragment(DashboardFragment())
        }
        else if(id==R.id.customer)
        {
            replaceFragment(MyCustomers())
        }
        else if(id==R.id.scedule)
        {
            replaceFragment(ShopScheduled())
        }
        else if(id==R.id.Return)
        {
            replaceFragment(ReturnFragment())
        }
        drawer.closeDrawer(Gravity.LEFT)
        return true
    }


    //Load  fragment
    fun replaceFragment(fragment: Fragment) {
        val newFragment: Fragment = fragment
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.main_fragment_container, newFragment).commit()

    }


    companion object
    {
        lateinit var tvHeaderText :TextView
        lateinit var ivCart:ImageView

        lateinit var iv_menu: ImageView
    }



    //handling backpress
    private var backPressedTime:Long = 0
    lateinit var backToast: Toast
    override fun onBackPressed() {
        backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG)
        if (backPressedTime + 1000 > System.currentTimeMillis()) {
            backToast.cancel()
            super.onBackPressed()
            return
        } else {
            backToast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}


