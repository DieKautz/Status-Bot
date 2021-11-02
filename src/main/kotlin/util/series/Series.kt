package util.series

import kotlinx.serialization.Serializable

@Serializable
data class Series(val prizes: List<Int>, val challenges: List<Challenge>)