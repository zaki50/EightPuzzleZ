package org.zakky.eightpuzzlez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.zakky.eightpuzzlez.databinding.FragmentRankingBinding

class RankingFragment : Fragment() {
    private lateinit var binding: FragmentRankingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentRankingBinding.inflate(layoutInflater, container, false).let {
            binding = it
            it.root
        }
    }
}