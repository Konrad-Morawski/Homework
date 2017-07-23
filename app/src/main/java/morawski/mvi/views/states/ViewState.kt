package morawski.mvi.views.states

import morawski.mvi.api.Profile
import morawski.mvi.toAbbreviatedString
import morawski.mvi.views.list.ListItem
import morawski.mvi.views.list.LoadingMoreItem
import morawski.mvi.views.list.NoMoreResultsItem
import morawski.mvi.views.list.ProfileItem

/**
 * View state able to represent all possible layout states on the main screen.
 *
 * This approach to Android architecture is based on the series:
 * http://hannesdorfmann.com/android/mosby3-mvi-1 (1 to 6)
 *
 * The assumptions are:
 *
 * 1. View state doesn't only contain retrieved data, but a complete definition of the layout state
 * (including "dynamic" aspects such as visibility of loading indicator)
 *
 * 2. ViewState is complete and self-contained - it is possible to fully recreate the layout
 * based on a single instance. It is not just an "update" to a state fully living in the view layer.
 *
 * 3. The role of updates is played by partial view states ([PartialViewState]).
 * The update takes place in the business logic layer.
 * See http://hannesdorfmann.com/android/mosby3-mvi-3 / state reducer pattern / [io.reactivex.Observable.scan]
 *
 * 4. All view states are immutable.
 *
 * @see [PartialViewState]
 * @see [ErrorViewState]
 * @see [CancelledSearchState]
 * @see [LoadingFreshSearchState]
 * @see [LoadingMoreResultsState]
 * @see [RetrievedFreshResultsState]
 * @see [RetrievedMoreResultsState]
 * @see [EndResultsReachedState]
 */
data class ViewState(
        val searchPhrase: String,
        val loadingStatus: LoadingStatus,
        val profiles: List<Profile> = listOf(),
        val error: Throwable? = null) {
    /**
     * There are no results and we're not expecting any to come in (it's not loading at the moment)
     */
    val hasNoResults = profiles.isEmpty()
            && (loadingStatus == LoadingStatus.NOT_AND_IMPOSSIBLE || loadingStatus == LoadingStatus.NOT_BUT_POSSIBLE)
            && searchPhrase.isNotEmpty()

    /**
     * Are there results ready for display?
     */
    val hasResults = profiles.isNotEmpty()
            /* if a new search is on its way, the state shouldn't contain any profiles anyway,
             * but if there were any, they would be stale (leftovers of previous search) */
            && loadingStatus != LoadingStatus.YES_FOR_NEW_SEARCH

    /**
     * Converts profiles to a list displayable in the UI,
     * possibly appending an extra item to reflect the status of the operation
     * ("no more results" or a spinner indicating more items are being loaded) */
    val toListItems: List<ListItem>
        get() = profiles
                .mapIndexed { index, profile -> ProfileItem(profile, index + 1) }
                .apply {
                    return when {
                        loadingStatus == LoadingStatus.YES_FOR_MORE_ITEMS -> this + LoadingMoreItem()
                        loadingStatus == LoadingStatus.NOT_AND_IMPOSSIBLE && profiles.isNotEmpty() -> this + NoMoreResultsItem()
                        else -> this
                    }
                }
                .toList<ListItem>()

    /**
     * Returns an updated (immutable) version of the view.
     * The partial view gets "merged" or folded into current state, producing a new one.
     * Reducer / state machine pattern.
     *
     * @param partial partial view state whose type and contents are used
     * to produce an updated version of the full state
     */
    operator fun plus(partial: PartialViewState) = ViewState(
            searchPhrase = partial.searchPhrase,
            loadingStatus = when (partial) {
                is CancelledSearchState -> LoadingStatus.NOT_BUT_POSSIBLE
                is LoadingFreshSearchState -> LoadingStatus.YES_FOR_NEW_SEARCH
                is LoadingMoreResultsState -> LoadingStatus.YES_FOR_MORE_ITEMS
                is EndResultsReachedState -> LoadingStatus.NOT_AND_IMPOSSIBLE
                is ErrorViewState -> LoadingStatus.NOT_AND_IMPOSSIBLE
                else -> LoadingStatus.NOT_BUT_POSSIBLE
            },
            profiles = when (partial) {
                is CancelledSearchState -> emptyList()
                is RetrievedMoreResultsState -> this.profiles + partial.profiles
                is RetrievedFreshResultsState -> partial.profiles
                is ErrorViewState -> emptyList()
                is EndResultsReachedState ->
                    when {
                        // it could be a brand new search that had no results
                        partial.searchPhrase != this.searchPhrase -> emptyList()
                        else -> this.profiles
                    }
                else -> this.profiles
            },
            error = (partial as? ErrorViewState)?.error)

    override fun toString() = "ViewState(searchPhrase='$searchPhrase', loadingStatus=$loadingStatus, profiles=${profiles.toAbbreviatedString()}, error=$error)"
}