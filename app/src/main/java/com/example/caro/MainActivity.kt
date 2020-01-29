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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    companion object {
        var mCounts = 0
        var deviceId: String = ""
        var isAttack = true
        var isViewMode = false
    }

    var mList: ArrayList<Int> = ArrayList()
    var adapter = AdapterCaro(this, mList)
    var tickList: ArrayList<Int> = ArrayList()
    var countList = arrayOf(15, 10, 12, 14, 16, 18, 20, 22, 25, 30)
    var mRef = FirebaseDatabase.getInstance().reference
    lateinit var dialogNotification: AlertDialog.Builder
    var fullList: ArrayList<Attack> = ArrayList()
    var deviceList = arrayOf("", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        dialogNotification = AlertDialog.Builder(this).setCancelable(true)

        mRecyclerView.adapter = adapter

        val spinnerAdapter = ArrayAdapter(this, R.layout.item_spinner, countList)
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
            mRef.child("attackList").setValue(null)
            mTvViewMode.text = ""
            isViewMode = false
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
                fullList.add(mAtt)

                if (fullList.size == 1) deviceList[0] = mAtt.deviceId
                else if (fullList.size == 2) {
                    deviceList[1] = mAtt.deviceId
                    if (deviceList[0] != deviceId && deviceList[1] != deviceId) {
                        mList[fullList[0].position] = 1
                        mTvViewMode.text = "You are in View mode"
                        isViewMode = true
                    }
                }

                if (isViewMode){
                    if (mAtt.deviceId == deviceList[0]){
                        if (mAtt.position < 0) mList[-mAtt.position] = 1
                        else mList[mAtt.position] = 1
                    } else {
                        if (mAtt.position < 0) mList[-mAtt.position] = 2
                        else mList[mAtt.position] = 2
                    }
                } else {
                    if (mAtt.deviceId != deviceId) {
                        isAttack = true
                        if (mAtt.position < 0) {
                            dialogNotification.setMessage("You lose").show()
                            mList[-mAtt.position] = 3
                        } else {
                            mList[mAtt.position] = 3
                        }
                    } else {
                        isAttack = false
                        if (mAtt.position < 0) {
                            dialogNotification.setMessage("You win").show()
                            mList[-mAtt.position] = 1
                        } else mList[mAtt.position] = 1
                        if (mList.contains(3)) mList[mList.indexOf(3)] = 2
                        tickList.add(mAtt.position)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                tickList.clear()
                mList.clear()
                fullList.clear()
                for (i in 0 until mCounts * mCounts) mList.add(0)
                adapter.notifyDataSetChanged()
                isAttack = true
            }
        })

        mRef.child("Counts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var tempCount = mCounts
                mCounts = p0.value.toString().toInt()
                AdapterCaro.C = mCounts
                mList.clear()
                for (i in 0 until mCounts * mCounts) mList.add(0)

                if (fullList.size > 0) {
                    if (tempCount != 0) {
                        mRef.child("attackList").setValue(null)
                        var tempRemove: ArrayList<Attack> = ArrayList()
                        for (i in 0 until fullList.size) {
                            if (fullList[i].position / tempCount < mCounts && fullList[i].position % tempCount < mCounts){
                                var tempAttack = Attack()
                                tempAttack.deviceId = fullList[i].deviceId
                                tempAttack.position = fullList[i].position / tempCount * mCounts + fullList[i].position % tempCount
                                fullList[i] = tempAttack
                                mRef.child("attackList").push().setValue(tempAttack)
                            } else {
                                if (tickList.contains(fullList[i].position))
                                    tickList.remove(fullList[i].position)
                                tempRemove.add(fullList[i])
                            }
                        }
                        fullList.removeAll(tempRemove)
                    }

                    if (isViewMode)
                        for (i in 0 until fullList.size)
                            if (fullList[i].deviceId == deviceList[0]) mList[fullList[i].position] = 1
                            else mList[fullList[i].position] = 2
                    else
                        for (i in 0 until fullList.size)
                            if (deviceId == fullList[i].deviceId) mList[fullList[i].position] = 1
                            else mList[fullList[i].position] = 2
                }

                mRecyclerView.layoutManager = GridLayoutManager(this@MainActivity, mCounts)
                adapter.notifyDataSetChanged()
                mSpinner.setSelection(countList.indexOf(mCounts))
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
