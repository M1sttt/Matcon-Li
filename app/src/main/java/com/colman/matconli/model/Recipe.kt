package com.colman.matconli.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colman.matconli.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class Recipe(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val ownerId: String,
    val lastUpdated: Long?
) {

    companion object {

        private const val LOCAL_LAST_UPDATED = "local_recipes_last_update"

        var lastUpdated: Long
            get() {
                return MyApplication.Globals.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.getLong(LOCAL_LAST_UPDATED, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.edit()
                    ?.putLong(LOCAL_LAST_UPDATED, value)
                    ?.apply()
            }

        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"
        const val IMAGE_URL_KEY = "imageUrl"
        const val OWNER_ID_KEY = "ownerId"
        const val LAST_UPDATED_KEY = "lastUpdated"

        fun fromJson(json: Map<String, Any?>): Recipe {
            val id = json[ID_KEY] as String
            val title = json[TITLE_KEY] as String
            val description = json[DESCRIPTION_KEY] as String
            val imageUrl = json[IMAGE_URL_KEY] as String?
            val ownerId = json[OWNER_ID_KEY] as String
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return Recipe(
                id = id,
                title = title,
                description = description,
                imageUrl = imageUrl,
                ownerId = ownerId,
                lastUpdated = lastUpdatedLong
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            TITLE_KEY to title,
            DESCRIPTION_KEY to description,
            IMAGE_URL_KEY to imageUrl,
            OWNER_ID_KEY to ownerId,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}