package morawski.mvi.views

import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.list_profiles
import kotlinx.android.synthetic.main.activity_main.loading_indicator
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.activity_main.txt_no_results
import morawski.mvi.MainPresenter
import morawski.mvi.R
import morawski.mvi.inMethod
import morawski.mvi.logDebug
import morawski.mvi.views.list.ProfilesAdapter
import morawski.mvi.views.states.LoadingStatus
import morawski.mvi.views.states.ViewState
import morawski.mvi.views.states.addTo
import morawski.mvi.views.states.toViewState
import java.net.UnknownHostException

class MainActivity : AppCompatActivity(), MainView {
    private var lastState: ViewState? = null
    private lateinit var presenter: MainPresenter
    private lateinit var searchView: SearchView

    private val dismissals = PublishSubject.create<Unit>()
    private val profilesAdapter = ProfilesAdapter(context = this, items = mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        presenter = MainPresenter(initialState = lastState ?: savedInstanceState?.toViewState())

        list_profiles.layoutManager = LinearLayoutManager(this)
        profilesAdapter.setOn(list_profiles)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnCloseListener {
            dismissals.onNext(Unit)
            false
        }

        // if it wasn't for the use of options menu, attachView would be called in onResume
        presenter.attachView(this)
        return true
    }

    override fun onPause() {
        presenter.detachView()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.let { lastState?.addTo(it) }
    }

    /**
     * Updates the layout with data contained in a [ViewState] object
     */
    override fun render(viewState: ViewState) {
        logDebug(inMethod("render")) { "Rendering $viewState" }
        lastState = viewState

        if (isFinishing) {
            /* it MIGHT happen if the view was detached from the presenter
             * while the query was executing already */
            presenter.detachView() // just in case
            return
        }

        with (viewState) {
            if (profiles.isEmpty().not()) {
                profilesAdapter.updateItems(toListItems)
            }

            if (hasNoResults) {
                txt_no_results.text = when {
                    error != null -> createMessageFor(error, searchPhrase)
                    else -> "No results were found for phrase \"$searchPhrase\".\n\nTry something else!"
                }
            }

            list_profiles.showIf(hasResults)
            txt_no_results.showIf(hasNoResults)

            /* LoadingStatus.YES_FOR_MORE_ITEMS uses a smaller loading indicator appended to the list:
             * we don't need to display the full-screen one, as it would obscure the results */
            loading_indicator.showIf(loadingStatus == LoadingStatus.YES_FOR_NEW_SEARCH)

            profilesAdapter.enableAutoloading = loadingStatus == LoadingStatus.NOT_BUT_POSSIBLE
        }
    }

    /**
     * Sequence of events representing queries submitted by the user.
     * The values are search phrases.
     * Dismissing search results (clearing the list) is expressed as an empty search phrase.
     * Blank search phrases (like " ") are ignored.
     */
    override fun searchEvents() = merge(
            dismissals
                    .map { _ -> "" }
                    .doOnNext {
                        logDebug(inMethod("searchEvents")) { "Dismissing search results (by submitting an empty search phrase)" }
                    },
            RxSearchView
                    .queryTextChangeEvents(searchView)
                    .filter { it.isSubmitted && it.queryText().isNotBlank() }
                    .doOnNext { searchView.clearFocus() /* hides the screen keyboard */ }
                    .map { it.queryText().toString() }
                    .doOnNext { logDebug(inMethod("searchEvents"), { "Submitted search phrase '$it'"}) })!!

    /**
     * Events representing each time the list of profiles is scrolled to the edge
     * (indicating more results should be loaded).
     */
    override fun listEndReachedEvents() = profilesAdapter.listEndReachedEvents()

    private fun createMessageFor(error: Throwable, searchPhrase: String) = ("Error encountered while searching for \"$searchPhrase\" :( \n(${error.message})."
            + "\n\n"
            // of course this could be checked for explicitly, there could be automated retry logic etc.
            + (if (error is UnknownHostException) "Isn't the internet down?\n" else "")
            + "Try again later.")
}