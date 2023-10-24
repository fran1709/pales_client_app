package com.example.pales

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ImagePagerAdapter(
    fragmentActivity: FragmentActivity,
    private val imageResources: List<Int>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return imageResources.size
    }

    override fun createFragment(position: Int): Fragment {
        val imageResourceId = imageResources[position]
        return ImageFragment.newInstance(imageResourceId)
    }
}
