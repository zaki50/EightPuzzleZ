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
        var newBoardArray: IntArray
        do {
            newBoard.shuffle(random)
            newBoardArray = newBoard.toIntArray()
        } while (!canClear(newBoardArray) || isClearedBoard(newBoardArray))
        return EightPuzzle(newBoardArray, intArrayOf())
    }

    fun move(index: Int): Pair<EightPuzzle, MoveDirection?> {
        val newBoard = board.copyOf()
        val direction = move(newBoard, index, true)

        return Pair(
            if (direction == null)
                this
            else
                EightPuzzle(
                    newBoard,
                    history.copyOf(historySize + 1).also { it[historySize] = board[index] }),
            direction,
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

    fun isCleared(): Boolean = isClearedBoard(board)

    fun lastMoved(): Int {
        if (history.isEmpty()) {
            throw NoSuchElementException("history is empty.")
        }
        return history[history.lastIndex]
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
            // ここにくれば数字の過不足はない
            return canClear(board)
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

        private fun isClearedBoard(board: IntArray): Boolean {
            for (index in 0 until board.size - 1 /* 最後の要素は確認不要 */) {
                if (board[index] != index + 1) {
                    return false
                }
            }
            return true
        }

        /**
         * クリア可能な盤面かどうかの判定
         *
         * 置換の偶奇とblankの最終位置までの距離の偶奇が一致していればクリア可能
         *
         * 参考: https://mathtrain.jp/8puzzle
         */
        private fun canClear(board: IntArray): Boolean {
            val copy = board.copyOf()
            var swapCount = 0
            for (index in 0 until board.size - 1) {
                val num = index + 1
                val indexOfNum = copy.indexOf(num)
                if (index != indexOfNum) {
                    swap(copy, index, indexOfNum)
                    swapCount++
                }
            }
            return swapCount % 2 == distanceOfBlank(board) % 2
        }

        private fun swap(board: IntArray, index0: Int, index1: Int) {
            val val0 = board[index0]
            val val1 = board[index1]
            board[index0] = val1
            board[index1] = val0
        }

        private fun distanceOfBlank(board: IntArray): Int {
            val indexOfBlank = board.indexOf(0)
            val horizontalDistance = when (indexOfBlank % 3) {
                0 -> 2
                1 -> 1
                2 -> 0
                else -> 0
            }
            val verticalDistance = when (indexOfBlank / 3) {
                0 -> 2
                1 -> 1
                2 -> 0
                else -> 0
            }
            return horizontalDistance + verticalDistance
        }
    }
}
