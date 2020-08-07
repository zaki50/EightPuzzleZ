package org.zakky.eightpuzzlez

import kotlin.random.Random

class EightPuzzle private constructor(private val board: IntArray, private val history: IntArray) {

    val historySize: Int
        get() = history.size

    val historyIterator: IntIterator
        get() = history.iterator()

    enum class MoveDirection {
        UP,
        DOWN,
        RIGHT,
        LEFT,
    }

    fun numberAt(index: Int): Int = board[index]

    fun fillBoardState(to: IntArray) {
        if (to.size != PANEL_COUNT + 1) {
            throw java.lang.IllegalArgumentException("invalid length of target array: ${to.size}")
        }
        board.copyInto(to)
    }

    fun shuffle(random: Random): EightPuzzle {
        val newBoard = board.toMutableList()
        newBoard.shuffle()
        return EightPuzzle(newBoard.toIntArray(), intArrayOf())
    }

    fun moveBack(): EightPuzzle {
        if (history.isEmpty()) {
            throw IllegalStateException("history is empty")
        }

        val newBoard = board.copyOf()
        move(newBoard, newBoard.indexOf(history[history.size - 1]), true)
            ?: throw IllegalStateException("failed to move back to previous board")

        // 最後の履歴を削除しながら複製する
        val newHistory = history.copyOf(history.size - 1)
        return EightPuzzle(newBoard, newHistory)
    }

    companion object {
        const val PANEL_COUNT = 8

        fun newInstance(): EightPuzzle {
            // board = intArrayOf(1, 2, 3, ..., 0)
            val board = IntArray(PANEL_COUNT + 1) { index -> (index + 1) % (PANEL_COUNT + 1) }
            val history = intArrayOf()

            return EightPuzzle(board, history)
        }

        fun newInstanceWithState(
            board: IntArray,
            history: IntArray,
        ): EightPuzzle {
            if (!isValidBoard(board)) {
                throw IllegalArgumentException("invalid board")
            }
            if (!isValidHistory(history, board)) {
                throw IllegalArgumentException("invalid history")
            }
            return EightPuzzle(board, history)
        }

        private fun isValidBoard(board: IntArray): Boolean {
            val seen = BooleanArray(PANEL_COUNT + 1)

            if (board.size != seen.size) {
                return false
            }
            for (item in board) {
                if (item < 0 || seen.size <= item) {
                    return false
                }
                if (seen[item]) {
                    return false
                }
                seen[item] = true
            }
            // 重複があれば上のfor文でreturn falseしているはずなので無条件にtrueを返して良い
            return true
        }

        private fun isValidHistory(history: IntArray, originalBoard: IntArray): Boolean {
            for (item in history) {
                if (item < 1 || PANEL_COUNT < item) {
                    return false
                }
            }

            val currentBoard = originalBoard.copyOf()
            for (movedNumber in history.reversed()) {
                val targetIndex = currentBoard.indexOf(movedNumber)
                if (move(currentBoard, targetIndex, true) == null) {
                    return false
                }
            }
            return true
        }

        @Suppress("SameParameterValue")
        private fun move(
            board: IntArray,
            index: Int,
            updateBoard: Boolean = false,
        ): MoveDirection? {
            val movedNumber = board[index]
            if (movedNumber == 0) {
                return null
            }
            val indexOfEmpty = board.indexOf(0)
            return when (indexOfEmpty - index) {
                -1 -> MoveDirection.LEFT
                1 -> MoveDirection.RIGHT
                -3 -> MoveDirection.UP
                3 -> MoveDirection.DOWN
                else -> null
            }.also {
                if (updateBoard && it != null) {
                    board[indexOfEmpty] = movedNumber
                    board[index] = 0
                }
            }
        }
    }
}
