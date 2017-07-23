package morawski.mvi.views

import io.reactivex.Observable
import morawski.mvi.views.list.endless.EdgeReachedDetector
import morawski.mvi.views.states.ViewState

interface MainView : EdgeReachedDetector {
    /**
     * Sequence of events representing queries submitted by the user.
     * The values are search phrases.
     */
    fun searchEvents() : Observable<String>

    /**
     * Events representing each time the list of profiles is scrolled to the edge
     * (indicating more results should be loaded).
     */
    override fun listEndReachedEvents() : Observable<Unit>

    /**
     * Updates the View with data in a [ViewState] instance
     */
    fun render(viewState: ViewState)
}