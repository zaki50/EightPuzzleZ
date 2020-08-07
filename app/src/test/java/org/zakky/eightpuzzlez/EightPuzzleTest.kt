package org.zakky.eightpuzzlez

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EightPuzzleTest {

    @Test
    fun newInstance() {
        val target = EightPuzzle.newInstance()

        for (index in 0 until EightPuzzle.PANEL_COUNT) {
            assertEquals((index + 1) % EightPuzzle.PANEL_COUNT, target.numberAt(index))
        }
        assertEquals(0, target.historySize)
    }

    @Test
    fun newInstanceWithState_emptyHistory() {
        val board = intArrayOf(0, 2, 8, 4, 6, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf())

        for (index in 0 until EightPuzzle.PANEL_COUNT) {
            assertEquals(board[index], target.numberAt(index))
        }
        assertEquals(0, target.historySize)
    }

    @Test
    fun newInstanceWithState_historyOfUp() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(2))
        assertEquals(1, target.historySize)
        assertEquals(2, target.historyIterator.nextInt())
    }

    @Test
    fun newInstanceWithState_historyOfDown() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(7))
        assertEquals(1, target.historySize)
        assertEquals(7, target.historyIterator.nextInt())
    }

    @Test
    fun newInstanceWithState_historyOfLeft() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(4))
        assertEquals(1, target.historySize)
        assertEquals(4, target.historyIterator.nextInt())
    }

    @Test
    fun newInstanceWithState_historyOfRight() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(5))
        assertEquals(1, target.historySize)
        assertEquals(5, target.historyIterator.nextInt())
    }

    @Test(expected = IllegalArgumentException::class)
    fun newInstanceWithState_historyOfInvalidMove() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        EightPuzzle.newInstanceWithState(board, intArrayOf(1/*斜め移動になってしまうので正しくないヒストリー*/))
    }

    @Test
    fun moveBack() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(5))

        val previous = target.moveBack()
        val previousBoard = IntArray(EightPuzzle.PANEL_COUNT).also {
            previous.fillBoardState(it)
        }

        assertEquals(0, previous.historySize)
        assertArrayEquals(intArrayOf(6, 2, 8, 4, 5, 0, 1, 7, 3), previousBoard)
    }

    @Test
    fun moveBack_targetIsUnmodified() {
        val board = intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3)

        val target = EightPuzzle.newInstanceWithState(board, intArrayOf(5))

        target.moveBack()
        val targetBoard = IntArray(EightPuzzle.PANEL_COUNT).also {
            target.fillBoardState(it)
        }

        assertEquals(1, target.historySize)
        assertArrayEquals(intArrayOf(6, 2, 8, 4, 0, 5, 1, 7, 3), targetBoard)
    }

}
