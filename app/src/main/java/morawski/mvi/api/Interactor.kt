package morawski.mvi.api

import io.reactivex.Observable

interface Interactor {
    fun search(keyword: String, page: Int, limit: Int): Observable<List<Profile>>
}