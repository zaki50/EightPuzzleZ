package org.zakky.eightpuzzlez

class RankingRepository {
    fun hasRankingData(): Boolean {
        // TODO ランキングデータが存在するかどうかを返す実装をする
        return false
    }

    companion object {
        // TODO DIを導入して管理する
        private val instance = RankingRepository()
        fun get() = instance
    }
}