package org.zakky.eightpuzzlez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zakky.eightpuzzlez.databinding.FragmentRankingBinding

class RankingFragment : Fragment() {
    private lateinit var viewModel: RankingViewModel
    private lateinit var binding: FragmentRankingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (modelClass) {
                    RankingViewModel::class.java -> RankingViewModel(
                        RankingRepository.get()
                    ) as T
                    else -> throw AssertionError()
                }
            }
        }).get(RankingViewModel::class.java)

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

class RankingViewModel(
    private val repository: RankingRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
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
