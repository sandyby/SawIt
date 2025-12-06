package com.example.sawit.activities

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.ActivityCreateEditBinding
import com.example.sawit.models.Activity
import com.example.sawit.viewmodels.FieldViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEditBinding
    private val calendar = Calendar.getInstance()
    private val fieldViewModel: FieldViewModel by viewModels()

    private var currentActivity: Activity? = null
    private var isEditMode = false

    companion object {
        const val EXTRA_ACTIVITY = "EXTRA_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (intent.hasExtra(EXTRA_ACTIVITY)) {
            currentActivity =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_ACTIVITY, Activity::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_ACTIVITY)
                }
            isEditMode = currentActivity != null
        }

        setupUI()
        setupListeners()
        setupActivityTypeDropdown()
        observeFields()

        if (isEditMode) {
            currentActivity?.let { populateForm(it) }
        }
    }

    private fun setupUI() {
        if (isEditMode) {
            binding.textViewTitle.text = "Edit Activity"
        } else {
            binding.textViewTitle.text = "New Activity"
        }
    }

    private fun populateForm(activity: Activity) {
        binding.autoCompleteField.setText(activity.fieldName, false) // false agar tidak memfilter
        binding.autoCompleteActivityType.setText(activity.activityType, false)
        calendar.time = activity.date
        updateDateInView()
        binding.editTextNotes.setText(activity.notes)
    }

    private fun setupListeners() {
        binding.ibBackBtn.setOnClickListener {
            finish()
        }
        binding.editTextDate.setOnClickListener {
            showDatePicker()
        }
        binding.buttonSave.setOnClickListener {
            validateAndShowToast()
        }
    }

    private fun validateAndShowToast() {
        val fieldName = binding.autoCompleteField.text.toString().trim()
        val activityType = binding.autoCompleteActivityType.text.toString().trim()
        val dateStr = binding.editTextDate.text.toString().trim()

        var isValid = true
        if (fieldName.isEmpty()) {
            binding.textFieldLayoutField.error = "Field must be selected"
            isValid = false
        } else {
            binding.textFieldLayoutField.error = null
        }

        if (activityType.isEmpty()) {
            binding.textFieldLayoutActivityType.error = "Activity type must be selected"
            isValid = false
        } else {
            binding.textFieldLayoutActivityType.error = null
        }

        if (dateStr.isEmpty()) {
            binding.textFieldLayoutDate.error = "Date must be selected"
            isValid = false
        } else {
            binding.textFieldLayoutDate.error = null
        }

        if (!isValid) return

        val toastMessage = if (isEditMode) {
            "Activity updated! (Demo)"
        } else {
            "Activity saved! (Demo)"
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        finish()
    }


    private fun setupActivityTypeDropdown() {
        val activityTypes = resources.getStringArray(R.array.activity_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityTypes)
        binding.autoCompleteActivityType.setAdapter(adapter)
    }

    private fun observeFields() {
        fieldViewModel.fieldsData.onEach { fields ->
            if (fields.isNotEmpty()) {
                val fieldNames = fields.map { it.fieldName }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    fieldNames
                )
                binding.autoCompleteField.setAdapter(adapter)
            }
        }
    }
    //        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                fieldViewModel.fieldsData.collect { fields ->
//                    if (fields.isNotEmpty()) {
//                        val fieldNames = fields.map { it.fieldName }
//                        val adapter = ArrayAdapter(
//                            ,
//                            android.R.layout.simple_dropdown_item_1line,
//                            fieldNames
//                        )
//                        binding.autoCompleteField.setAdapter(adapter)
//                    }
//                }
//            }
//        }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDate.setText(sdf.format(calendar.time))
    }
}

