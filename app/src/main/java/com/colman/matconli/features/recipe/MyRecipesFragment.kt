package com.colman.matconli.features.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentMyRecipesBinding
import com.colman.matconli.features.feed.RecipeAdapter
import com.colman.matconli.model.Recipe
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.base.BaseFragment
import com.colman.matconli.features.feed.RecipeListFragmentDirections
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show
import com.colman.matconli.utilis.toggleVisibility

class MyRecipesFragment : BaseFragment(), RecipeAdapter.OnItemClickListener {

    var binding: FragmentMyRecipesBinding? = null

    private lateinit var adapter: RecipeAdapter
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = getCurrentUserId()
        setupMenu()
        setupRecyclerView()
        setupClickListeners()
        observeRecipes()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_my_recipes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_main_feed -> {
                        findNavController().popBackStack()
                        true
                    }
                    R.id.action_search_external -> {
                        findNavController().navigate(
                            RecipeListFragmentDirections.actionFeedFragmentToExternalSearchFragment()
                        )
                        true
                    }
                    R.id.action_profile -> {
                        findNavController().navigate(
                            MyRecipesFragmentDirections.actionMyRecipesFragmentToProfileFragment()
                        )
                        true
                    }
                    R.id.action_logout -> {
                        performLogout(MyRecipesFragmentDirections.actionMyRecipesFragmentToLoginFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter()
        adapter.listener = this
        binding?.fragmentMyRecipesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.fragmentMyRecipesRecyclerView?.adapter = adapter
    }

    private fun setupClickListeners() {
        binding?.fragmentMyRecipesFloatingActionButton?.setOnClickListener {
            findNavController().navigate(
                MyRecipesFragmentDirections.actionMyRecipesFragmentToAddRecipeFragment(null)
            )
        }

        binding?.fragmentMyRecipesSwipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
    }

    private fun observeRecipes() {
        RecipeRepository.shared.getAllRecipes().observe(viewLifecycleOwner) { recipes ->
            val myRecipes = recipes.filter { it.ownerId == userId }
            adapter.updateRecipes(myRecipes)
            binding?.fragmentMyRecipesTextViewEmpty?.visibility = if (myRecipes.isEmpty()) View.VISIBLE else View.GONE
            binding?.fragmentMyRecipesRecyclerView?.visibility = if (myRecipes.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun refreshData() {
        binding?.fragmentMyRecipesProgressBar?.show()
        RecipeRepository.shared.refreshRecipes()
        binding?.fragmentMyRecipesProgressBar?.hide()
        binding?.fragmentMyRecipesSwipeRefreshLayout?.isRefreshing = false
    }

    override fun onItemClick(recipe: Recipe) {
        findNavController().navigate(
            MyRecipesFragmentDirections.actionMyRecipesFragmentToRecipeDetailFragment(recipe.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

