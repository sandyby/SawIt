package com.example.sawit.activities

import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.widget.AdapterView
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
import com.example.sawit.databinding.ActivityCreateEditBinding
import com.example.sawit.models.Activity
import com.example.sawit.viewmodels.ActivityViewModel
import com.example.sawit.viewmodels.FieldViewModel
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class CreateEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEditBinding
    private val calendar = Calendar.getInstance()
    private val fieldViewModel: FieldViewModel by viewModels()
    private val activityViewModel: ActivityViewModel by viewModels()
    private var currentActivity: Activity? = null
    private var isEditMode = false

    companion object {
        const val EXTRA_ACTIVITY = "EXTRA_ACTIVITY"
        private const val FIELD_PLACEHOLDER = "Choose a Field"
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
        val fieldId = ""
        val fieldName = binding.autoCompleteField.text.toString().trim()
        val activityType = binding.autoCompleteActivityType.text.toString().trim()
        val dateStr = binding.editTextDate.text.toString().trim()
        val notes = binding.editTextNotes.text.toString()

        var isValid = true
        if (fieldName.isEmpty() || fieldName == FIELD_PLACEHOLDER) {
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

        if (!isValid) return

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        val activityDate = dateFormat.parse(dateStr) ?: Date()

        val activityData = Activity(
            id = currentActivity?.id,
            fieldId = fieldId,
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
                fieldViewModel.fieldsData.collect { fields ->
                    val actualFieldNames = fields.map { it.fieldName }

                    // 1. Create the list with the placeholder
                    val fieldNamesWithPlaceholder = mutableListOf(FIELD_PLACEHOLDER)
                    fieldNamesWithPlaceholder.addAll(actualFieldNames)

                    // 2. Fix: Use 'fieldNamesWithPlaceholder' in the adapter, not 'actualFieldNames'
                    val adapter = ArrayAdapter(
                        this@CreateEditActivity,
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

                        // 3. Fix: Add an explicit click listener to ensure the value sticks
                        setOnItemClickListener { parent, _, position, _ ->
                            val selectedItem = parent.getItemAtPosition(position).toString()
                            setText(selectedItem, false)
                        }
                    }

                    // 4. Fix: Only reset text to placeholder if the box is empty or currently holds the placeholder.
                    // This prevents overwriting a valid selection if the data flow emits again.
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
                                    this@CreateEditActivity,
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

