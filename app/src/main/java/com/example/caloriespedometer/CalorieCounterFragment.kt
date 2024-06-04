package com.example.caloriespedometer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CalorieCounterFragment : Fragment() {

    private lateinit var editTextAge: EditText
    private lateinit var editTextHeight: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var spinnerActivity: Spinner
    private lateinit var buttonCalculate: Button
    private lateinit var textViewResult: TextView

    private var BMR: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calorie_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editTextAge = view.findViewById(R.id.editText_age)
        editTextHeight = view.findViewById(R.id.editText_height)
        editTextWeight = view.findViewById(R.id.editText_weight)
        radioGroupGender = view.findViewById(R.id.radioGroup_gender)
        spinnerActivity = view.findViewById(R.id.spinner_activity)
        buttonCalculate = view.findViewById(R.id.button_calculate)
        textViewResult = view.findViewById(R.id.textView_result)

        // Setup spinner for activity levels
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.activity_levels,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerActivity.adapter = adapter
        }

        buttonCalculate.setOnClickListener {
            calculateCalories()
        }

        editTextAge.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val age = s?.toString()?.toIntOrNull()
                if (age != null) {
                    if (age < 15 || age > 80) {
                        editTextAge.error = "Please enter a valid age between 15 and 80."
                    } else {
                        editTextAge.error = null // Clear the error
                    }
                } else {
                    editTextAge.error = "Please enter a valid age."
                }
            }
        })
    }

    private fun calculateCalories() {
        val age = editTextAge.text.toString().toIntOrNull()
        val height = editTextHeight.text.toString().toIntOrNull()
        val weight = editTextWeight.text.toString().toIntOrNull()
        val gender = when (radioGroupGender.checkedRadioButtonId) {
            R.id.radioButton_male -> "male"
            R.id.radioButton_female -> "female"
            else -> null
        }
        val activityLevel = spinnerActivity.selectedItem.toString()

        if (age == null || height == null || weight == null || gender == null) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        BMR = if (gender == "male") {
            66.5 + (13.75 * weight) + (5.003 * height) - (6.75 * age)
        } else {
            655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
        }

        val calories = when (activityLevel) {
            "Basal Metabolic Rate (BMR)" -> BMR
            "Sedentary: little or no exercise" -> BMR * 1.2
            "Light: exercise 1-3 times/week" -> BMR * 1.375
            "Moderate: exercise 4-5 times/week" -> BMR * 1.55
            "Active: daily exercise or intense exercise 3-4 times/week" -> BMR * 1.725
            "Very Active: intense exercise 6-7 times/week" -> BMR * 1.9
            "Extra Active: very intense exercise daily, or physical job" -> BMR * 2.0
            else -> BMR
        }

        textViewResult.text = "Your Caloric Needs: %.2f calories/day".format(calories)
    }
}
