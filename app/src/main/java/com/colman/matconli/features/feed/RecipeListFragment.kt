package com.colman.matconli.features.feed

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
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentFeedBinding
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.model.Recipe
import com.colman.matconli.base.BaseFragment
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show
import com.colman.matconli.utilis.toggleVisibility

class RecipeListFragment : BaseFragment(), RecipeAdapter.OnItemClickListener {

    var binding: FragmentFeedBinding? = null

    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                menuInflater.inflate(R.menu.menu_feed, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_my_recipes -> {
                        findNavController().navigate(
                            RecipeListFragmentDirections.actionFeedFragmentToMyRecipesFragment()
                        )
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
                            RecipeListFragmentDirections.actionFeedFragmentToProfileFragment()
                        )
                        true
                    }
                    R.id.action_logout -> {
                        performLogout(RecipeListFragmentDirections.actionFeedFragmentToLoginFragment())
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
        binding?.fragmentFeedRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.fragmentFeedRecyclerView?.adapter = adapter
    }

    private fun setupClickListeners() {
        binding?.fragmentFeedFloatingActionButton?.visibility = View.VISIBLE
        binding?.fragmentFeedFloatingActionButton?.setOnClickListener {
            findNavController().navigate(
                RecipeListFragmentDirections.actionFeedFragmentToAddRecipeFragment(null)
            )
        }

        binding?.fragmentFeedSwipeRefreshLayout?.setOnRefreshListener {
            refreshData()
        }
    }

    private var hasLoadedInitial = false

    private fun observeRecipes() {
        RecipeRepository.shared.getAllRecipes().observe(viewLifecycleOwner) { recipes ->
            adapter.updateRecipes(recipes)
            binding?.fragmentFeedTextViewEmpty?.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            binding?.fragmentFeedRecyclerView?.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE

            if (!hasLoadedInitial && recipes.isEmpty()) {
                hasLoadedInitial = true
                refreshData()
            }
        }
    }

    private fun refreshData() {
        binding?.fragmentFeedProgressBar?.show()
        RecipeRepository.shared.refreshRecipes()
        binding?.fragmentFeedProgressBar?.hide()
        binding?.fragmentFeedSwipeRefreshLayout?.isRefreshing = false
    }

    override fun onItemClick(recipe: Recipe) {
        findNavController().navigate(
            RecipeListFragmentDirections.actionFeedFragmentToRecipeDetailFragment(recipe.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

