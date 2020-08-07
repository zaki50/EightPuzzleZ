package org.zakky.eightpuzzlez

import android.content.Context
import android.content.SharedPreferences

class GameRepository(private val pref: SharedPreferences) {

    fun saveGame(puzzle: EightPuzzle) {
        val board = intArrayOf(EightPuzzle.PANEL_COUNT + 1)
        puzzle.fillBoardState(board)

        val boardBuilder = StringBuilder(board.size)
        for (number in board) {
            boardBuilder.append((number + '0'.toInt()).toChar())
        }
        val historyBuilder = StringBuilder(puzzle.historySize)
        for (number in puzzle.historyIterator) {
            historyBuilder.append((number + '0'.toInt()).toChar())
        }
        pref.edit()
            .putString(KEY_BOARD, boardBuilder.toString())
            .putString(KEY_MOVE_HISTORY, boardBuilder.toString())
            .apply()
    }

    fun loadGame(): EightPuzzle {
        val boardStr = pref.getString(KEY_BOARD, "")
        val historyStr = pref.getString(KEY_MOVE_HISTORY, "")

        val board = parseBoardString(boardStr)
        val history = parseHistoryString(historyStr)
        return EightPuzzle.newInstanceWithState(board, history)
    }

    fun clearGame() = pref.edit().clear().apply()

    fun hasSavedGame(): Boolean {
        return pref.contains(KEY_BOARD)
    }

    companion object {
        private const val KEY_BOARD = "board"
        private const val KEY_MOVE_HISTORY = "move_history"

        // TODO DIを導入して管理する
        private lateinit var instance: GameRepository
        fun init(context: Context) {
            instance =
                GameRepository(context.getSharedPreferences("game_state", Context.MODE_PRIVATE))
        }

        fun get() = instance

        private fun parseBoardString(boardStr: String?): IntArray {
            if (boardStr == null || boardStr.length != 9) {
                return intArrayOf()
            }
            return boardStr.toCharArray().map { it - '0' }.toIntArray()
        }

        private fun parseHistoryString(historyStr: String?): IntArray {
            if (historyStr == null) {
                return intArrayOf()
            }
            return historyStr.toCharArray().map { it - '0' }.toIntArray()
        }
    }
}