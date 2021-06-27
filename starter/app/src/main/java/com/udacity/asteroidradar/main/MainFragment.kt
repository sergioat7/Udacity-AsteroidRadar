package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }
    private lateinit var adapter: AsteroidAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        adapter = AsteroidAdapter(AsteroidAdapter.AsteroidListener {
            viewModel.onAsteroidClicked(it)
        })

        val binding = FragmentMainBinding.inflate(inflater).apply {

            lifecycleOwner = this@MainFragment
            viewModel = this@MainFragment.viewModel
            adapter = this@MainFragment.adapter
        }

        viewModel.navigateToAsteroidDetail.observe(viewLifecycleOwner, {
            it?.let { asteroid ->
                findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onAsteroidDetailNavigated()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        viewModel.updateFilter(
            when (item.itemId) {
                R.id.show_week_menu -> Constants.NasaApiFilter.SHOW_WEEK
                R.id.show_today_menu -> Constants.NasaApiFilter.SHOW_TODAY
                else -> Constants.NasaApiFilter.SHOW_SAVED
            }
        )
        return true
    }
}
