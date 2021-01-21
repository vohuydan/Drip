package com.drip.drip_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_info.*


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        github_image.setOnClickListener{
            gotoURL("https://github.com/vohuydan/Drip")
        }
    }

    private fun gotoURL(string: String){
        val uri: Uri = Uri.parse(string)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

}
