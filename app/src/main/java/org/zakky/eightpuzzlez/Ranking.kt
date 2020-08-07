package org.zakky.eightpuzzlez

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RankingHolder(
    val ranking: List<Ranking>,
)

@JsonClass(generateAdapter = true)
data class Ranking(
    @Json(name = "move_count")
    val moveCount: Int,
    val time: Long,
)
