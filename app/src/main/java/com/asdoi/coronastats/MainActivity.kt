package com.asdoi.coronastats

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.asdoi.corona.Parser
import com.asdoi.corona.ParserDE
import com.asdoi.corona.worldometers.world.WmWorldParser
import com.asdoi.coronastats.databinding.ActivityMainBinding
import com.mikepenz.aboutlibraries.LibsBuilder
import com.pd.chocobar.ChocoBar
import java.util.*


const val PREF_CITIES = "prefCities"
const val PREF_COUNTRIES = "prefCountries"
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

        refreshRecyclerView()
    }

    private fun refreshRecyclerView() {
        binding.loading.visibility = View.VISIBLE

        Thread {
            try {
                val cities = getCities()
                val parsedTickers =
                    if (isAllCountries())
                        Parser.parseNoInternalErrors(*cities.toTypedArray())
                    else
                        ParserDE.parseNoInternalErrors(*cities.toTypedArray())


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
            }
        }.start()
    }

    fun addCityDialog() {
        val suggestions =
            if (isAllCountries())
                Parser.getSuggestions()
            else
                ParserDE.getSuggestions()

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, suggestions
        )
        val textView = AutoCompleteTextView(this)
        textView.setAdapter(adapter)

        MaterialDialog(this).show {
            customView(view = textView, horizontalPadding = true)
            title(R.string.add_city)
            positiveButton(R.string.add) {
                val view = it.getCustomView() as AutoCompleteTextView
                val city = view.text.toString()

                Thread {
                    try {
                        val cities =
                            if (isAllCountries())
                                Parser.parseNoErrors(city)
                            else
                                ParserDE.parseNoErrors(city)

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
        }
    }


    private fun saveCity(city: String) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putString(
                PREF_CITIES,
                "${this.getCitiesString()}$DIVIDER${city.uppercase(Locale.getDefault())}"
            )
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
        cities.remove(name.uppercase(Locale.getDefault()))
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
        return getCities().contains(WmWorldParser.location.uppercase(Locale.getDefault()))
    }

    fun addWorldWide() {
        saveCity(WmWorldParser.location.uppercase(Locale.getDefault()))
    }

    fun removeWorldWide() {
        removeCity(WmWorldParser.location.uppercase(Locale.getDefault()))
    }

    fun isAllCountries(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_COUNTRIES, false)
    }

    fun changeAllCountries() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(PREF_COUNTRIES, !isAllCountries())
            .apply()
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

        val countriesItem = menu.findItem(R.id.action_include_countries)!!
        if (isAllCountries()) {
            countriesItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_earth)
            countriesItem.title = getString(R.string.exclude_all_countries)
        } else {
            countriesItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_no_earth)
            countriesItem.title = getString(R.string.include_all_countries)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> refreshRecyclerView()
            R.id.action_add_city -> addCityDialog()
            R.id.action_info -> {
                LibsBuilder()
                    .withActivityTitle(getString(R.string.about))
                    .withAboutIconShown(true)
                    .withFields(R.string::class.java.fields)
                    .withLicenseShown(true)
                    .withAboutAppName(getString(R.string.app_name))
                    .withAboutSpecial1(getString(R.string.source_code))
                    .withAboutSpecial1Description("<h1>" + getString(R.string.source_code) + ":</h1><br><a href=\"https://gitlab.com/asdoi/coronastats\">Gitlab.com/asdoi/coronastats</a>")
                    .withAboutSpecial2(getString(R.string.author))
                    .withAboutSpecial2Description("<h1>" + getString(R.string.author) + ":</h1><br><a href=\"https://gitlab.com/asdoi\">Asdoi</a>")
                    .withAboutSpecial3(getString(R.string.license))
                    .withAboutSpecial3Description("<h1>MIT License</h1><br><a href=\"https://gitlab.com/asdoi/coronastats/-/blob/master/LICENSE\">GitLab</a>")
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
            R.id.action_include_countries -> {
                changeAllCountries()
                refreshRecyclerView()
                invalidateOptionsMenu()
            }
        }
        return true
    }
}