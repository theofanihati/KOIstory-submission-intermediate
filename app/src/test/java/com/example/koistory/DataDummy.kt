package com.example.koistory

import com.example.koistory.data.response.ListStoryItem
import kotlin.random.Random

object DataDummy {
    fun generateDummyStoryResponse() : List<ListStoryItem> {
        val items : MutableList<ListStoryItem> = arrayListOf()

        for (i in 0..100) {
            val story = ListStoryItem(
                id = Random.nextInt(1000, 9999).toString(),
                name = "Story Name $i",
                description = "Description for story $i",
                photoUrl = "https://example.com/photo_$i.jpg",
                createdAt = "2024-12-18T12:00:00Z",
                lat = Random.nextDouble(-90.0, 90.0),
                lon = Random.nextDouble(-180.0, 180.0)
            )
            items.add(story)
        }
        return items
    }
}