package org.zakky.eightpuzzlez

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

class RankingRepository(private val pref: SharedPreferences) {
    private val rankingHolderAdapter: JsonAdapter<RankingHolder> =
        Moshi.Builder().build().adapter(RankingHolder::class.java)

    fun add(ranking: Ranking) {
        val rankingList: MutableList<Ranking> = getRankingList()
        val index = rankingList.binarySearch(ranking, object : Comparator<Ranking> {
            override fun compare(left: Ranking?, right: Ranking?): Int {
                if (left == null) {
                    return if (right == null) 0 else -1
                }
                if (right == null) return 1
                if (left.moveCount == right.moveCount) {
                    return when { // 時刻は昔のものほど後ろ
                        left.time == right.time -> 0
                        left.time < right.time -> 1
                        else -> -1
                    }
                }
                return if (left.moveCount < right.moveCount) -1 else 1
            }
        })

        rankingList.add(if (index < 0) -(index + 1) else index, ranking)

        val json = rankingHolderAdapter.toJson(RankingHolder(rankingList))
        pref.edit()
            .putString(KEY_RANKING, json)
            .apply()
    }

    fun getRankingList(): MutableList<Ranking> {
        return pref.getString(KEY_RANKING, "").let {
            if (it.isNullOrEmpty()) {
                mutableListOf()
            } else {
                rankingHolderAdapter.fromJson(it)?.ranking?.toMutableList()
                    ?: mutableListOf()
            }
        }
    }

    fun clearRanking() = pref.edit().clear().apply()

    fun hasRankingData(): Boolean {
        return pref.contains(KEY_RANKING)
    }

    companion object {
        private const val KEY_RANKING = "ranking"

        // TODO DIを導入して管理する
        private lateinit var instance: RankingRepository
        fun init(context: Context) {
            instance =
                RankingRepository(context.getSharedPreferences("ranking", Context.MODE_PRIVATE))
        }

        fun get() = instance
    }
}

