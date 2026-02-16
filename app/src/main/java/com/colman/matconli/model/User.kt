package com.colman.matconli.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colman.matconli.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val lastUpdated: Long?
) {

    companion object {

        private const val LOCAL_LAST_UPDATED = "local_user_last_update"

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
        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val AVATAR_URL_KEY = "avatarUrl"
        const val LAST_UPDATED_KEY = "lastUpdated"

        fun fromJson(json: Map<String, Any?>): User {
            val id = json[ID_KEY] as String
            val name = json[NAME_KEY] as String
            val email = json[EMAIL_KEY] as String
            val avatarUrl = json[AVATAR_URL_KEY] as String?
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return User(
                id = id,
                name = name,
                email = email,
                avatarUrl = avatarUrl,
                lastUpdated = lastUpdatedLong
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            NAME_KEY to name,
            EMAIL_KEY to email,
            AVATAR_URL_KEY to avatarUrl,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}