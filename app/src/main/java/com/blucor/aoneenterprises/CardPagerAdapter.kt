package com.blucor.steamersindia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.blucor.aoneenterprises.CardAdapter
import com.blucor.aoneenterprises.R

 abstract class CardPagerAdapter(private val mContext: Context, var array: IntArray) :
    PagerAdapter() {
    private var mBaseElevation = 0f
    var clickListener: View.OnClickListener
    protected abstract fun onCategoryClick(view: View?, str: String?)
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =
            LayoutInflater.from(container.context).inflate(R.layout.adapter, container, false)
        container.addView(view)
        val cardView = view.findViewById<View>(R.id.cardView) as CardView
        val iv = view.findViewById<View>(R.id.iv) as ImageView
        if (mBaseElevation == 0.0f) {
            mBaseElevation = cardView.cardElevation
        }

//        Picasso.with(this.mContext).load((String) ((HashMap) this.arraylist.get(position)).get("BannerImage")).into(iv);
        iv.setImageDrawable(mContext.resources.getDrawable(array[position]))
        cardView.maxCardElevation = mBaseElevation * array.size.toFloat()
        view.tag = position
        view.setOnClickListener(clickListener)
        return view
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return array.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    init {
        clickListener = View.OnClickListener { v -> onCategoryClick(v, v.tag.toString()) }
    }
}