package com.example.sawit.activities

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.sawit.R
import com.example.sawit.databinding.ActivityCreateEditActivityBinding
import com.example.sawit.models.Activity
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateEditActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEditActivityBinding
    private val calendar = Calendar.getInstance()
    private val fieldViewModel: FieldViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by viewModels()
    private var currentActivity: Activity? = null
    private var isEditMode = false
    private var fieldIdMap: Map<String, String> = emptyMap()

    companion object {
        const val EXTRA_ACTIVITY = "EXTRA_ACTIVITY"
        private const val FIELD_PLACEHOLDER = "Choose a field"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateEditActivityBinding.inflate(layoutInflater)
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
        observeActivityEvents()

        if (isEditMode) {
            currentActivity?.let { populateForm(it) }
        }
    }

    private fun setupUI() {
        if (isEditMode) {
            binding.tvHeaderTitle.text = "Edit Activity"
            disableFieldSelection()
        } else {
            binding.tvHeaderTitle.text = "New Activity"
        }
        binding.buttonSave.text = if (isEditMode) "SAVE" else "CREATE"
    }

    private fun disableFieldSelection() {
        binding.autoCompleteField.isEnabled = false
        binding.textFieldLayoutField.endIconMode = TextInputLayout.END_ICON_NONE
    }

    private fun populateForm(activity: Activity) {
        binding.autoCompleteField.setText(activity.fieldName, false)
        binding.autoCompleteActivityType.setText(activity.activityType, false)
        calendar.time = activity.date
        updateDateInView()
        binding.editTextNotes.setText(activity.notes)
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.editTextDate.setOnClickListener {
            showDatePicker()
        }
        binding.buttonSave.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val fieldName = binding.autoCompleteField.text.toString().trim()
        val activityType = binding.autoCompleteActivityType.text.toString().trim()
        val dateStr = binding.editTextDate.text.toString().trim()
        val notes = binding.editTextNotes.text.toString()

        val fieldId = fieldIdMap[fieldName]

        var isValid = true
        if (fieldName.isEmpty() || fieldName == FIELD_PLACEHOLDER || fieldId.isNullOrEmpty()) {
            binding.textFieldLayoutField.error = "Please select a field!"
            isValid = false
        } else {
            binding.textFieldLayoutField.error = null
        }

        if (activityType.isEmpty()) {
            binding.textFieldLayoutActivityType.error = "Please select an activity!"
            isValid = false
        } else {
            binding.textFieldLayoutActivityType.error = null
        }

        if (dateStr.isEmpty()) {
            binding.textFieldLayoutDate.error = "Please select a valid date!"
            isValid = false
        } else {
            binding.textFieldLayoutDate.error = null
        }

        if (notes.isNotBlank() && notes.length > 300) {
            binding.textFieldLayoutNotes.error = "The maximum characters allowed is 300!"
            isValid = false
        } else {
            binding.textFieldLayoutNotes.error = null
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val activityDate = dateFormat.parse(dateStr) ?: Date()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (activityDate.before(today)) {
            binding.textFieldLayoutDate.error = "You cannot schedule activities in the past, duh!"
            isValid = false
        } else {
            binding.textFieldLayoutDate.error = null
        }

        if (!isValid) return

        val activityData = Activity(
            id = currentActivity?.id,
            userId = currentActivity?.userId ?: "",
            fieldId = fieldId!!,
            fieldName = fieldName,
            activityType = activityType,
            date = activityDate,
            notes = notes,
            status = currentActivity?.status ?: "planned"
        )

        if (isEditMode) {
            activityViewModel.updateActivity(activityData)
        } else {
            activityViewModel.createNewActivity(activityData)
        }
    }

    private fun setupActivityTypeDropdown() {
        val activityTypes = resources.getStringArray(R.array.activity_types)
        val adapter = ArrayAdapter(this, R.layout.autocompleteview_dropdown_item, activityTypes)
        binding.autoCompleteActivityType.apply {
            setDropDownBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.autocomplete_background,
                    null
                )
            )
        }
        binding.autoCompleteActivityType.setAdapter(adapter)

        if (!isEditMode && activityTypes.isNotEmpty()) {
            binding.autoCompleteActivityType.setText(activityTypes[0], false)
        }
    }

    private fun observeFields() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                fieldViewModel.fieldsData.collectLatest { fields ->
                    fieldIdMap = fields.associate { it.fieldName to it.fieldId }
                    val actualFieldNames = fields.map { it.fieldName }

                    val fieldNamesWithPlaceholder = mutableListOf(FIELD_PLACEHOLDER)
                    fieldNamesWithPlaceholder.addAll(actualFieldNames)

                    val adapter = ArrayAdapter(
                        this@CreateEditActivityActivity,
                        R.layout.autocompleteview_dropdown_item,
                        fieldNamesWithPlaceholder
                    )

                    binding.autoCompleteField.apply {
                        setDropDownBackgroundDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.autocomplete_background,
                                null
                            )
                        )
                        setAdapter(adapter)

                        setOnItemClickListener { parent, _, position, _ ->
                            val selectedItem = parent.getItemAtPosition(position).toString()
                            setText(selectedItem, false)
                        }
                    }

                    val currentText = binding.autoCompleteField.text.toString()
                    if (currentText.isEmpty() || currentText == FIELD_PLACEHOLDER) {
                        binding.autoCompleteField.setText(FIELD_PLACEHOLDER, false)
                    }
                }
            }
        }
    }

    private fun observeActivityEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    activityViewModel.events.collect { event ->
                        when (event) {
                            is ActivityViewModel.Event.ShowMessage -> {
                                Toast.makeText(
                                    this@CreateEditActivityActivity,
                                    event.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                if (event.message.contains("Successfully")) {
                                    finish()
                                }
                            }
                        }
                    }
                }
                launch {
                    activityViewModel.isLoading.collect { isLoading ->
                        updateButtonState(isLoading)
                    }
                }
            }
        }
    }

    private fun updateButtonState(isLoading: Boolean) {
        binding.buttonSave.apply {
            isEnabled = !isLoading
            if (isLoading && isEditMode) {
                text = "SAVING..."
            } else if (isLoading && !isEditMode) {
                text = "CREATING..."
            } else {
                text = if (isEditMode) "SAVE" else "CREATE"
            }
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        val datePicker = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDate.setText(sdf.format(calendar.time))
    }
}

