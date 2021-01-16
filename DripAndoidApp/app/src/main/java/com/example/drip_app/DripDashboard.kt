package com.example.drip_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_drip_dashboard.*

class DripDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drip_dashboard)

        scan_card.setOnClickListener {
            val intent = Intent(this, DripScan::class.java)
            startActivity(intent)
        }
        instagram_card.setOnClickListener {
            gotoURL("https://www.instagram.com/jvd.aquatics/")
        }

        linkedin_card.setOnClickListener {
            gotoURL("https://www.linkedin.com/in/huydanvo/")
        }

        exit_card.setOnClickListener{
            finish()
        }

        go_to_end_button.setOnClickListener{
            scrollView.setScrollY(5000)
        }
    }

    private fun gotoURL(string: String){
        val uri: Uri = Uri.parse(string)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}
