package com.example.aegis

import com.example.aegis.BuildConfig
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aegis.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val client = OkHttpClient()
    private val PREFS_NAME = "AegisPrefs"
    private val PIN_KEY = "user_pin"

    // --- SECRETS FETCHED FROM local.properties via BuildConfig ---
    // Note: If BuildConfig is red, go to Build > Rebuild Project
    private val LOCK_URL = BuildConfig.LOCK_URL
    private val API_KEY = BuildConfig.API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLock.setOnClickListener {
            // Trigger heavy haptic for the initial tap
            it.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )

            handlePinProcess()
        }
    }

    private fun handlePinProcess() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPin = prefs.getString(PIN_KEY, null)

        if (savedPin == null) {
            showCreatePinDialog()
        } else {
            showVerifyPinDialog(savedPin)
        }
    }

    private fun showCreatePinDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Create 4-digit PIN"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("First Time Setup")
            .setMessage("Please set a PIN to protect your laptop.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save PIN") { _, _ ->
                val newPin = input.text.toString()
                if (newPin.length >= 4) {
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putString(PIN_KEY, newPin)
                        .apply()
                    Toast.makeText(this, "PIN Saved! Tap again to lock.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PIN too short!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showVerifyPinDialog(correctPin: String) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Enter PIN"
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Identity")
            .setView(input)
            .setPositiveButton("Lock Laptop") { _, _ ->
                if (input.text.toString() == correctPin) {
                    sendLockRequest()
                } else {
                    // "Reject" feedback for wrong PIN
                    binding.btnLock.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    Toast.makeText(this, "Wrong PIN!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendLockRequest() {
        // Build the request using variables from local.properties
        val request = Request.Builder()
            .url(LOCK_URL)
            .addHeader("X-Api-Key", API_KEY) // Ensure this dot is connected to the line above
            .post("".toRequestBody())
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            // Heavy success feedback
                            binding.btnLock.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            Toast.makeText(this@MainActivity, "Success: Laptop Locked", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Server Error: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Check Internet/Tailscale", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}