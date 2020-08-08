package org.zakky.eightpuzzlez

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.zakky.eightpuzzlez.databinding.FragmentGameBinding
import kotlin.random.Random


class GameFragment : Fragment(), ResultDialogFragment.OnResultDialogListener {
    private lateinit var viewModel: GameViewModel
    private lateinit var binding: FragmentGameBinding

    private lateinit var move: LiveData<Pair<EightPuzzle.MoveDirection, EightPuzzle>>

    private val panels: MutableList<ImageView> = ArrayList(EightPuzzle.PANEL_COUNT)
    private val images: MutableList<Drawable> = ArrayList(EightPuzzle.PANEL_COUNT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (modelClass) {
                    GameViewModel::class.java -> GameViewModel(
                        GameRepository.get(),
                        RankingRepository.get()
                    ) as T
                    else -> throw AssertionError()
                }
            }
        }).get(GameViewModel::class.java)
        viewModel.puzzle.let { initialPuzzleLiveData ->
            initialPuzzleLiveData.observe(this, object : Observer<EightPuzzle> {
                override fun onChanged(loadedPuzzle: EightPuzzle) {
                    initialPuzzleLiveData.removeObserver(this)

                    updateDescription(loadedPuzzle)

                    val moveSink = MutableLiveData<Pair<EightPuzzle.MoveDirection, EightPuzzle>>()
                    move = moveSink
                    move.observe(this@GameFragment) { (_, puzzleAfterMove) ->
                        updateDescription(puzzleAfterMove)

                        val board = IntArray(EightPuzzle.PANEL_COUNT).also { array ->
                            puzzleAfterMove.fillBoardState(array)
                        }

                        val lastMovedNum = puzzleAfterMove.lastMoved()
                        val indexOfEmpty = board.indexOf(0)
                        val indexOfMoved = board.indexOf(lastMovedNum)

                        panels[indexOfEmpty].setImageDrawable(images[0])
                        panels[indexOfMoved].setImageDrawable(images[lastMovedNum])
                        animatePanel(panels[indexOfMoved], from = panels[indexOfEmpty])

                        if (puzzleAfterMove.isCleared()) {
                            viewModel.clearGameData()
                            viewModel.addRankingData(
                                Ranking(
                                    puzzleAfterMove.historySize,
                                    System.currentTimeMillis()
                                )
                            )
                            ResultDialogFragment().also { dialogFragment ->
                                dialogFragment.setTargetFragment(this@GameFragment, -1)
                            }.show(parentFragmentManager, "dialog")
                        } else {
                            viewModel.saveGame(puzzleAfterMove)
                        }
                    }

                    initPanels(binding, loadedPuzzle, moveSink)
                }
            })
        }
        viewModel.fetchPuzzle()
    }

    private fun updateDescription(puzzle: EightPuzzle) {
        binding.boardDescription.text =
            resources.getString(R.string.board_description, puzzle.historySize)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        for (imageId in NUM_IMAGE_IDS) {
            images.add(ResourcesCompat.getDrawable(resources, imageId, activity?.theme)!!)
        }

        return FragmentGameBinding.inflate(layoutInflater, container, false).let {
            binding = it
            it.root
        }
    }

    private fun initPanels(
        binding: FragmentGameBinding,
        initialPuzzle: EightPuzzle,
        moveData: MutableLiveData<Pair<EightPuzzle.MoveDirection, EightPuzzle>>
    ) {
        panels.add(binding.panel0)
        panels.add(binding.panel1)
        panels.add(binding.panel2)
        panels.add(binding.panel3)
        panels.add(binding.panel4)
        panels.add(binding.panel5)
        panels.add(binding.panel6)
        panels.add(binding.panel7)
        panels.add(binding.panel8)

        val board =
            IntArray(EightPuzzle.PANEL_COUNT).also { initialPuzzle.fillBoardState(it) }
        for (index in 0 until panels.size) {
            val num = board[index]
            panels[index].setImageDrawable(images[num])
            panels[index].setOnClickListener {
                getLatestPuzzle()?.let { puzzleBeforeMove ->
                    val (puzzleAfterMove, direction) = puzzleBeforeMove.move(index)
                    direction?.let {
                        moveData.postValue(Pair(direction, puzzleAfterMove))
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
    }

    companion object {
        private val NUM_IMAGE_IDS = intArrayOf(
            R.mipmap.transparent,
            R.mipmap.num1,
            R.mipmap.num2,
            R.mipmap.num3,
            R.mipmap.num4,
            R.mipmap.num5,
            R.mipmap.num6,
            R.mipmap.num7,
            R.mipmap.num8,
        )

        private fun animatePanel(target: View, from: View) {
            val location = IntArray(2)
            val (targetX, targetY) = location.also {
                target.getLocationInWindow(location)
            }
            val (fromX, fromY) = location.also {
                from.getLocationInWindow(location)
            }
            target.translationX = (fromX - targetX).toFloat()
            target.translationY = (fromY - targetY).toFloat()
            target.animate().translationX(0f).translationY(0f)
        }
    }

    override fun onDialogOkClicked() {
        findNavController().navigateUp()
    }

    private fun getLatestPuzzle(): EightPuzzle? {
        return move.value?.second ?: viewModel.puzzle.value
    }
}

class ResultDialogFragment : DialogFragment() {
    interface OnResultDialogListener {
        fun onDialogOkClicked()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // ダイアログ生成  AlertDialogのBuilderクラスを指定してインスタンス化します
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext()).also {
            it.setTitle(R.string.clear_dialog_title)
            it.setMessage(R.string.clear_dialog_message)
        }

        dialogBuilder.setPositiveButton(resources.getString(android.R.string.ok)) { _, _ -> // editTextから値を取得
            (targetFragment as OnResultDialogListener).onDialogOkClicked()
        }
        return dialogBuilder.create()
    }
}

class GameViewModel(
    private val gameRepo: GameRepository,
    private val rankingRepo: RankingRepository,
) : ViewModel() {

    private val _puzzle = MutableLiveData<EightPuzzle>()

    val puzzle: LiveData<EightPuzzle>
        get() = _puzzle

    fun fetchPuzzle() {
        viewModelScope.launch {
            val puzzle = if (gameRepo.hasSavedGame()) {
                gameRepo.loadGame()
            } else {
                EightPuzzle.newInstance().shuffle(Random.Default).also {
                    GameRepository.get().saveGame(it)
                }
            }
            _puzzle.postValue(puzzle)
        }
    }

    fun clearGameData() {
        viewModelScope.launch {
            gameRepo.clearGame()
        }
    }

    fun saveGame(puzzle: EightPuzzle) {
        viewModelScope.launch {
            gameRepo.saveGame(puzzle)
        }
    }

    fun addRankingData(ranking: Ranking) {
        viewModelScope.launch {
            rankingRepo.add(ranking)
        }
    }
}
