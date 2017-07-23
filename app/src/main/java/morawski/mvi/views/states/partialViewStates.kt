package morawski.mvi.views.states

import morawski.mvi.api.Profile
import morawski.mvi.toAbbreviatedString

/**
 * Objects representing partial view states supported within the application.
 *
 * This approach to Android architecture is based on the series:
 * http://hannesdorfmann.com/android/mosby3-mvi-1 (1 to 6)
 *
 * 1. View states don't only contain retrieved data,
 * but describe state of the layout completely (including "dynamic" aspects such as visibility
 * of loading indicator)
 *
 * 2. [ViewState] objects are complete and self-contained - it is possible to fully recreate the layout
 * based on a single view state. They're not just "updates" to a state fully living in the view layer
 *
 * 3. The role of updates is played by partial view states ([PartialViewState]), defined below.
 * The update takes place in the business logic layer.
 * See http://hannesdorfmann.com/android/mosby3-mvi-3 / state reducer pattern / [io.reactivex.Observable.scan]
 *
 * 4. All view states are immutable.
 *
 * @see [ViewState]
 */

sealed class PartialViewState(val searchPhrase: String)

class ErrorViewState(searchPhrase: String, val error: Throwable) : PartialViewState(searchPhrase) {
    override fun toString() = "ErrorViewState(error=$error)"
}

class CancelledSearchState : PartialViewState("") {
    override fun toString() = "CancelledSearchState"
}

class LoadingFreshSearchState(searchPhrase: String) : PartialViewState(searchPhrase) {
    override fun toString() = "LoadingFreshSearchState(searchPhrase='$searchPhrase')"
}

class LoadingMoreResultsState(searchPhrase: String) : PartialViewState(searchPhrase) {
    override fun toString() = "LoadingMoreResultsState(searchPhrase='$searchPhrase')"
}

class RetrievedFreshResultsState(searchPhrase: String, val profiles: List<Profile>) : PartialViewState(searchPhrase) {
    override fun toString() = "RetrievedFreshResultsState(searchPhrase='$searchPhrase', profiles=${profiles.toAbbreviatedString()})"
}

class RetrievedMoreResultsState(searchPhrase: String, val profiles: List<Profile>) : PartialViewState(searchPhrase) {
    override fun toString() = "RetrievedMoreResultsState(searchPhrase='$searchPhrase', profiles=${profiles.toAbbreviatedString()})"
}

class EndResultsReachedState(searchPhrase: String) : PartialViewState(searchPhrase) {
    override fun toString() = "EndResultsReachedState(searchPhrase='$searchPhrase')"
}