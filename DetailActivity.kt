package com.example.submission_github.detail

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.submission_github.Data
import com.example.submission_github.FavoriteActivity
import com.example.submission_github.R
import com.example.submission_github.SectionPagerAdapter
import com.example.submission_github.database.DatabaseContract.FavoriteColumns.Companion.AVATAR
import com.example.submission_github.database.DatabaseContract.FavoriteColumns.Companion.COMPANY
import com.example.submission_github.database.DatabaseContract.FavoriteColumns.Companion.CONTENT_URI
import com.example.submission_github.database.DatabaseContract.FavoriteColumns.Companion.USERNAME
import com.example.submission_github.setting.SettingActivity
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {

    private var data: Data? = null
    private var status = false
    private lateinit var uriWithId: Uri

    companion object{
        const val KEY_DATA = "key"
        const val KEY_FAV = "key_fav"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        data = intent.getParcelableExtra(KEY_DATA)
        val sectionPagerAdapter = SectionPagerAdapter(this, supportFragmentManager)
        sectionPagerAdapter.username = data?.nama
        view_pager.adapter = sectionPagerAdapter
        tabs.setupWithViewPager(view_pager)
        supportActionBar?.elevation = 0f
        detailData(data?.nama)

        uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + data?.id)

        floating.setOnClickListener{
            status = !status
            val values = ContentValues()
            values.put(USERNAME, data?.nama)
            values.put(COMPANY, data?.company)
            values.put(AVATAR, data?.photo)
            contentResolver.insert(CONTENT_URI, values)
            setFavoorit(status)
        }
    }

    private fun detailData(nama : String?){
        ProgresBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        val url = "https://api.github.com/users/$nama"
        client.addHeader("Authorization","token 7c9c872526439f91c0e8ab022601d26fbc8c2d9d")
        client.addHeader("User-Agent","request")
        client.get(url, object : AsyncHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                ProgresBar.visibility = View.INVISIBLE
                try {
                    val result = String(responseBody)
                    val responseObject = JSONObject(result)

                    val name = responseObject.getString("name")
                    val photo = responseObject.getString("avatar_url")
                    val company = responseObject.getString("company")
                    val location = responseObject.getString("location")

                    data?.nama = name
                    data?.photo = photo
                    data?.company = company
                    data?.location = location

                    Glide.with(this@DetailActivity)
                        .load(data?.photo)
                        .apply(RequestOptions().override(100,100))
                        .into(img_photo)
                    txt_name.text = data?.nama
                    txt_company.text = data?.company
                    txt_location.text = data?.location

                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                ProgresBar.visibility = View.VISIBLE
                Log.d("onFailure", error.message.toString())
            }
        })
    }

    private fun setFavoorit(status: Boolean){
        if (status){
            floating.setImageResource(R.drawable.baseline_favorite_black_18dp)
        }else{
            floating.setImageResource(R.drawable.baseline_favorite_border_black_18dp)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.favorite -> {
                val intent = Intent(this, FavoriteActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return true
        }
    }
}