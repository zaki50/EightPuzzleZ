package org.zakky.eightpuzzlez

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository @VisibleForTesting constructor(
    private val pref: SharedPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun saveGame(puzzle: EightPuzzle) {
        withContext(dispatcher) {
            val board = IntArray(EightPuzzle.PANEL_COUNT)
            puzzle.fillBoardState(board)

            val boardBuilder = StringBuilder(board.size)
            for (number in board) {
                @Suppress("BlockingMethodInNonBlockingContext")
                boardBuilder.append((number + '0'.toInt()).toChar())
            }
            val historyBuilder = StringBuilder(puzzle.historySize)
            for (number in puzzle.historyIterator) {
                @Suppress("BlockingMethodInNonBlockingContext")
                historyBuilder.append((number + '0'.toInt()).toChar())
            }
            pref.edit()
                .putString(KEY_BOARD, boardBuilder.toString())
                .putString(KEY_MOVE_HISTORY, historyBuilder.toString())
                .apply()
        }
    }

    suspend fun loadGame(): EightPuzzle {
        return withContext(dispatcher) {
            val boardStr = pref.getString(KEY_BOARD, "")
            val historyStr = pref.getString(KEY_MOVE_HISTORY, "")

            val board = parseBoardString(boardStr)
            val history = parseHistoryString(historyStr)
            EightPuzzle.newInstanceWithState(board, history)
        }
    }

    suspend fun clearGame() {
        withContext(dispatcher) {
            pref.edit().clear().apply()
        }
    }

    suspend fun hasSavedGame(): Boolean {
        return withContext(dispatcher) {
            pref.contains(KEY_BOARD)
        }
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
            if (boardStr == null || boardStr.length != EightPuzzle.PANEL_COUNT) {
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