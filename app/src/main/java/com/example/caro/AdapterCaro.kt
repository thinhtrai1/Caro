package com.example.caro

import android.app.Activity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterCaro(
    private val activity: Activity,
    private val mList: ArrayList<Int>,
    private val mOnClick: IOnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mLayoutParams = ViewGroup.LayoutParams(0, 0)
    private var mPadding = 0
    private var isViewMode = false

    fun setViewMode(isViewMode: Boolean) {
        this.isViewMode = isViewMode
    }

    fun setCount(count: Int) {
        val metrics = DisplayMetrics()
        activity.display?.getRealMetrics(metrics)
        mLayoutParams =
            ViewGroup.LayoutParams(metrics.widthPixels / count, metrics.widthPixels / count)
        mPadding = 48 / count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(
            LayoutInflater.from(activity).inflate(R.layout.item, parent, false)) {}
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder.itemView as TextView).apply {
            text = when (mList[position]) {
                0 -> ""
                1 -> "X"
                2 -> "O"
                else -> {
                    holder.itemView.setBackgroundResource(R.drawable.bg_item_previous)
                    "O"
                }
            }
            layoutParams = mLayoutParams
            setPadding(mPadding, mPadding, mPadding, mPadding)
        }

        if (!isViewMode) {
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {
                mOnClick.onItemClick(position)
            }
        } else {
            holder.itemView.isClickable = false
        }
    }

    interface IOnItemClickListener {
        fun onItemClick(position: Int)
    }
}