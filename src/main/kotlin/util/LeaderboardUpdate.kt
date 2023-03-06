package util

import orm.LeaderboardEntry

data class LeaderboardUpdate(
    val entry: LeaderboardEntry,
    val oldRank: Int? = null
) {
    override fun toString(): String {
        val now = "now **${entry.rank}**"
        if (oldRank == null) {
            return now
        }
        return "from **${oldRank}** -> $now"
    }
}
