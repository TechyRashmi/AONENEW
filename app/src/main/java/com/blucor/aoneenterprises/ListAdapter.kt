package com.blucor.aoneenterprises

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

import kotlinx.android.synthetic.main.row_item.view.*
import java.util.*


class ListAdapter(private val context: Activity, var mData: List<String>) : BaseAdapter(), Filterable {
    var mStringFilterList: List<String>
    var valueFilter: ValueFilter? = null
    private var inflater: LayoutInflater? = null
    var searchString = ""

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): String {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }




    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (inflater == null) {
            inflater = parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }


        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.row_item, null, true)

        Log.e("set","example")


        rowView.tvSearch.setText(mData[position])
        val search = mData[position].toLowerCase(Locale.getDefault())
        if (search.contains(searchString)) {
            val startPos = search.indexOf(searchString)
            val endPos = startPos + searchString.length
            val spanText: Spannable = Spannable.Factory.getInstance()
                .newSpannable(rowView.tvSearch.getText()) // <- EDITED: Use the original string, as `country` has been converted to lowercase.
            spanText.setSpan(
                ForegroundColorSpan(Color.RED),
                startPos,
                endPos,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            rowView.tvSearch.setText(spanText, TextView.BufferType.SPANNABLE)
        }
        return rowView
    }

    override fun getFilter(): Filter {
        if (valueFilter == null) {
            valueFilter = ValueFilter()
        }
        return valueFilter!!
    }

    fun Filter(searchString: String) {
        this.searchString = searchString

        // Filtering stuff as normal.
    }

    inner class ValueFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()
            if (constraint != null && constraint.length > 0) {
                val filterList: MutableList<String> = ArrayList()
                for (i in mStringFilterList.indices) {
                    if (mStringFilterList[i].toUpperCase()
                            .contains(constraint.toString().toUpperCase())
                    ) {
                        filterList.add(mStringFilterList[i])
                    }
                }
                results.count = filterList.size
                results.values = filterList
            } else {
                results.count = mStringFilterList.size
                results.values = mStringFilterList
            }
            return results
        }

        override fun publishResults(
            constraint: CharSequence,
            results: FilterResults
        ) {
            mData = results.values as List<String>
            notifyDataSetChanged()
        }
    }

    init {
        mStringFilterList = mData
    }
}