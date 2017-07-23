package morawski.mvi

import io.reactivex.Observable.just
import morawski.mvi.api.Interactor
import morawski.mvi.api.Profile

class FakeInteractor : Interactor {
    val PAGE_SIZE = 5

    private val names = listOf("Alice", "Bob", "Charlie", "Dan", "Eve")

    override fun search(keyword: String, page: Int, limit: Int) = just(
            thousandProfiles
                    .filter { it.name.contains(keyword, ignoreCase = true) }
                    .drop((page - 1) * PAGE_SIZE)
                    .take(PAGE_SIZE)
                    .toList())!!

    val thousandProfiles : List<Profile> by lazy {
        (1..1000)
                .map {
                    Profile(
                            name = names[(it - 1) % names.size] + " " + ((it / names.size) + 1),
                            title = "",
                            link = "irrelevant")
                }
                .toList()
    }
}