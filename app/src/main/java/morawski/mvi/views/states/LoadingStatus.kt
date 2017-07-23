package morawski.mvi.views.states

enum class LoadingStatus {
    /**
     * It's not loading at the moment and loading is not possible
     * (eg. because the last attempt didn't return any more results = the end has been reached)
     */
    NOT_AND_IMPOSSIBLE,
    /**
     * Nothing is loading at the moment, but further requests for loading more data are possible
     */
    NOT_BUT_POSSIBLE,
    /**
     * It's loading at the moment, executing a new search (no existing results yet)
     */
    YES_FOR_NEW_SEARCH,
    /**
     * It's loading at the moment, trying to fetch more results to add to the existing search (further pages)
     */
    YES_FOR_MORE_ITEMS
}