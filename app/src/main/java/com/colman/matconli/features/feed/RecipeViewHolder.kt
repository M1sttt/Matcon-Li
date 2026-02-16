package com.colman.matconli.features.feed

import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.R
import com.colman.matconli.databinding.ItemRecipeBinding
import com.colman.matconli.model.Recipe
import com.squareup.picasso.Picasso

class RecipeViewHolder(
    private val binding: ItemRecipeBinding
) : RecyclerView.ViewHolder(binding.root) {

    var listener: RecipeAdapter.OnItemClickListener? = null

    fun bind(recipe: Recipe?) {
        recipe?.let {
            binding.itemRecipeTextViewTitle.text = it.title
            if (!it.imageUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .error(R.drawable.ic_recipe_placeholder)
                    .into(binding.itemRecipeImageView)
            } else {
                binding.itemRecipeImageView.setImageResource(R.drawable.ic_recipe_placeholder)
            }

            binding.root.setOnClickListener { _ ->
                listener?.onItemClick(it)
            }
        }
    }
}
