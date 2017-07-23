package morawski.mvi

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import morawski.mvi.api.Interactor
import morawski.mvi.views.MainView
import morawski.mvi.views.states.LoadingStatus
import morawski.mvi.views.states.ViewState
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * There is no comprehensive unit test coverage in the app as it's a sample program
 * and unit testing is repetitive work.
 * These few tests demonstrate the technique.
 */
class MainPresenterTest {
    val immediateScheduler = Schedulers.trampoline()

    lateinit var simulatedSearches: PublishSubject<String>
    lateinit var simulatedScrolledToEndEvents: PublishSubject<Unit>
    lateinit var view: MainView
    lateinit var fakeInteractor: Interactor

    @Test
    fun `when search never performed - view receives one status with no profiles and not loading`() {
        val captor = argumentCaptor<ViewState>()

        verify(view, times(1)).render(captor.capture())
        with (captor.firstValue) {
            mustHaveProfiles(0)
            mustHaveStatus(LoadingStatus.NOT_AND_IMPOSSIBLE)
        }
    }

    @Test
    fun `when search for Alice performed - view receives consecutive updates with correct results at the end`() {
        simulatedSearches.onNext("alice")

        val captor = argumentCaptor<ViewState>()

        verify(view, times(3)).render(captor.capture())
        with (captor.firstValue) {
            mustHaveStatus(LoadingStatus.NOT_AND_IMPOSSIBLE)
            mustHaveProfiles(0)
        }
        with (captor.secondValue) {
            mustHaveStatus(LoadingStatus.YES_FOR_NEW_SEARCH)
            mustHaveProfiles(0) // but no results yet...
        }
        with (captor.thirdValue) {
            mustHaveStatus(LoadingStatus.NOT_BUT_POSSIBLE)
            mustHaveProfiles(
                    "Alice 1",
                    "Alice 2",
                    "Alice 3",
                    "Alice 4",
                    "Alice 5")
        }
    }

    @Test
    fun `when search for Alice performed - and list scrolled to end - view receives an update with second page of results`() {
        simulatedSearches.onNext("alice")
        simulatedScrolledToEndEvents.onNext(Unit) // and list scrolled to the end...
        val captor = argumentCaptor<ViewState>()

        verify(view, times(5)).render(captor.capture())

        with (captor.allValues[3]) {
            // first page of results is in, but loading another one...
            mustHaveProfiles(5)
            mustHaveStatus(LoadingStatus.YES_FOR_MORE_ITEMS)
        }

        with (captor.allValues[4]) {
            // second page of results added
            mustHaveProfiles(
                    "Alice 1",
                    "Alice 2",
                    "Alice 3",
                    "Alice 4",
                    "Alice 5",
                    // page 2 of results
                    "Alice 6",
                    "Alice 7",
                    "Alice 8",
                    "Alice 9",
                    "Alice 10")
            mustHaveStatus(LoadingStatus.NOT_BUT_POSSIBLE)
        }
    }

    @Before
    fun setUp() {
        overrideSchedulers()
        initializeMocks()
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }

    /**
     * Overriding schedulers is necessary, as tested classes operate on specific threads.
     * they need to be substituted with the current thread
     * in order for tests to execute synchronously. */
    fun overrideSchedulers() {
        RxJavaPlugins.setComputationSchedulerHandler { _ -> immediateScheduler }
        RxJavaPlugins.setIoSchedulerHandler { _ -> immediateScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { _ -> immediateScheduler }
    }

    fun initializeMocks() {
        simulatedSearches = PublishSubject.create<String>()
        simulatedScrolledToEndEvents = PublishSubject.create<Unit>()
        view = mock {
            on { searchEvents() } doReturn simulatedSearches
            on { listEndReachedEvents() } doReturn simulatedScrolledToEndEvents.startWith(Unit)
        }

        fakeInteractor = FakeInteractor()
        val presenter = MainPresenter(fakeInteractor)
        presenter.attachView(view)
    }

    fun ViewState.mustHaveProfiles(expectedCount: Int) = assertEquals(expectedCount, profiles.size)

    fun ViewState.mustHaveProfiles(vararg expectedNames: String) {
        mustHaveProfiles(expectedNames.size)
        expectedNames
                .zip(profiles)
                .forEach { (expectedName, actualProfile) -> assertEquals(expectedName, actualProfile.name) }
    }

    fun ViewState.mustHaveStatus(expectedStatus: LoadingStatus) = assertEquals(expectedStatus, loadingStatus)
}