package org.zakky.eightpuzzlez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.zakky.eightpuzzlez.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentMainBinding.inflate(layoutInflater, container, false).let {
            binding = it
            setupButtons()
            it.root
        }
    }

    override fun onResume() {
        super.onResume()

        binding.resumeButton.isEnabled = GameRepository.get().hasSavedGame()
        binding.rankingButton.isEnabled = RankingRepository.get().hasRankingData()
    }

    private fun setupButtons() {
        binding.newButton.setOnClickListener {
            GameRepository.get().clearGame()
            findNavController().navigate(R.id.main_to_game)
        }
        binding.resumeButton.setOnClickListener {
            findNavController().navigate(R.id.main_to_game)
        }
        binding.rankingButton.setOnClickListener {
            findNavController().navigate(R.id.main_to_ranking)
        }
    }
}