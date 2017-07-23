package morawski.mvi.views.states

import android.os.Bundle
import morawski.mvi.api.Profile
import morawski.mvi.logDebug
import morawski.mvi.logError
import morawski.mvi.inMethod
import morawski.mvi.toAbbreviatedString

private val SEARCH_PHRASE_KEY = "dd"
private val ERROR_KEY = "error"
private val LOADING_STATUS_KEY = "loadingStatus"
private val PROFILE_NAMES_KEY = "names"
private val PROFILE_LINKS_KEY = "links"
private val PROFILE_TITLES_KEY = "titles"

/**
 * Persists [ViewState] instance in the given [Bundle] object
 */
fun ViewState.addTo(bundle: Bundle) {
    logDebug(inMethod("addTo")) { "Serializing view state\n$this\nto a Bundle" }
    with (bundle) {
        putString(SEARCH_PHRASE_KEY, searchPhrase)
        putSerializable(ERROR_KEY, error)
        putSerializable(LOADING_STATUS_KEY, loadingStatus)

        putStringArrayList(PROFILE_NAMES_KEY, ArrayList(profiles.map { it.name }))
        putStringArrayList(PROFILE_LINKS_KEY, ArrayList(profiles.map { it.link }))
        putStringArrayList(PROFILE_TITLES_KEY, ArrayList(profiles.map { it.title }))
    }
}

/**
 * Tries to retrieve [ViewState] instance from a given [Bundle]
 *
 * @throws [IllegalStateException] if the object was partially present in the [Bundle],
 * but what was found couldn't be used to restore a valid [ViewState] object
 *
 * @return [ViewState] instance if deserialization was possible,
 * null if relevant data wasn't found in the [Bundle]
 */
fun Bundle.toViewState() : ViewState? {
    val searchPhrase = getString(SEARCH_PHRASE_KEY) ?: return null
    val loadingStatus = (getSerializable(LOADING_STATUS_KEY) as? LoadingStatus) ?: return null
    val names = getStringArrayList(PROFILE_NAMES_KEY) ?: return null
    val links = getStringArrayList(PROFILE_LINKS_KEY) ?: return null
    val titles = getStringArrayList(PROFILE_TITLES_KEY) ?: return null
    val error = getSerializable(ERROR_KEY) as? Error // optional, so it's OK if it's not there

    if (names.size != titles.size || names.size != links.size) {
        val message = "Deserialization failed: arrays aren't of equal length: ${names.toAbbreviatedString()}, ${titles.toAbbreviatedString()}, ${links.toAbbreviatedString()}"
        logError(inMethod("toViewState")) { "$message - about to crash" }
        error(message)
    }

    val profiles = (0 until names.size)
            .map { Profile(name = names[it], title = titles[it], link = links[it]) }
            .toList()

    val deserialized = ViewState(
            searchPhrase = searchPhrase,
            loadingStatus = when (loadingStatus) {
                /* we don't persist active loading status. any ongoing operations are considered dead.
                 * obviously it's possible to handle this scenario with a bit more work */
                LoadingStatus.YES_FOR_MORE_ITEMS,
                LoadingStatus.YES_FOR_NEW_SEARCH -> LoadingStatus.NOT_BUT_POSSIBLE
                else -> loadingStatus // other statuses are good as they are
            },
            profiles = profiles,
            error = error)

    logDebug(inMethod("toViewState")) { "Deserialized state:\n$deserialized\nfrom a Bundle" }
    return deserialized
}