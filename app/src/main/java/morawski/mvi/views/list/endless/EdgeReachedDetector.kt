package morawski.mvi.views.list.endless

import io.reactivex.Observable

interface EdgeReachedDetector {
    /**
     * Events representing each time the list of profiles is scrolled to the edge
     */
    fun listEndReachedEvents() : Observable<Unit>
}