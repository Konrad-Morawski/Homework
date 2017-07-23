package morawski.mvi

import android.os.Bundle
import android.support.test.runner.AndroidJUnit4
import morawski.mvi.api.Profile
import morawski.mvi.views.states.LoadingStatus
import morawski.mvi.views.states.ViewState
import morawski.mvi.views.states.addTo
import morawski.mvi.views.states.toViewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewStateSerializationTest {
    val sampleProfiles = listOf(
            Profile("name 1", "title 1", "www.link1.com"),
            Profile("name 2", "title 2", "www.link2.com"))

    @Test
    fun givenBundle_toWhichViewStateWasNeverPersisted_retrievalAttemptReturnsNull() {
        val bundleToWhichViewStateIsNeverPersisted = Bundle()
        bundleToWhichViewStateIsNeverPersisted.putString("irrelevant", "whatever")

        val retrieved = bundleToWhichViewStateIsNeverPersisted.toViewState()

        assertNull(retrieved)
    }

    @Test
    fun givenViewState_savedToAndRetrievedFromBundle_retrievedInstanceIsIdentical() {
        val original = ViewState(
                searchPhrase = "name",
                loadingStatus = LoadingStatus.NOT_BUT_POSSIBLE,
                profiles = sampleProfiles)

        val bundle = Bundle()
        original.addTo(bundle)
        val retrieved = bundle.toViewState()

        assertNotNull(retrieved)
        assertEquals(original.searchPhrase, retrieved!!.searchPhrase)
        assertEquals(original.loadingStatus, retrieved.loadingStatus)
        assertEquals(original.profiles, retrieved.profiles)
    }

    @Test
    fun givenLoadingViewState_savedToAndRetrievedFromBundle_retrievedInstanceHasDisabledLoadingStatus() {
        val original = ViewState(
                searchPhrase = "name",
                loadingStatus = LoadingStatus.YES_FOR_MORE_ITEMS,
                profiles = sampleProfiles)

        val bundle = Bundle()
        original.addTo(bundle)
        val retrieved = bundle.toViewState()

        assertNotNull(retrieved)
        assertEquals(LoadingStatus.NOT_BUT_POSSIBLE, retrieved!!.loadingStatus)
    }
}