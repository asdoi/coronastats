package com.asdoi.coronastats

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.asdoi.corona.ParserDE
import com.asdoi.corona.worldometers.world.WmWorldParser
import com.asdoi.coronastats.databinding.ActivityMainBinding
import com.mikepenz.aboutlibraries.LibsBuilder
import com.pd.chocobar.ChocoBar


const val PREF_CITIES = "prefCities"
const val DIVIDER = "%"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: StatsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val viewManager = LinearLayoutManager(this)
        viewAdapter = StatsAdapter(
                this, listOf()
        )
        recyclerView = binding.recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshRecyclerView()
        }

        refreshRecyclerView()
    }

    private fun refreshRecyclerView() {
        binding.loading.visibility = View.VISIBLE

        Thread {
            try {
                val cities = getCities()
                val parsedTickers = ParserDE.parseNoInternalErrors(*cities.toTypedArray())

                runOnUiThread {
                    viewAdapter.data = parsedTickers
                    recyclerView.visibility = View.VISIBLE
                    binding.noInternet.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    recyclerView.visibility = View.GONE
                    binding.noInternet.visibility = View.VISIBLE
                }
            }

            runOnUiThread {
                binding.loading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }.start()
    }

    fun addCityDialog() {
        MaterialDialog(this).show {
            input(
                    hintRes = R.string.munich,
                    inputType = InputType.TYPE_CLASS_TEXT
            ) { _, text ->
                val city = text.toString()

                Thread {
                    try {
                        val cities = ParserDE.parseNoErrors(city)
                        if (cities.isNotEmpty()) {
                            saveCity(cities[0].location)
                            runOnUiThread {
                                refreshRecyclerView()
                            }
                        } else
                            throw Exception()

                    } catch (e: Exception) {
                        e.printStackTrace()

                        runOnUiThread {
                            ChocoBar.builder().setView(binding.root)
                                    .setText(R.string.city_not_found)
                                    .setDuration(ChocoBar.LENGTH_LONG)
                                    .red()
                                    .show()
                        }
                    }
                }.start()
            }
            positiveButton(R.string.add)
            title(R.string.add_city)
        }
    }


    private fun saveCity(city: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PREF_CITIES, "${this.getCitiesString()}$DIVIDER${city.toUpperCase()}")
                .apply()
        invalidateOptionsMenu()
    }

    private fun getCitiesString() =
            PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(PREF_CITIES, "")!!

    fun getCities(): MutableList<String> {
        val cities = getCitiesString().split(DIVIDER).toMutableList()
        cities.removeAll(listOf(""))
        return cities
    }

    fun removeCity(name: String) {
        val cities = getCities()
        cities.remove(name.toUpperCase())
        val saveString = StringBuilder()
        if (cities.size == 1) {
            saveString.append(cities[0])
        } else {
            for (city in cities) {
                saveString.append(DIVIDER).append(city)
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(PREF_CITIES, saveString.toString())
                .apply()

        refreshRecyclerView()
        invalidateOptionsMenu()
    }

    fun isWorldWide(): Boolean {
        return getCities().contains(WmWorldParser.location.toUpperCase())
    }

    fun addWorldWide() {
        saveCity(WmWorldParser.location.toUpperCase())
    }

    fun removeWorldWide() {
        removeCity(WmWorldParser.location.toUpperCase())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val worldItem = menu.findItem(R.id.action_switch_world)!!
        if (isWorldWide()) {
            worldItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_earth)
            worldItem.title = getString(R.string.hide_worldwide_statistics)
        } else {
            worldItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_no_earth)
            worldItem.title = getString(R.string.show_worldwide_statistics)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> refreshRecyclerView()
            R.id.action_add_city -> addCityDialog()
            R.id.action_info -> {
                LibsBuilder()
                        .withActivityTitle(getString(R.string.open_source_libraries))
                        .withAboutIconShown(true)
                        .withFields(R.string::class.java.fields)
                        .withLicenseShown(true)
                        .withAboutAppName(getString(R.string.app_name))
                        .start(this)
            }
            R.id.action_switch_world -> {
                if (isWorldWide())
                    removeWorldWide()
                else
                    addWorldWide()
                refreshRecyclerView()
                invalidateOptionsMenu()
            }
        }
        return true
    }
}