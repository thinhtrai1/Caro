package com.example.caro

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var mRoomName = ""
    private var person = 1
    private val mDeviceId: String = "P" + (100000 + Random().nextInt(899999))
    private var isAttack = true
    private val mCaroList = ArrayList<Int>()
    private val mCheckList = ArrayList<Int>()
    private val countList = arrayOf(15, 10, 12, 14, 16, 18, 20, 22, 25, 30)
    private val mRef = FirebaseDatabase.getInstance().reference
    private lateinit var mAdapter: AdapterCaro

    companion object {
        private var C = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dialog(this, android.R.style.Theme_Black_NoTitleBar).apply {
            val currentTime = System.currentTimeMillis()
            val rooms = ArrayList<Room>()
            val names = ArrayList<String>()
            val list = ArrayList<String>()
            val listView = ListView(this@MainActivity)
            val adapter = ArrayAdapter(this@MainActivity, R.layout.item_spinner, list)
            setContentView(listView)
            show()
            listView.adapter = adapter
            val listener = object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(Room::class.java)?.let {
                        list[names.indexOf(snapshot.key!!)] = "Room " + snapshot.key + ". Persons: " + it.persons
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(Room::class.java)?.let {
                        if (currentTime - it.id > 86400000) {
                            it.id = currentTime
                            it.persons = 0
                            mRef.child(snapshot.key!!).setValue(it)
                        }
                        rooms.add(it)
                        names.add(snapshot.key!!)
                        list.add("Room " + snapshot.key + ". Persons: " + it.persons)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            mRef.addChildEventListener(listener)
            listView.setOnItemClickListener { _, _, i, _ ->
                mRef.removeEventListener(listener)
                mRoomName = names[i]
                C = rooms[i].count
                person = rooms[i].persons + 1
                dismiss()
                toRoom(rooms[i].persons > 1)
                mRef.child(mRoomName).child("persons").setValue(person)
            }

            setOnDismissListener {
                if (mRoomName.isEmpty()) {
                    mRef.removeEventListener(listener)
                    mRoomName = String.format("%03d", rooms.size)
                    val room = Room()
                    room.id = System.currentTimeMillis()
                    C = room.count
                    mRef.child(mRoomName).setValue(room)
                    toRoom(false)
                }
            }
        }

        mSpinner.adapter = ArrayAdapter(this, R.layout.item_spinner, countList)
        mSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (mRoomName.isNotEmpty()) {
                    mRef.child(mRoomName).child("attackList").setValue(null)
                    mRef.child(mRoomName).child("count").setValue(countList[position])
                }
            }
        }

        mBtnAgain.setOnClickListener {
            mRef.child(mRoomName).child("attackList").push().setValue("again")
        }
    }

    private fun toRoom(isViewMode: Boolean) {
        supportActionBar?.title = "Caro - Room $mRoomName"
        if (isViewMode) {
            mTvViewMode.text = getString(R.string.you_are_in_view_mode)
            mBtnAgain.visibility = View.GONE
            mSpinner.isEnabled = false
        } else {
            mRef.child(mRoomName).child("attackList").push().setValue("again")
        }
        for (i in 1..C * C) {
            mCaroList.add(0)
        }
        mAdapter = AdapterCaro(this, mCaroList, object : AdapterCaro.IOnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isAttack && mCaroList[position] == 0) {
                    isAttack = false
                    if (isWin(mCheckList, position)) {
                        mRef.child(mRoomName).child("attackList").push().setValue(Attack().apply {
                            this.deviceId = mDeviceId
                            this.position = -position
                        })
                    } else {
                        mRef.child(mRoomName).child("attackList").push().setValue(Attack().apply {
                            this.deviceId = mDeviceId
                            this.position = position
                        })
                    }
                }
            }
        })
        mAdapter.setViewMode(isViewMode)
        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = GridLayoutManager(this, C)

        listener()
    }

    private fun listener() {
        mRef.child(mRoomName).child("attackList").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if (p0.value.toString() == "again") {
                    for (i in 0 until mCaroList.size) {
                        mCaroList[i] = 0
                    }
                    mAdapter.notifyDataSetChanged()
                    mRef.child(mRoomName).child("attackList").setValue(null)
                    mCheckList.clear()
                    return
                }
                p0.getValue(Attack::class.java)?.let {
                    if (it.deviceId == mDeviceId) {
                        if (it.position < 0) {
                            mCheckList.add(-it.position)
                            mCaroList[-it.position] = 1
                            mAdapter.notifyItemChanged(-it.position)
                            showNotification(true)
                            mCheckList.clear()
//                            mRef.child(mRoomName).child("attackList").push().setValue("again")
                        } else {
                            mCheckList.add(it.position)
                            mCaroList[it.position] = 1
                            mAdapter.notifyItemChanged(it.position)
                        }
                    } else {
                        isAttack = true
                        if (it.position < 0) {
                            mCaroList[-it.position] = 2
                            mAdapter.notifyItemChanged(-it.position)
                            showNotification(false)
                            mCheckList.clear()
                        } else {
                            mCaroList[it.position] = 2
                            mAdapter.notifyItemChanged(it.position)
                        }
                    }
                }
            }
        })

        mRef.child(mRoomName).child("count").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val previousCount = C
                C = p0.value.toString().toInt()
                mCaroList.clear()
                for (i in 0 until  C * C) {
                    mCaroList.add(0)
                }
                (mRecyclerView.layoutManager as GridLayoutManager).spanCount = C
                mAdapter.setCount(C)
                mAdapter.notifyDataSetChanged()
                mSpinner.setSelection(countList.indexOf(C))

                val temps = mCheckList.toIntArray()
                mCheckList.clear()
                if (previousCount < C && temps.isNotEmpty()) {
                    for (i in temps) {
                        mRef.child(mRoomName).child("attackList").push().setValue(Attack().apply {
                            this.deviceId = mDeviceId
                            this.position = i % previousCount + i / previousCount * C
                        })
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        mRef.child(mRoomName).child("persons").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                person = snapshot.value.toString().toInt()
            }
        })
    }

    private fun showNotification(isWin: Boolean) {
        AlertDialog
            .Builder(this)
            .setMessage(if (isWin) "You win" else "You lose")
            .show()
    }

    private fun isWin(l: ArrayList<Int>, position: Int): Boolean{
        return (position % C in 4 until C && l.contains(position - 4) && l.contains(position - 3) && l.contains(position - 2) && l.contains(position - 1)
                || position % C in 3..C - 2 && l.contains(position - 3) && l.contains(position - 2) && l.contains(position - 1) && l.contains(position + 1)
                || position % C in 2..C - 3 && l.contains(position - 2) && l.contains(position - 1) && l.contains(position + 1) && l.contains(position + 2)
                || position % C in 1..C - 4 && l.contains(position - 1) && l.contains(position + 1) && l.contains(position + 2) && l.contains(position + 3)
                || position % C in 0..C - 5 && l.contains(position + 1) && l.contains(position + 2) && l.contains(position + 3) && l.contains(position + 4)

                || position > C * 4 - 1 && l.contains(position - C * 4) && l.contains(position - C * 3) && l.contains(position - C * 2) && l.contains(position - C)
                || position > C * 3 - 1 && l.contains(position - C * 3) && l.contains(position - C * 2) && l.contains(position - C) && l.contains(position + C)
                || position > C * 2 - 1 && l.contains(position - C * 2) && l.contains(position - C) && l.contains(position + C) && l.contains(position + C * 2)
                || position > C - 1 && l.contains(position - C) && l.contains(position + C) && l.contains(position + C * 2) && l.contains(position + C * 3)
                || position >= 0 && l.contains(position + C) && l.contains(position + C * 2) && l.contains(position + C * 3) && l.contains(position + C * 4)

                || position % C in 4 until C && l.contains(position + ((C - 1) * 4)) && l.contains(position + ((C - 1) * 3)) && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1)
                || position % C in 3..C - 2 && l.contains(position + ((C - 1) * 3)) && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1) && l.contains(position - (C - 1))
                || position % C in 2..C - 3 && l.contains(position + ((C - 1) * 2)) && l.contains(position + C - 1) && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2))
                || position % C in 1..C - 4 && l.contains(position + C - 1) && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2)) && l.contains(position - ((C - 1) * 3))
                || position % C in 0..C - 5 && l.contains(position - (C - 1)) && l.contains(position - ((C - 1) * 2)) && l.contains(position - ((C - 1) * 3)) && l.contains(position - ((C - 1) * 4))

                || position % C in 4 until C && l.contains(position - ((C + 1) * 4)) && l.contains(position - ((C + 1) * 3)) && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1))
                || position % C in 3..C - 2 && l.contains(position - ((C + 1) * 3)) && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1)) && l.contains(position + C + 1)
                || position % C in 2..C - 3 && l.contains(position - ((C + 1) * 2)) && l.contains(position - (C + 1)) && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2))
                || position % C in 1..C - 4 && l.contains(position - (C + 1)) && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2)) && l.contains(position + ((C + 1) * 3))
                || position % C in 0..C - 5 && l.contains(position + C + 1) && l.contains(position + ((C + 1) * 2)) && l.contains(position + ((C + 1) * 3)) && l.contains(position + ((C + 1) * 4)))
    }

    override fun onDestroy() {
        if (mRoomName.isNotEmpty()) {
            mRef.child(mRoomName).child("persons").setValue(person - 1)
        }
        super.onDestroy()
    }

    class Room {
        var id = 0L
        var persons = 1
        var count = 15
    }

    class Attack {
        var deviceId: String = ""
        var position: Int = 0
    }
}
