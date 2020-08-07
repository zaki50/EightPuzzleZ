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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.zakky.eightpuzzlez.databinding.FragmentGameBinding
import kotlin.random.Random


class GameFragment : Fragment(), ResultDialogFragment.OnResultDialogListener {
    private lateinit var viewModel: GameViewModel
    private lateinit var binding: FragmentGameBinding

    private val panels: MutableList<ImageView> = ArrayList(EightPuzzle.PANEL_COUNT)
    private val images: MutableList<Drawable> = ArrayList(EightPuzzle.PANEL_COUNT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.puzzle = if (GameRepository.get().hasSavedGame()) {
                GameRepository.get().loadGame()
            } else {
                EightPuzzle.newInstance().shuffle(Random.Default).also {
                    // onPause() でも保存するが、backした際のMainFragment の onResume() に間に合わないのでここでも保存しておく
                    GameRepository.get().saveGame(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        for (imageId in NUM_IMAGE_IDS) {
            images.add(ResourcesCompat.getDrawable(resources, imageId, activity?.theme)!!)
        }

        return FragmentGameBinding.inflate(layoutInflater, container, false).let {
            initPanels(it)
            binding = it
            setBoardDescription()
            it.root
        }
    }

    private fun setBoardDescription() {
        binding.boardDescription.text = resources.getString(R.string.board_description, viewModel.puzzle.historySize)
    }

    private fun initPanels(binding: FragmentGameBinding) {
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
            IntArray(EightPuzzle.PANEL_COUNT).also { viewModel.puzzle.fillBoardState(it) }
        for (index in 0 until panels.size) {
            val num = board[index]
            panels[index].setImageDrawable(images[num])
            panels[index].setOnClickListener {
                val (puzzle, direction) = viewModel.puzzle.move(index)
                direction?.let {
                    panels[index].setImageDrawable(images[0])
                    panels[index + direction.offset].let { moveTargetPanel ->
                        moveTargetPanel.setImageDrawable(images[puzzle.numberAt(index + direction.offset)])
                        when (direction) {
                            EightPuzzle.MoveDirection.UP -> {
                                animateVertically(panels[index], moveTargetPanel)
                            }
                            EightPuzzle.MoveDirection.DOWN -> {
                                animateVertically(panels[index], moveTargetPanel)
                            }
                            EightPuzzle.MoveDirection.LEFT -> {
                                animateHorizontally(panels[index], moveTargetPanel)
                            }
                            EightPuzzle.MoveDirection.RIGHT -> {
                                animateHorizontally(panels[index], moveTargetPanel)
                            }
                        }

                        viewModel.puzzle = puzzle

                        setBoardDescription()

                        if (puzzle.isCleared()) {
                            GameRepository.get().clearGame()
                            RankingRepository.get().add(Ranking(puzzle.historySize, System.currentTimeMillis()))
                            ResultDialogFragment().also {
                                it.setTargetFragment(this, -1)
                            }.show(parentFragmentManager, "dialog")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
    }

    override fun onPause() {
        super.onPause()

        if (viewModel.puzzle.isCleared()) {
            GameRepository.get().clearGame()
        } else {
            GameRepository.get().saveGame(viewModel.puzzle)
        }
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

        private fun animateVertically(originPanel: View, targetPanel: View) {
            val location = IntArray(2)
            val targetY = location.let {
                targetPanel.getLocationInWindow(location)
                location[1]
            }
            val originY = location.let {
                originPanel.getLocationInWindow(location)
                location[1]
            }
            targetPanel.translationY = (originY - targetY).toFloat()
            targetPanel.animate().translationY(0f)
        }

        private fun animateHorizontally(originPanel: View, targetPanel: View) {
            val location = IntArray(2)
            val targetX = location.let {
                targetPanel.getLocationInWindow(location)
                location[0]
            }
            val originX = location.let {
                originPanel.getLocationInWindow(location)
                location[0]
            }
            targetPanel.translationX = (originX - targetX).toFloat()
            targetPanel.animate().translationX(0f)
        }
    }

    override fun onDialogOkClicked() {
        findNavController().navigateUp()
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