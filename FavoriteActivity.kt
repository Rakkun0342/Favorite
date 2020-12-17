package com.example.submission_github

import android.app.AlertDialog
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.submission_github.adapter.FavoriteAdapter
import com.example.submission_github.database.DatabaseContract.FavoriteColumns.Companion.CONTENT_URI
import com.example.submission_github.database.MappingHelper
import com.example.submission_github.setting.SettingActivity
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {

    private lateinit var adapter : FavoriteAdapter
    private lateinit var uriWithId: Uri

    companion object{
        private const val EXTRA_STATE = "extra_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        showRecycler()

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler){
            override fun onChange(selfChange: Boolean) {
                muatData()
            }
        }
        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        if (savedInstanceState == null){
            muatData()
        }else{
            savedInstanceState.getParcelableArrayList<Data>(EXTRA_STATE)?.also {
                adapter.listData = it
            }
        }
    }
    private fun muatData(){
        GlobalScope.launch(Dispatchers.Main) {
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = contentResolver.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val notes = deferredNotes.await()
            if (notes.size > 0) {
                adapter.listData = notes
            } else {
                adapter.listData = ArrayList()
            }
        }
    }
    private fun showRecycler(){
        adapter = FavoriteAdapter(this )
        adapter.notifyDataSetChanged()

        favorit_rv.layoutManager = LinearLayoutManager(this)
        favorit_rv.adapter = adapter

        adapter.setOnItemClickCallback(object : FavoriteAdapter.OnItemClickCallback{
            override fun onItemClicked(data: Data) {
                uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + data.id)
                val alertDialog = AlertDialog.Builder(this@FavoriteActivity)
                alertDialog.setTitle("Hapus Data")
                alertDialog
                        .setMessage("Apakah anda ingin menghapus data ini")
                        .setCancelable(false)
                        .setPositiveButton("Ya"){ _,_->
                            contentResolver.delete(uriWithId, null, null)
                            finish()
                        }
                        .setNegativeButton("Tidak"){dialog, _ -> dialog.cancel()}
                val alert = alertDialog.create()
                alert.show()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listData)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return true
        }
    }
}