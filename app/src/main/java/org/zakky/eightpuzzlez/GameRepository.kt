package org.zakky.eightpuzzlez

class GameRepository {
    fun hasSavedGame(): Boolean {
        // TODO ゲームが保存されているかどうかを返す実装をする
        return false
    }

    companion object {
        // TODO DIを導入して管理する
        private val instance = GameRepository()
        fun get() = instance
    }
}