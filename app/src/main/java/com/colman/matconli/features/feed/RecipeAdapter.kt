package com.colman.matconli.features.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.databinding.ItemRecipeBinding
import com.colman.matconli.model.Recipe

class RecipeAdapter : RecyclerView.Adapter<RecipeViewHolder>() {

    var listener: OnItemClickListener? = null
    var recipes: MutableList<Recipe>? = null

    interface OnItemClickListener {
        fun onItemClick(recipe: Recipe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.listener = listener
        holder.bind(recipes?.get(position))
    }

    override fun getItemCount(): Int = recipes?.size ?: 0

    fun updateRecipes(newRecipes: List<Recipe>) {
        val oldList = recipes ?: mutableListOf()
        val diffCallback = RecipeDiffCallback(oldList, newRecipes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        if (recipes == null) {
            recipes = mutableListOf()
        }
        recipes?.clear()
        recipes?.addAll(newRecipes)
        diffResult.dispatchUpdatesTo(this)
    }

    private class RecipeDiffCallback(
        private val oldList: List<Recipe>,
        private val newList: List<Recipe>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}

