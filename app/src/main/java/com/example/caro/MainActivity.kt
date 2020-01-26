package com.example.caro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        var mCounts = 0
        var deviceId: String = ""
        var isAttack = true
    }

    var mList: ArrayList<Int> = ArrayList()
    var adapter = AdapterCaro(this, mList)
    var tickList: ArrayList<Int> = ArrayList()
    var countList = arrayOf(15, 10, 12, 14, 16, 18, 20)
    var mRef = FirebaseDatabase.getInstance().reference
    lateinit var dialogNotification: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        dialogNotification = AlertDialog.Builder(this).setCancelable(true)

        mRecyclerView.adapter = adapter

        val spinnerAdapter =
            ArrayAdapter(this, R.layout.item_spinner, countList)
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner)
        mSpinner.adapter = spinnerAdapter
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mCounts != 0) mRef.child("Counts").setValue(countList[position])
            }
        }

        mBtnAgain.setOnClickListener {
            mRef.setValue(null)
        }

        for (i in 0..500) mList.add(0)
        listener()
    }

    private fun listener() {
        mRef.child("attackList").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                var mAtt = p0.getValue(Attack::class.java)!!
                if (mAtt.deviceId != deviceId) {
                    isAttack = true
                    if (mAtt.position < 0) {
                        dialogNotification.setMessage("Bạn đã thua").show()
                        mList[-mAtt.position] = 3
                    } else {
                        mList[mAtt.position] = 3
                    }
                } else {
                    isAttack = false
                    if (mAtt.position < 0) {
                        dialogNotification.setMessage("Bạn đã thắng").show()
                        mList[-mAtt.position] = 1
                    } else mList[mAtt.position] = 1
                    if (mList.contains(3)) mList[mList.indexOf(3)] = 2
                    tickList.add(mAtt.position)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                tickList.clear()
                mList.clear()
                for (i in 0 until mCounts * mCounts) mList.add(0)
                adapter.notifyDataSetChanged()
                isAttack = true
            }
        })

        mRef.child("Counts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                mCounts = p0.value.toString().toInt()
                AdapterCaro.C = mCounts
                mRef.child("attackList").setValue(null)
                mList.clear()
                for (i in 0 until mCounts * mCounts) mList.add(0)
                mRecyclerView.layoutManager = GridLayoutManager(this@MainActivity, mCounts)
                adapter.notifyDataSetChanged()
                mSpinner.setSelection(countList.indexOf(mCounts))
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
