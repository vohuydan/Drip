package com.drip.drip_app.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.drip.drip_app.R
import kotlinx.android.synthetic.main.fragment_scan_.*

/**
 * A simple [Fragment] subclass.
 */
class Scan_Fragment : Fragment() {
    var startstop:Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        startstop = arguments?.getBoolean("StartOrNah")
        Log.d("INSIDE SCAN_FRAGMENT","h")
        if (startstop == true) {
            //textView2.setText("TRUE")
        }else if (startstop == false){
            //textView2.setText("FALSE")
        }
        return inflater.inflate(R.layout.fragment_scan_, container, false)
    }


}
