package com.colman.matconli.features.external_search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.databinding.ItemExternalRecipeBinding
import com.colman.matconli.model.ExternalRecipe

class ExternalRecipeAdapter : RecyclerView.Adapter<ExternalRecipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recipe: ExternalRecipe)
    }
    var listener: OnItemClickListener? = null
    var recipes: MutableList<ExternalRecipe>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExternalRecipeViewHolder {
        val binding = ItemExternalRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExternalRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExternalRecipeViewHolder, position: Int) {
        holder.listener = listener
        holder.bind(recipes?.get(position))
    }

    override fun getItemCount(): Int = recipes?.size ?: 0

    fun updateRecipes(newRecipes: List<ExternalRecipe>) {
        recipes = newRecipes.toMutableList()
        notifyDataSetChanged()
    }
}
