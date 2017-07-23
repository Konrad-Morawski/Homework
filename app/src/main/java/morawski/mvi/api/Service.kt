package morawski.mvi.api

import io.reactivex.Observable
import morawski.mvi.BuildConfig
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Represents the Buddyschool API (available under [BuildConfig.ENDPOINT])
 */
interface Service {
    companion object {
        val ENDPOINT = BuildConfig.ENDPOINT
    }

    @GET("search")
    fun search(
            @Query("keyword") keyword: String,
            @Query("limit") limit: Int,
            @Query("page") page: Int): Observable<ResponseBody>
}