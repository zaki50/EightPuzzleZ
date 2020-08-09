package org.zakky.eightpuzzlez

import kotlin.random.Random

/**
 * 8パズルの盤面とそれに対する操作を提供するクラス。
 *
 * このクラスはimmutableです。盤面に対する操作を行うと、操作後の新たな盤面を表す
 * インスタンスを生成して返します。
 *
 * ブランク以外はその数値で、ブランクは 0 で表します。
 *
 * このクラスで使用する index は、盤面の以下の位置に対応します。
 *
 * ```
 * |0|1|2|
 * |3|4|5|
 * |6|7|8|
 * ```
 */
class EightPuzzle private constructor(private val board: IntArray, private val history: IntArray) {

    val historySize: Int
        get() = history.size

    val historyIterator: IntIterator
        get() = history.iterator()

    /**
     * moveを行った際にどの方向に動いたかを表します。
     */
    enum class MoveDirection(val offset: Int) {
        UP(-3),
        DOWN(3),
        RIGHT(1),
        LEFT(-1),
    }

    /**
     * 指定された場所の番号を返します。
     *
     * @param index 番号を取得したい場所。
     * @return 指定された場所に存在する番号を返します。ブランクの場合は 0 を返します。
     * @throws ArrayIndexOutOfBoundsException indexが範囲外の場合
     */
    fun numberAt(index: Int): Int = board[index]

    /**
     * 渡された配列に現状の盤面の情報をコピーします。
     *
     * @param to コピー先の配列。長さは[PANEL_COUNT]と一致している必要があります。
     * @throws IllegalArgumentException [to]の長さが[PANEL_COUNT]と一致しない場合。
     */
    fun fillBoardState(to: IntArray) {
        if (to.size != PANEL_COUNT) {
            throw IllegalArgumentException("invalid length of target array: ${to.size}")
        }
        board.copyInto(to)
    }

    /**
     * 数字をシャッフルした盤面を表すインスタンスを生成して返します。
     * 履歴はクリアされます。
     *
     * @param random 乱数生成機
     * @return 生成された盤面を表すインスタンス
     */
    fun shuffle(random: Random): EightPuzzle {
        val newBoard = board.toMutableList()
        var newBoardArray: IntArray
        do {
            newBoard.shuffle(random)
            newBoardArray = newBoard.toIntArray()
        } while (!canClear(newBoardArray) || isClearedBoard(newBoardArray))
        return EightPuzzle(newBoardArray, intArrayOf())
    }

    /**
     * 指定されたインデックスの場所に存在する数字を動かした後の盤面を表すインスタンスを
     * 生成して返します。
     *
     * @param index 動かす数の場所
     * @return 動かした後の盤面と移動方向。移動できない場合は [MoveDirection]をnullで返します。
     * @throws ArrayIndexOutOfBoundsException [index]が範囲外の場合
     */
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

    /**
     * 履歴の最後にある数字を動かし、履歴をひとつ削った盤面のインスタンスを返します。
     *
     * @return ひとつ前の盤面を表すインスタンス
     * @throws IllegalStateException 履歴が空の場合
     */
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

    /**
     * 現状の盤面がクリア状態になっているかを返します。
     *
     * @return クリア盤面の場合は true
     */
    fun isCleared(): Boolean = isClearedBoard(board)

    /**
     * 最後に動かした番号を返します。
     *
     * @return 最後に動かした番号
     * @throws NoSuchElementException 履歴が空の場合
     */
    fun lastMoved(): Int {
        if (history.isEmpty()) {
            throw NoSuchElementException("history is empty.")
        }
        return history[history.lastIndex]
    }

    companion object {
        const val PANEL_COUNT = 9
        private const val WIDTH = 3

        /**
         * クリア状態の盤面で履歴が空のインスタンスを返します。
         *
         * @return クリア状態のインスタンス
         */
        fun newInstance(): EightPuzzle {
            // board = intArrayOf(1, 2, 3, ..., 0)
            val board = IntArray(PANEL_COUNT) { index -> (index + 1) % PANEL_COUNT }
            val history = intArrayOf()

            return EightPuzzle(board, history)
        }

        /**
         * 指定された履歴と盤面を持つインスタンスを生成して返します。
         *
         * [board]で指定された盤面がクリア可能なものであるか、存在しない番号が履歴に含まれていないか、
         * 履歴に含まれる操作が可能なものになっているかのチェックを行います。
         *
         * @param board 最新の盤面を表す配列
         * @param history 履歴を表す配列。要素は動かした番号です。先頭の要素が一番古い操作、末尾の要素が最後の操作です
         */
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
                MoveDirection.LEFT.offset -> if (index % WIDTH == 0) null else MoveDirection.LEFT
                MoveDirection.RIGHT.offset -> if (index % WIDTH == (WIDTH - 1)) null else MoveDirection.RIGHT
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
