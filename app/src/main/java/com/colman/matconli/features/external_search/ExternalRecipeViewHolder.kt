package com.colman.matconli.features.external_search

import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.R
import com.colman.matconli.databinding.ItemExternalRecipeBinding
import com.colman.matconli.model.ExternalRecipe
import com.squareup.picasso.Picasso

class ExternalRecipeViewHolder(
    private val binding: ItemExternalRecipeBinding
) : RecyclerView.ViewHolder(binding.root) {

    var listener: ExternalRecipeAdapter.OnItemClickListener? = null

    fun bind(recipe: ExternalRecipe?) {
        recipe?.let {
            binding.itemExternalRecipeTextViewTitle.text = it.title
            if (!it.imageUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .error(R.drawable.ic_recipe_placeholder)
                    .into(binding.itemExternalRecipeImageView)
            } else {
                binding.itemExternalRecipeImageView.setImageResource(R.drawable.ic_recipe_placeholder)
            }

            binding.root.setOnClickListener { _ ->
                listener?.onItemClick(it)
            }
        }
    }
}
