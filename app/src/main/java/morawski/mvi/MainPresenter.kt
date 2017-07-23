package morawski.mvi

import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import morawski.mvi.api.BuddyschoolInteractor
import morawski.mvi.api.Interactor
import morawski.mvi.views.MainView
import morawski.mvi.views.states.CancelledSearchState
import morawski.mvi.views.states.EndResultsReachedState
import morawski.mvi.views.states.ErrorViewState
import morawski.mvi.views.states.LoadingFreshSearchState
import morawski.mvi.views.states.LoadingMoreResultsState
import morawski.mvi.views.states.LoadingStatus
import morawski.mvi.views.states.RetrievedFreshResultsState
import morawski.mvi.views.states.RetrievedMoreResultsState
import morawski.mvi.views.states.ViewState

/**
 * Binds main view (which displays the results and forwards user input)
 * with an interactor (which connects to the API, fetches and parses the results).
 *
 * @param interactor interactor handling API connections.
 * Optional: default implementation is used if not specified.
 * Can be substituted with a mock for testing purposes..

 * @param initialState optional initial state
 * (eg. if the presenter gets recreated along with its host Activity)
 *
 * @see [MainView]
 */
class MainPresenter(
        private val interactor: Interactor = BuddyschoolInteractor(),
        private val initialState: ViewState? = null) {
    private val DEFAULT_BLANK_STATE = ViewState(
            searchPhrase = "",
            loadingStatus = LoadingStatus.NOT_AND_IMPOSSIBLE)

    /**
     * How many records are requested from the backend at once?
     *
     * The built-in search interface of buddyschool.com supports 10, 20 or 50:
     * other values are disregarded
     */
    private val PAGE_LIMIT = 10

    // BehaviorSubject reemits the last encountered state every time a View is reattached
    private val states = BehaviorSubject.create<ViewState>()
    private var view: MainView? = null
    private var viewSubscription: Disposable? = null

    @Synchronized
    fun attachView(view: MainView) {
        logDebug(inMethod("attachView")) { "Attaching view $view" }

        if (this.view != null) {
            logWarning(inMethod("attachView")) {
                "Two consecutive calls of attachView without a detachView in between - indicative of sloppy implementation!"
            }
            detachView()
        }

        this.view = view
        bindIntentions()
    }

    @Synchronized
    fun detachView() {
        when {
            this.view != null ->
                logDebug(inMethod("detachView")) { "Detaching view (currently attached: ${this.view})" }
            else ->
                logWarning(inMethod("detachView")) { "Two consecutive calls of detachView without an attachView in between - indicative of sloppy implementation!" }
        }
        viewSubscription?.dispose()
        this.view = null
    }

    @Synchronized
    private fun bindIntentions() {
        val here = inMethod("bindIntentions")
        val view = this.view ?: return
        view
                .searchEvents()
                .map(String::trim)
                .distinctUntilChanged()
                .switchMap { searchPhrase -> handleSearch(searchPhrase, view) }
                .doOnNext { logDebug(here) { "Results converted to partial state: $it" } }
                .scan(
                        // use custom initial state if it's been supplied (it could have not)
                        initialState ?: DEFAULT_BLANK_STATE,
                        {
                            full, partial ->
                                logDebug(here) {
                                    "Combining full view state:\n$full\nwith partial:\n$partial\n"
                                }
                            full + partial
                        })
                .doOnNext { logDebug(here) { "Returning new, updated full state:\n$it\n" } }
                .subscribe(states)

        viewSubscription = states
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::render)
    }

    private fun handleSearch(searchPhrase: String, view: MainView) = when {
        searchPhrase.isEmpty() -> just(CancelledSearchState())
        else -> view
                    .listEndReachedEvents()
                    // counting these events to keep track of which page to request values from
                    .map { 1 }
                    .scan { i, _ -> i + 1 }
                    .doOnNext {
                        logDebug("handleSearch") { "Loading page $it of results (for '$searchPhrase') requested" }
                    }
                    .map { page -> performSearch(searchPhrase, page) }
                    .flatMap { it }
    }

    private fun performSearch(searchPhrase: String, page: Int) = interactor
            .search(keyword = searchPhrase, page = page, limit = PAGE_LIMIT)
            .map {
                when {
                    it.isEmpty() -> EndResultsReachedState(searchPhrase)
                    page == 1 -> RetrievedFreshResultsState(searchPhrase, it)
                    else -> RetrievedMoreResultsState(searchPhrase, it)
                }
            }
            .startWith(
                    when {
                        page > 1 -> LoadingMoreResultsState(searchPhrase)
                        else -> LoadingFreshSearchState(searchPhrase)
                    })
            .doOnError {
                logError(inMethod("performSearch")) {
                    "Error encountered while performing search for '$searchPhrase' (page $page): $it!"
                }
            }
            .onErrorReturn { ErrorViewState(searchPhrase, it) }
}