package org.zakky.eightpuzzlez

import kotlin.random.Random

class EightPuzzle private constructor(private val board: IntArray, private val history: IntArray) {

    val historySize: Int
        get() = history.size

    val historyIterator: IntIterator
        get() = history.iterator()

    enum class MoveDirection(val offset: Int) {
        UP(-3),
        DOWN(3),
        RIGHT(1),
        LEFT(-1),
    }

    fun numberAt(index: Int): Int = board[index]

    fun fillBoardState(to: IntArray) {
        if (to.size != PANEL_COUNT) {
            throw java.lang.IllegalArgumentException("invalid length of target array: ${to.size}")
        }
        board.copyInto(to)
    }

    fun shuffle(random: Random): EightPuzzle {
        val newBoard = board.toMutableList()
        newBoard.shuffle(random)
        return EightPuzzle(newBoard.toIntArray(), intArrayOf())
    }

    fun move(index: Int): Pair<EightPuzzle, MoveDirection?> {
        val newBoard = board.copyOf()
        val direction = move(newBoard, index, true) ?: return Pair(this, null)

        return Pair(
            EightPuzzle(
                newBoard,
                history.copyOf(historySize + 1).also { it[historySize] = board[index] }), direction
        )
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

    fun isCleared(): Boolean {
        for(index in 0 until board.size - 1 /* 最後の要素は確認不要 */) {
            if (board[index] != index + 1) {
                return false
            }
        }
        return true
    }

    companion object {
        const val PANEL_COUNT = 9

        fun newInstance(): EightPuzzle {
            // board = intArrayOf(1, 2, 3, ..., 0)
            val board = IntArray(PANEL_COUNT) { index -> (index + 1) % PANEL_COUNT }
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
            val seen = BooleanArray(PANEL_COUNT)

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
                if (item < 1 || PANEL_COUNT <= item) {
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
                MoveDirection.LEFT.offset -> MoveDirection.LEFT
                MoveDirection.RIGHT.offset -> MoveDirection.RIGHT
                MoveDirection.UP.offset -> MoveDirection.UP
                MoveDirection.DOWN.offset -> MoveDirection.DOWN
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
