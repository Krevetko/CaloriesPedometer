package com.example.caloriespedometer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class PedometerFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var isStarted = false
    private var stepCount = 0
    private var stepGoal: Int? = null  // Default step goal is null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pedometer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(FragmentActivity.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        val startStopButton: Button = view.findViewById(R.id.StartStopButton)
        val resetButton: Button = view.findViewById(R.id.ResetButton)
        val circularProgressBar: CircularProgressBar = view.findViewById(R.id.circularProgressBar)
        val stepCountTextView: TextView = view.findViewById(R.id.stepCountTextView)
        val setStepGoalTextView: TextView = view.findViewById(R.id.set_step_goal)

        // Initialize the step count text view
        updateStepCountText(stepCount, stepGoal)

        startStopButton.setOnClickListener {
            when (startStopButton.text.toString()) {
                "Start" -> {
                    startStopButton.text = "Stop"
                    resetButton.visibility = Button.VISIBLE
                    isStarted = true
                    startStepCounting()
                }
                "Stop" -> {
                    startStopButton.text = "Resume"
                    isStarted = false
                    stopStepCounting()
                }
                "Resume" -> {
                    startStopButton.text = "Stop"
                    isStarted = true
                    startStepCounting()
                }
            }
        }

        resetButton.setOnClickListener {
            stepCount = 0
            updateStepCountText(stepCount, stepGoal)
            circularProgressBar.progress = 0f
            circularProgressBar.setProgressWithAnimation(0f)  // Reset the progress bar color
            resetButton.visibility = View.GONE
            startStopButton.text = "Start"
            isStarted = false
            stopStepCounting()
        }

        setStepGoalTextView.setOnClickListener {
            showStepGoalDialog()
        }
    }

    private fun updateStepCountText(stepCount: Int, stepGoal: Int?) {
        val stepCountTextView: TextView = view?.findViewById(R.id.stepCountTextView) ?: return
        if (stepGoal != null && stepGoal > 0) {
            stepCountTextView.text = "$stepCount/$stepGoal"
        } else {
            stepCountTextView.text = stepCount.toString()
        }
    }

    private fun showStepGoalDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Your Step Goal")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputText = input.text.toString()
            stepGoal = if (inputText.isNotEmpty()) inputText.toInt() else null
            updateStepCountText(stepCount, stepGoal)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun startStepCounting() {
        if (stepSensor == null) {
            Toast.makeText(requireContext(), "No Step Sensor Detected!", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopStepCounting() {
        sensorManager.unregisterListener(this, stepSensor)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (isStarted && event != null && event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            val circularProgressBar: CircularProgressBar = view?.findViewById(R.id.circularProgressBar) ?: return
            updateStepCountText(stepCount, stepGoal)
            val goal = stepGoal
            if (goal != null && goal > 0) {
                circularProgressBar.progress = (stepCount.toFloat() / goal) * 100
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(requireContext(), "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestActivityRecognitionPermission()
        }
    }
}
