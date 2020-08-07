package org.zakky.eightpuzzlez

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.zakky.eightpuzzlez.databinding.FragmentGameBinding
import kotlin.random.Random

class GameFragment : Fragment() {
    private lateinit var viewModel: GameViewModel
    private lateinit var binding: FragmentGameBinding

    private val panels: MutableList<ImageView> = ArrayList(EightPuzzle.PANEL_COUNT)
    private val images: MutableList<Drawable> = ArrayList(EightPuzzle.PANEL_COUNT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.puzzle = EightPuzzle.newInstance().let { it.shuffle(Random.Default) }
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
            it.root
        }
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
            panels[index].setOnClickListener { v: View ->
                val (puzzle, direction) = viewModel.puzzle.move(index)
                direction?.let {
                    panels[index].setImageDrawable(images[0])
                    panels[index + direction.offset].setImageDrawable(images[puzzle.numberAt(index + direction.offset)])
                    viewModel.puzzle = puzzle
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
            R.mipmap.blank,
            R.mipmap.num1,
            R.mipmap.num2,
            R.mipmap.num3,
            R.mipmap.num4,
            R.mipmap.num5,
            R.mipmap.num6,
            R.mipmap.num7,
            R.mipmap.num8,
        )
    }
}