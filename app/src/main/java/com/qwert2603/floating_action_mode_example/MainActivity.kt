package com.qwert2603.floating_action_mode_example

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item.view.*
import kotlinx.android.synthetic.main.user_list_action_mode.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = ItemsAdapter()
        recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        select_all.setOnClickListener { Snackbar.make(activity_main, "select_all", Snackbar.LENGTH_SHORT).show() }
        floating_action_mode.onCloseListener = object : com.qwert2603.floating_action_mode.FloatingActionMode.OnCloseListener {
            override fun onClose() {
                Snackbar.make(activity_main, "closed", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private inner class ItemsAdapter : RecyclerView.Adapter<ItemsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(this@MainActivity).inflate(R.layout.item, parent, false))

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(position)

        override fun getItemCount() = 100

        internal inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

            init {
                itemView.setOnClickListener {
                    Snackbar.make(this@MainActivity.activity_main, itemView.text.text, Snackbar.LENGTH_SHORT).show()
                    if (adapterPosition == 14) {
                        floating_action_mode.canDrag = !floating_action_mode.canDrag
                    }
                    if (adapterPosition == 17) {
                        floating_action_mode.canClose = !floating_action_mode.canClose
                    }
                    if (adapterPosition == 26) {
                        floating_action_mode.contentRes = if (floating_action_mode.contentRes == 0) R.layout.user_list_action_mode else 0
                    }
                }
                itemView.setOnLongClickListener {
                    floating_action_mode.open()
                    return@setOnLongClickListener true
                }
            }

            @SuppressLint("SetTextI18n")
            fun bind(i: Int) = with(itemView) {
                color.setBackgroundColor(Color.argb(i * 256 / 100, 0, 0, 0xff))
                text.text = "Text #" + i
            }
        }

    }
}
