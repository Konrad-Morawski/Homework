package morawski.mvi.views.list

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import morawski.mvi.R
import morawski.mvi.inMethod
import morawski.mvi.logDebug
import morawski.mvi.toAbbreviatedString
import morawski.mvi.views.list.endless.AutoloadScrollListener
import morawski.mvi.views.list.endless.EdgeReachedDetector

class ProfilesAdapter(
        private val autoloadScrollListener: AutoloadScrollListener = AutoloadScrollListener(),
        private val context: Context,
        private var items: MutableList<ListItem> = mutableListOf())
    : RecyclerView.Adapter<ProfilesAdapter.Holder>(), EdgeReachedDetector by autoloadScrollListener {

    fun setOn(recyclerView: RecyclerView) = recyclerView.let {
        it.adapter = this
        it.addOnScrollListener(autoloadScrollListener)
    }

    var enableAutoloading: Boolean
        get() = autoloadScrollListener.enableAutoloading
        set(value) { autoloadScrollListener.enableAutoloading = value }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ListItem>) {
        logDebug(inMethod("updateItems")) { "Updating adapter with ${newItems.toAbbreviatedString()}" }

        this.items.let {
            DiffUtil
                    .calculateDiff(DiffUtilCallback(it, newItems))
                    .dispatchUpdatesTo(this)
            it.clear()
            it.addAll(newItems)
        }
    }

    override fun getItemViewType(position: Int) = items[position].getViewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
            when (viewType) {
                ProfileItem.VIEW_TYPE -> ProfileItemView(context)
                LoadingMoreItem.VIEW_TYPE -> parent.inflateWith(R.layout.loading_more_item)
                NoMoreResultsItem.VIEW_TYPE -> parent.inflateWith(R.layout.no_more_results_item)
                else -> error("Unrecognized viewType: $viewType")
            })

    override fun onBindViewHolder(holder: Holder, position: Int) {
        items[position]
                .takeIf {
                    it is ProfileItem
                    /* all other view types are static layouts
                     * and don't need to be populated with custom contents */
                }
                ?.let {
                    (holder.itemView as? ProfileItemView)?.bind(it as ProfileItem)
                }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffUtilCallback(
            private val oldList: List<ListItem>,
            private val newList: List<ListItem>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = areItemsTheSame(oldItemPosition, newItemPosition)
    }

    private fun ViewGroup.inflateWith(@LayoutRes layoutId: Int) =
            LayoutInflater.from(this.context).inflate(layoutId, this, false)
}