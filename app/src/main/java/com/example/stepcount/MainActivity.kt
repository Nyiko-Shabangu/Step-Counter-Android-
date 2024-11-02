package com.example.stepcount

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stepcount.ui.theme.StepCountTheme

import androidx.lifecycle.lifecycleScope


import android.hardware.SensorEventListener
import android.os.Build
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

class MainActivity : ComponentActivity(), SensorEventListener {



    private lateinit var sensorManager: SensorManager
    private var stepCounter: Sensor? = null
    private var stepCount = 0
    private lateinit var stepCountTextView: TextView
    private lateinit var database: StepCountDatabase
    private lateinit var dao: StepCountDao


    // Firebase reference
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val stepCountRef = firebaseDatabase.getReference("stepCounts")

    companion object {
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE = 100
        private const val ACTIVITY_RECOGNITION_PERMISSION = "android.permission.ACTIVITY_RECOGNITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stepCountTextView = findViewById(R.id.stepCountTextView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        database = StepCountDatabase.getDatabase(this)
        dao = database.stepCountDao()

        if (stepCounter == null) {
            stepCountTextView.text = "Step counter sensor not available on this device"
        } else {
            checkAndRequestPermission()
            //loadLatestStepCount()
            loadAllStepCounts()
        }
    }

    private fun loadLatestStepCount() {
        lifecycleScope.launch {
            val latestStepCount = dao.getLatestStepCount()
            latestStepCount?.let {
                stepCount = it.count
                updateStepCountUI(stepCount)
            }
        }
    }

    private fun loadStepCountsFromApi() {
        lifecycleScope.launch {
            try {
                val stepCounts = RetrofitInstance.api.getAllStepCounts()
                displayStepCounts(stepCounts.map { StepCount(it.id, it.count) }) // Convert to your local model
            } catch (e: Exception) {
                e.printStackTrace() // Handle the error
            }
        }
    }

    private fun loadAllStepCounts() {
        lifecycleScope.launch {
            val stepCounts = dao.getAllStepCounts()
            displayStepCounts(stepCounts)
        }
    }

    private fun displayStepCounts(stepCounts: List<StepCount>) {
        val formattedSteps = stepCounts.joinToString(separator = "\n") { step ->
            "ID: ${step.id}, Steps: ${step.count}"
        }
        stepCountTextView.text = "All Steps:\n$formattedSteps"
    }

    override fun onResume() {
        super.onResume()
        stepCounter?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = it.values[0].toInt()
                updateStepCountUI(stepCount)
                saveStepCount(stepCount)
            }
        }
    }

    private fun saveStepCount(count: Int) {
        // Save locally
        lifecycleScope.launch {
            dao.insert(StepCount(count = count))



            // API post
            try {
                val stepCountApiResponse =
                    StepCountApiResponse(id = 0, count = count+100) // Assuming ID is autogenerated
                val response =  RetrofitInstance.api.addapiStepCount(stepCountApiResponse)

                if (response.isSuccessful) {
                    Log.d("API Success", "Step count saved successfully.")
                } else {
                    Log.e("API Error", "Error saving step count: ${response.errorBody()?.string()}")
                }
            }
            catch (e: HttpException) {
            Log.e("API Error", "Error saving step count: ${e.message()}")
        } catch (e: Exception) {
            Log.e("General Error", "Error: ${e.message}")
        }

            
            // Sync with Firebase
            stepCountRef.push().setValue(StepCount(count = count))
    }


    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun updateStepCountUI(count: Int) {
        stepCountTextView.text = "Steps: $count"
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(ACTIVITY_RECOGNITION_PERMISSION),
                    ACTIVITY_RECOGNITION_REQUEST_CODE)
            } else {
                stepCountTextView.text = "Step counter initialized. Start walking!"
            }
        } else {
            // Permission not required for Android 9 (Pie) and below
            stepCountTextView.text = "Step counter initialized. Start walking!"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    stepCountTextView.text = "Step counter initialized. Start walking!"
                } else {
                    stepCountTextView.text = "Step counter permission denied"
                }
            }
        }
    }
}