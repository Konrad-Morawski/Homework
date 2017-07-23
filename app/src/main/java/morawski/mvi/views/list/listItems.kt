package morawski.mvi.views.list

import morawski.mvi.api.Profile

/**
 * Classes representing profile items (and other types of items) as displayed in the list
 */

sealed class ListItem : IdentifiableViewType

/**
 * Teacher profile (the most basic and common view type)
 *
 * @property profile profile data
 * @property ordinal ordinal number of the profile within the list
 */
data class ProfileItem(val profile: Profile, val ordinal: Int): ListItem(), IdentifiableViewType {
    companion object {
        val VIEW_TYPE = 0
    }

    override fun toString() = "ProfileItem(profile=$profile)"

    override val getViewType: Int
        get() = VIEW_TYPE
}

/**
 * Loading indicator appended at the end of the list to inform the user
 * more results are being loaded
 */
class LoadingMoreItem : ListItem(), IdentifiableViewType {
    companion object {
        val VIEW_TYPE = 1
    }

    override fun toString() = "LoadingMoreItem"

    override val getViewType: Int
        get() = VIEW_TYPE
}

/**
 * Information appended at the end of the list to inform the user
 * the last request returned no results and therefore all the results
 * available for the given search phrase have been fetched already.
 */
class NoMoreResultsItem : ListItem(), IdentifiableViewType {
    companion object {
        val VIEW_TYPE = 2
    }

    override fun toString() = "NoMoreResultsItem"

    override val getViewType: Int
        get() = VIEW_TYPE
}

interface IdentifiableViewType {
    val getViewType: Int
}