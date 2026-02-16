package com.colman.matconli.features.external_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentExternalSearchBinding
import com.colman.matconli.model.ExternalRecipe
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show

class ExternalSearchFragment : Fragment(), ExternalRecipeAdapter.OnItemClickListener {

    var binding: FragmentExternalSearchBinding? = null

    private val viewModel: ExternalSearchViewModel by viewModels()
    private lateinit var adapter: ExternalRecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExternalSearchBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_external_search, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_main_feed -> {
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = ExternalRecipeAdapter()
        adapter.listener = this

        binding?.fragmentExternalSearchRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExternalSearchFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding?.fragmentExternalSearchSearchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchRecipes(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.updateRecipes(recipes)
            updateEmptyState(recipes.isEmpty())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding?.fragmentExternalSearchProgressBar?.show()
                binding?.fragmentExternalSearchRecyclerView?.hide()
                binding?.fragmentExternalSearchTextViewEmpty?.hide()
            } else {
                binding?.fragmentExternalSearchProgressBar?.hide()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding?.fragmentExternalSearchRecyclerView?.hide()
            binding?.fragmentExternalSearchTextViewEmpty?.show()
        } else {
            binding?.fragmentExternalSearchRecyclerView?.show()
            binding?.fragmentExternalSearchTextViewEmpty?.hide()
        }
    }

    override fun onItemClick(recipe: ExternalRecipe) {
        Toast.makeText(requireContext(), recipe.title, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
