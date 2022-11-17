package util.series

import kotlinx.serialization.Serializable


/*
    the prizes attribute is currently not parsed as kotlinx.serialization cant handle arrays of mixed types
    val prizes: List<Int>,
 */
@Serializable
data class Series(val challenges: List<Challenge>)