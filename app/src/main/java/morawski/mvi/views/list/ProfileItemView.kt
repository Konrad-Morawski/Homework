package morawski.mvi.views.list

import android.content.Context
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.profile_item.view.*
import morawski.mvi.BuildConfig
import morawski.mvi.R
import morawski.mvi.api.Profile

/**
 * Custom layout representing a teacher profile.
 * Use [bind] to populate its UI with model data.
 *
 * @see [Profile]
 * @see [R.layout.profile_item]
 */
class ProfileItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        RelativeLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.profile_item, this)
    }

    fun bind(model: ProfileItem) = model.let {
        txt_ordinal_number.text = "#${it.ordinal}"

        it.profile.let {
            txt_text.text = it.title

            val title = SpannableString(Html.fromHtml(toHtmlLink(it)))
            txt_title.setText(title, TextView.BufferType.SPANNABLE)
            txt_title.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    /* the layout knowing the endpoint is dubious, but it's just for simplicity's sake.
     * in a real (larger) app I'd refactor this out of here. */
    private fun toHtmlLink(model: Profile) = "<a href=\"${BuildConfig.ENDPOINT + model.link}\">${model.name}</a>"
}