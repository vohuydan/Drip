package com.drip.drip_app.PagerAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.drip.drip_app.Fragments.Paired_Fragment
import com.drip.drip_app.Fragments.Scan_Fragment

class PagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!){
    override fun getItem(position: Int): Fragment {

        return when(position) {
            0 -> {
                Scan_Fragment()
            }
            1->{
                Paired_Fragment()
            }

            else ->Scan_Fragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }
}