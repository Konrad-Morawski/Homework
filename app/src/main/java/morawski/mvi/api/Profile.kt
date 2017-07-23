package morawski.mvi.api

import java.io.Serializable

data class Profile(val name: String, val title: String, val link: String) : Serializable