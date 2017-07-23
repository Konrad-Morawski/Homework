package morawski.mvi.views.list.endless

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import morawski.mvi.inMethod
import morawski.mvi.logDebug

/**
 * Detects whether scrolling gets near to the bottom edge,
 * and triggers a signal that requests loading another page of results
 * (endless scrolling UX pattern).
 *
 * Disable this behaviour by setting [AutoloadScrollListener.enableAutoloading]
 * to false once new results are no longer retrieved (we got to the end).
 *
 * @param distanceTrigger how far from the end of the list (counted in items)
 * is requesting triggered?
 * 0 means only when the list is scrolled all the way down.
 * Can't be negative.
 */
class AutoloadScrollListener(private val distanceTrigger: Int = 1)
    : RecyclerView.OnScrollListener(), EdgeReachedDetector {
    init {
        require(distanceTrigger >= 0)
    }

    private val listEndReachedEvents = PublishSubject.create<Unit>()

    private var autoloading = true
    internal var enableAutoloading
        get() = autoloading
        set(value) {
            logDebug(inMethod("enableAutoloading")) { "Setting enableAutoloading to $value" }
            autoloading = value
        }

    /**
     * Events representing each time the list of profiles is scrolled to the edge
     * (indicating more results should be loaded).
     */
    override fun listEndReachedEvents(): Observable<Unit> = listEndReachedEvents
            .filter { enableAutoloading }
            .doOnNext { logDebug(inMethod("listEndReachedEvents")) { "Detected reaching end of the list" } }
            .startWith(Unit /* this initial "fake" signal triggers the first API request */)
            .hide()

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (!autoloading) {
            return
        }
        with (recyclerView.layoutManager as LinearLayoutManager) {
            val allItems = itemCount
            val passedVisibleItems = findFirstVisibleItemPosition()
            val visibleItems = childCount
            val leftToTheEdge = allItems - passedVisibleItems - visibleItems

            if (leftToTheEdge <= distanceTrigger) {
                listEndReachedEvents.onNext(Unit)
            }
        }
    }
}