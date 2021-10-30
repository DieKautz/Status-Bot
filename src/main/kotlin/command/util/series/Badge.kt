package command.util.series

import kotlinx.serialization.Serializable

@Serializable
data class Badge(val main: String, val mono: String? = null)