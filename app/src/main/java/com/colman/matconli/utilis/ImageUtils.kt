package com.colman.matconli.utilis

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso

object ImageUtils {

    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        @DrawableRes placeholderRes: Int
    ) {
        if (imageUrl.isNullOrBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        Picasso.get()
            .load(imageUrl)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .into(imageView)
    }
}

