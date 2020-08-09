package org.zakky.eightpuzzlez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.zakky.eightpuzzlez.databinding.FragmentMainBinding

@AndroidEntryPoint
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.hasSavedGame.observe(this) { newValue ->
            binding.resumeButton.isEnabled = newValue
        }
        viewModel.hasRankingData.observe(this) { newValue ->
            binding.rankingButton.isEnabled = newValue
        }
    }

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

        viewModel.fetchCurrentState()
    }

    private fun setupButtons() {
        binding.newButton.setOnClickListener {
            viewModel.clearSavedGame()
            findNavController().navigate(MainFragmentDirections.mainToGame())
        }
        binding.resumeButton.let { resumeGameButton ->
            resumeGameButton.isEnabled = false
            resumeGameButton.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.mainToGame())
            }
        }
        binding.rankingButton.let { rankingButton ->
            rankingButton.isEnabled = false
            rankingButton.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.mainToRanking())
            }
        }
    }
}

class MainViewModel @ViewModelInject constructor(
    private val gameRepo: GameRepository,
    private val rankingRepo: RankingRepository,
) : ViewModel() {
    private val _hasSavedGame = MutableLiveData<Boolean>()
    private val _hasRankingData = MutableLiveData<Boolean>()

    val hasSavedGame: LiveData<Boolean>
        get() = _hasSavedGame
    val hasRankingData: LiveData<Boolean>
        get() = _hasRankingData

    fun fetchCurrentState() {
        viewModelScope.launch {
            _hasSavedGame.postValue(gameRepo.hasSavedGame())
            _hasRankingData.postValue(rankingRepo.hasRankingData())
        }
    }

    fun clearSavedGame() {
        viewModelScope.launch {
            gameRepo.clearGame()
        }
    }
}