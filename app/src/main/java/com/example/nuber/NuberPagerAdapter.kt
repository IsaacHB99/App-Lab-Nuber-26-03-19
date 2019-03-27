package com.example.nuber

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class NuberPagerAdapter(fm: FragmentManager):FragmentPagerAdapter(fm){
    override fun getItem(p0: Int): Fragment {
        return when (p0) {
            0 -> NUberMapsActivity()
            1 -> NuberShoppingFragment()
            else -> NuberProductsFragment()
        }
    }

    override fun getCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return 3
    }



    override fun getPageTitle(position: Int): CharSequence? {
        return super.getPageTitle(position)

        return when (position) {
            0 -> "Mapa Nuber"
            1 -> "Shooping"
            else -> "My "
        }
    }



}