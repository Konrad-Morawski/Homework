package morawski.mvi.api

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import morawski.mvi.logDebug
import morawski.mvi.inMethod
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Converts raw HTML returned by the Buddyschool webpage ([morawski.mvi.BuildConfig.ENDPOINT])
 * to a list of [Profile] instances found in it
 */
class HtmlToProfileTransformer : ObservableTransformer<ResponseBody, List<Profile>> {
    /**
     * The div class where [Profile.name] can be found in the HTML
     */
    private val NICK_CLASS = "profileMainNick"

    /**
     * The div class where [Profile.title] and [Profile.link] can be found in the HTML
     */
    private val MAIN_TITLE_CLASS = "profileMainTitle"

    override fun apply(upstream: Observable<ResponseBody>) = upstream
            .doOnNext { logDebug(inMethod("apply")) { "Received HTML response (${it.contentType()})" } }
            .map { it.toDocument() }
            .doOnNext { logDebug(inMethod("apply")) { "Parsed response to an HTML document" } }
            .map { it.toProfiles() }!!
            .doOnNext { logDebug(inMethod("apply")) { "Parsed HTML document to ${it.size} result(s)" } }

    /**
     * Parses a response containing raw HTML to a queryable [Document] instance
     */
    private fun ResponseBody.toDocument() = Jsoup.parse(string())

    /**
     * Parses an HTML document to a list of Profiles found in it
     */
    private fun Document.toProfiles(): List<Profile> = select("." + NICK_CLASS)
            .zip(select("." + MAIN_TITLE_CLASS).map {
                mainElement -> mainElement.text() to mainElement.select("a[href]").attr("href")
            })
            .map {(nick, main) ->
                val (title, link) = main
                Profile(name = nick.text(), title = title, link = link)
            }
            .toList()
}