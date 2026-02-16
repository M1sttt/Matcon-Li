/* this is a file for convetions and code samples use it as guide, it is not related to the projects content*/
/*package com.idz.colman2026class2.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.idz.colman2026class2.base.MyApplication

@Entity
data class Student(

    @PrimaryKey
    val id: String,

    val name: String,
    var isPresent: Boolean,
    val avatarUrlString: String?,
    val lastUpdated: Long?
) {

    companion object {

        var lastUpdated: Long
            get() {
                return MyApplication.Globals.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.getLong(LAST_UPDATED_KEY, 0) ?: 0
            }
            set(value) {
                MyApplication.Globals.appContext
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                    ?.edit()
                    ?.putLong(LAST_UPDATED_KEY, value)
                    ?.apply()
            }

        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        const val IS_PRESENT_KEY = "isPresent"
        const val AVATAR_URL_STRING_KEY = "avatarUrlString"
        const val LAST_UPDATED_KEY = "lastUpdated"

        fun fromJson(json: Map<String, Any?>): Student {
            val id = json[ID_KEY] as String
            val name = json[NAME_KEY] as String
            val isPresent = json[IS_PRESENT_KEY] as Boolean
            val avatarUrlString = json[AVATAR_URL_STRING_KEY] as String?
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return Student(
                id = id,
                name = name,
                isPresent = isPresent,
                avatarUrlString = avatarUrlString,
                lastUpdated = lastUpdatedLong
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            ID_KEY to id,
            NAME_KEY to name,
            IS_PRESENT_KEY to isPresent,
            AVATAR_URL_STRING_KEY to avatarUrlString,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
}*/