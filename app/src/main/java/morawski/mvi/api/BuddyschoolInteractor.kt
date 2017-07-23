package morawski.mvi.api

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import morawski.mvi.logDebug
import morawski.mvi.inMethod
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

/**
 * Connects to the API and fetches results parsed and converted to model instances.
 *
 * Handles threading.
 *
 * @param service raw API representation.
 * Optional: by default it's constructed by Retrofit.
 * Can be replaced with a mock for testing purposes.
 *
 * @see [Profile]
 */
class BuddyschoolInteractor(
        private val service: Service = Retrofit.Builder()
                .baseUrl(Service.ENDPOINT + "/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okhttp3.OkHttpClient())
                .build()
                .create(Service::class.java)) : Interactor {

    @Synchronized
    override fun search(keyword: String, page: Int, limit: Int): Observable<List<Profile>> {
        logDebug(inMethod("search")) { "About to perform search (keyword = '$keyword', limit = $limit, page = $page)" }

        return service
                .search(keyword, limit, page)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .compose(HtmlToProfileTransformer())
    }
}