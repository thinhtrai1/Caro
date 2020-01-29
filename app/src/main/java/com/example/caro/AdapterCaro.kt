package com.example.caro

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class AdapterCaro(var mContext: Context, var mList: ArrayList<Int>) :
    RecyclerView.Adapter<AdapterCaro.ViewHolder>() {
    companion object{
        var C = 15
    }
    var mRef = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val metrics = DisplayMetrics()
        var windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        var width = metrics.widthPixels
        val layoutParams = LinearLayout.LayoutParams(width / C, width / C)
        holder.mLayout.layoutParams = layoutParams
        if (C > 15) {
            holder.mLayout.setPadding(3, 3, 3, 3)
        } else holder.mLayout.setPadding(5, 5, 5, 5)

        var item = mList[position]
        when (item) {
            1 -> holder.mImage.setImageResource(R.drawable.ic_x)
            2 -> holder.mImage.setImageResource(R.drawable.ic_o)
            3 -> {
                holder.mImage.setImageResource(R.drawable.ic_o)
                holder.mLayout.setBackgroundResource(R.drawable.bg_item_previous_black)
            }
            else -> holder.mImage.setImageResource(0)
        }

        var mActivity = mContext as MainActivity

        holder.mLayout.setOnClickListener {
            if (!MainActivity.isViewMode && MainActivity.isAttack && mActivity.mList[position] == 0) {
                if (hasWin(mActivity.tickList, position)) {
                    var attack = Attack()
                    attack.deviceId = MainActivity.deviceId
                    attack.position = -position
                    mRef.child("attackList").push().setValue(attack)
                } else {
                    var attack = Attack()
                    attack.deviceId = MainActivity.deviceId
                    attack.position = position
                    mRef.child("attackList").push().setValue(attack)
                }
            }
        }
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        var mLayout: LinearLayout = row.findViewById(R.id.mLayout) as LinearLayout
        var mImage: ImageView = row.findViewById(R.id.mImage) as ImageView
    }

    fun hasWin(l: ArrayList<Int>, position: Int): Boolean{
        return (position % C in 4..C - 1 && l.contains(position - 4) && l.contains(position - 3) && l.contains(position - 2) && l.contains(position - 1)
                || position % C in 3..C - 2 && l.contains(position - 3) && l.contains(position - 2) && l.contains(position - 1) && l.contains(position + 1)
                || position % C in 2..C - 3 && l.contains(position - 2) && l.contains(position - 1) && l.contains(position + 1) && l.contains(position + 2)
                || position % C in 1..C - 4 && l.contains(position - 1) && l.contains(position + 1) && l.contains(position + 2) && l.contains(position + 3)
                || position % C in 0..C - 5 && l.contains(position + 1) && l.contains(position + 2) && l.contains(position + 3) && l.contains(position + 4)

                || position > C * 4 - 1 && l.contains(position - C * 4) && l.contains(position - C * 3) && l.contains(position - C * 2) && l.contains(position - C)
                || position > C * 3 - 1 && l.contains(position - C * 3) && l.contains(position - C * 2) && l.contains(position - C) && l.contains(position + C)
                || position > C * 2 - 1 && l.contains(position - C * 2) && l.contains(position - C) && l.contains(position + C) && l.contains(position + C * 2)
                || position > C - 1 && l.contains(position - C) && l.contains(position + C) && l.contains(position + C * 2) && l.contains(position + C * 3)
                || position >= 0 && l.contains(position + C) && l.contains(position + C * 2) && l.contains(position + C * 3) && l.contains(position + C * 4)

                || position % C in 4..C - 1 && l.contains(position + ((C - 1) * 4)) && l.contains(position + ((C - 1) * 3)) && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1)
                || position % C in 3..C - 2 && l.contains(position + ((C - 1) * 3)) && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1) && l.contains(position - (C - 1))
                || position % C in 2..C - 3 && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1) && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2))
                || position % C in 1..C - 4 && l.contains(position + C - 1) && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2)) && l.contains(position - ((C - 1) * 3))
                || position % C in 0..C - 5 && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2)) && l.contains(position - ((C - 1) * 3)) && l.contains(position - ((C - 1) * 4))

                || position % C in 4..C - 1 && l.contains(position - ((C + 1) * 4)) && l.contains(position - ((C + 1) * 3)) && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1))
                || position % C in 3..C - 2 && l.contains(position - ((C + 1) * 3)) && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1)) && l.contains(position + C + 1)
                || position % C in 2..C - 3 && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1)) && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2))
                || position % C in 1..C - 4 && l.contains(position - (C + 1)) && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2)) && l.contains(position + ((C + 1) * 3))
                || position % C in 0..C - 5 && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2)) && l.contains(position + ((C + 1) * 3)) && l.contains(position + ((C + 1) * 4)))
    }
}