package com.drip.drip_app

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_drip_dashboard.*
import kotlinx.android.synthetic.main.activity_drip_dashboard.exit_card
import kotlinx.android.synthetic.main.activity_drip_dashboard.scan_card
import kotlinx.android.synthetic.main.drip_dashboard_v2.*

class DripDashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drip_dashboard_v2)

        scan_card.setOnClickListener {
            val intent = Intent(this, DripScan::class.java)
            startActivity(intent)
        }
        info_card.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
        tutorial_card.setOnClickListener {
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)
        }

        exit_card.setOnClickListener{
            finish()
        }


    }


}
