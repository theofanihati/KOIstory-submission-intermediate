package com.example.koistory.ui.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.example.koistory.R
import com.example.koistory.data.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

internal class StackRemoteViewsFactory(
    private val mContext: Context,
    private val repository: UserRepository
) : RemoteViewsService.RemoteViewsFactory {
    private val mWidgetItems = ArrayList<Pair<String, String>>()

    override fun onCreate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getStories()
                val stories = response.listStory
                Log.d("StackRemoteViewsFactory", "Received ${stories.size} stories")

                if (stories.isNotEmpty()) {
                    mWidgetItems.clear()
                    for (story in stories) {
                        try {
                            val name = story.name ?: "No Name"
                            val description = story.description ?: "No Description"
                            mWidgetItems.add(Pair(name, description))
                            Log.d("StackRemoteViewsFactory", "Added item: $name, $description")
                        } catch (e: Exception) {
                            Log.e("StackRemoteViewsFactory", "Error loading image: ${e.message}")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Log.d("StackRemoteViewsFactory", "onDataSetChanged called")
                        onDataSetChanged()
                    }

                } else {
                    Log.e("StackRemoteViewsFactory", "No stories found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onDataSetChanged() {
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int {
        Log.d("StackRemoteViewsFactory", "Item count: ${mWidgetItems.size}")
        return mWidgetItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val item = mWidgetItems[position]
        Log.d("StackRemoteViewsFactory", "Getting view for position: $position, item: $item")
        val remoteViews = RemoteViews(mContext.packageName, R.layout.item_widget)
        remoteViews.setTextViewText(R.id.tv_widget_name, item.first)
        remoteViews.setTextViewText(R.id.tv_description, item.second)

        val extras = bundleOf(
            KoiWidget.EXTRA_ITEM to position
        )

        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}