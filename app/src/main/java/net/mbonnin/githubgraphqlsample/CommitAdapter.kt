package net.mbonnin.githubgraphqlsample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item.*

class CommitAdapter(val commitList: List<GetCommitsQuery.Node>) : RecyclerView.Adapter<CommitAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): Holder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return Holder(itemView)
    }

    override fun getItemCount(): Int {
        return commitList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(commitList[position])
    }


    class Holder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun bind(node: GetCommitsQuery.Node) {
            message.text = node.message()
            author.text = node.author()?.name
            date.text = node.committedDate()
        }
    }
}
