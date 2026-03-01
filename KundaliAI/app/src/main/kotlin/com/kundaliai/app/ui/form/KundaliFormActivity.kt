package com.kundaliai.app.ui.form

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kundaliai.app.R
import com.kundaliai.app.data.models.ApiState
import com.kundaliai.app.data.models.KundaliRequest
import com.kundaliai.app.databinding.ActivityKundaliFormBinding
import com.kundaliai.app.language.LanguageManager
import com.kundaliai.app.ui.result.ResultActivity
import com.kundaliai.app.ui.viewmodel.KundaliViewModel
import com.kundaliai.app.utils.Constants
import com.kundaliai.app.utils.gone
import com.kundaliai.app.utils.showToast
import com.kundaliai.app.utils.visible
import java.util.*

class KundaliFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKundaliFormBinding
    private val viewModel: KundaliViewModel by viewModels()
    private lateinit var languageManager: LanguageManager

    private var selectedDate = ""
    private var selectedTime = ""
    private var selectedLanguage = Constants.LANG_ENGLISH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKundaliFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        languageManager = LanguageManager(this)
        selectedLanguage = languageManager.getSavedLanguage()

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.generate_kundali)

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Date picker
        binding.etDateOfBirth.setOnClickListener { showDatePicker() }
        binding.tilDateOfBirth.setEndIconOnClickListener { showDatePicker() }

        // Time picker
        binding.etTimeOfBirth.setOnClickListener { showTimePicker() }
        binding.tilTimeOfBirth.setEndIconOnClickListener { showTimePicker() }

        // Language spinner
        val langOptions = languageManager.getLanguageOptions().map { it.second }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, langOptions)
        binding.acLanguage.setAdapter(adapter)

        val currentLangIndex = languageManager.getLanguageOptions().indexOfFirst {
            it.first == selectedLanguage
        }.coerceAtLeast(0)
        binding.acLanguage.setText(langOptions[currentLangIndex], false)

        binding.acLanguage.setOnItemClickListener { _, _, position, _ ->
            selectedLanguage = languageManager.getLanguageOptions()[position].first
        }

        // Submit
        binding.btnGenerate.setOnClickListener { validateAndSubmit() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate = "%02d/%02d/%04d".format(day, month + 1, year)
                binding.etDateOfBirth.setText(selectedDate)
            },
            cal.get(Calendar.YEAR) - 25,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).also { dialog ->
            dialog.datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedTime = "%02d:%02d".format(hour, minute)
                binding.etTimeOfBirth.setText(selectedTime)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun validateAndSubmit() {
        val name = binding.etName.text.toString().trim()
        val pob = binding.etPlaceOfBirth.text.toString().trim()

        val error = viewModel.validateForm(name, selectedDate, selectedTime, pob)
        if (error != null) {
            showToast(error)
            return
        }

        val request = KundaliRequest(
            name = name,
            dateOfBirth = selectedDate,
            timeOfBirth = selectedTime,
            placeOfBirth = pob,
            language = selectedLanguage
        )

        viewModel.generateKundali(request)
    }

    private fun observeViewModel() {
        viewModel.kundaliState.observe(this) { state ->
            when (state) {
                is ApiState.Loading -> {
                    binding.progressContainer.visible()
                    binding.btnGenerate.isEnabled = false
                }
                is ApiState.Success -> {
                    binding.progressContainer.gone()
                    binding.btnGenerate.isEnabled = true

                    startActivity(
                        Intent(this, ResultActivity::class.java).apply {
                            putExtra(Constants.KEY_KUNDALI_RESULT, state.data)
                        }
                    )
                }
                is ApiState.Error -> {
                    binding.progressContainer.gone()
                    binding.btnGenerate.isEnabled = true
                    showToast("Error: ${state.message}")
                }
            }
        }
    }
}
