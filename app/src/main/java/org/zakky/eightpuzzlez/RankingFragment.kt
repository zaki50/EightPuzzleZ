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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.zakky.eightpuzzlez.databinding.FragmentRankingBinding

@AndroidEntryPoint
class RankingFragment : Fragment() {
    private val viewModel: RankingViewModel by viewModels()
    private lateinit var binding: FragmentRankingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.rankingList.observe(this) {
            // TODO リストをUIに表示する
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentRankingBinding.inflate(layoutInflater, container, false).let {
            binding = it
            it.root
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchRankingList()
    }
}

class RankingViewModel @ViewModelInject constructor(
    private val repository: RankingRepository,
) : ViewModel() {
    private val _rankingList = MutableLiveData<List<Ranking>>()

    val rankingList: LiveData<List<Ranking>>
        get() = _rankingList

    fun fetchRankingList() {
        viewModelScope.launch {
            val rankingList = repository.getRankingList()
            _rankingList.postValue(rankingList)
        }
    }
}
