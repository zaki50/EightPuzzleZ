package org.zakky.eightpuzzlez

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RankingRepository @VisibleForTesting constructor(
    private val pref: SharedPreferences,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val rankingHolderAdapter: JsonAdapter<RankingHolder> =
        Moshi.Builder().build().adapter(RankingHolder::class.java)

    suspend fun add(ranking: Ranking) {
        withContext(dispatcher) {
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
    }

    suspend fun getRankingList(): MutableList<Ranking> {
        return withContext(dispatcher) {
            pref.getString(KEY_RANKING, "").let {
                if (it.isNullOrEmpty()) {
                    mutableListOf()
                } else {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    rankingHolderAdapter.fromJson(it)?.ranking?.toMutableList()
                        ?: mutableListOf()
                }
            }
        }
    }

    suspend fun clearRanking() {
        withContext(dispatcher) {
            pref.edit().clear().apply()
        }
    }

    suspend fun hasRankingData(): Boolean {
        return withContext(dispatcher) {
            pref.contains(KEY_RANKING)
        }
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

