package command.util.series

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Challenge(val date: Instant, val badges: Badge)