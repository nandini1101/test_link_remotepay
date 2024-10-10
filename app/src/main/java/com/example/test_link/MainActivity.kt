package com.example.test_link

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eze.api.EzeAPI
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnMakePayment: Button
    private val REQUEST_CODE_INITIALIZE = 100016

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial)

        // Initialize the views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnMakePayment = findViewById(R.id.btnMakePayment)

        // Set the button click listener
        btnMakePayment.setOnClickListener {
            // Get the input values
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phoneNumber = etPhoneNumber.text.toString()

            // Build the JSON request
            val paymentRequest = buildCashPaymentRequest(name, email, phoneNumber)

            // Call the payment method
            doRemotePayTxn(paymentRequest)
        }
        initializeSDK();
    }

    private fun initializeSDK() {
        Log.d("Log", "Called initialization")
        val jsonRequest = JSONObject()
        try {
            jsonRequest.put("demoAppKey", "80504210-c4a6-4678-bd7a-b3f5aaca2ffb")
            jsonRequest.put("prodAppKey", "Your prod app key")
            jsonRequest.put("merchantName", "NAGAR_NIGAM_LKO")
            jsonRequest.put("userName", "9718775851")
            jsonRequest.put("currencyCode", "INR")
            jsonRequest.put("appMode", "Demo")
            jsonRequest.put("captureSignature", "true")
            jsonRequest.put("prepareDevice", "true")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.d("Log", "json created $jsonRequest")
        Log.d("Log", "Called init")

        EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest)
        Log.d("Log", "Ended init")
    }

    // Function to build the JSON request for payment
    private fun buildCashPaymentRequest(name: String, email: String, phoneNumber: String): JSONObject {
        // Validate input
        if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            showMessage("All fields are required.")
            return JSONObject() // Return an empty JSONObject or handle it as needed
        }

        val jsonRequest = JSONObject()
        try {
            jsonRequest.put("amount", "123") // This should be dynamic if possible

            // Build references
            val references = JSONObject().apply {
                put("reference1", "1234")

                val additionalReferences = JSONArray().apply {
                    put("addRef_xx1")
                    put("addRef_xx2")
                }
                put("additionalReferences", additionalReferences)
            }

            // Build customer object from input values
            val customer = JSONObject().apply {
                put("name", name)
                put("mobileNo", phoneNumber)
                put("email", email)
            }

            // Combine into options
            val options = JSONObject().apply {
                put("references", references)
                put("customer", customer)
            }

            jsonRequest.put("options", options)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonRequest
    }


    // Method to handle the remote payment transaction
    private fun doRemotePayTxn(jsonRequest: JSONObject) {
        // Assuming EzeAPI is integrated for remote payment
        EzeAPI.remotePayment(this, REQUEST_CODE_REMOTE_PAY, jsonRequest)
    }

    // Handle the result of the payment transaction
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_REMOTE_PAY) {
            if (data != null) {
                try {
                    val response = data.getStringExtra("response") // Get the response from the Intent
                    Log.d(TAG, "Response received: $response")

                    val jsonResponse = JSONObject(response)

                    if (resultCode == Activity.RESULT_OK) {
                        // Handle success
                        val transactionStatus = jsonResponse.optString("status", "No status")
                        val transactionMessage = jsonResponse.optString("message", "No message")
                        showMessage("Payment Successful: $transactionStatus - $transactionMessage")
                        Log.d(TAG, "Transaction Status: $transactionStatus")
                        Log.d(TAG, "Transaction Message: $transactionMessage")
                    } else {
                        // Handle failure
                        val errorMessage = jsonResponse.optString("errorMessage", "Unknown error occurred")
                        showMessage("Payment Failed: $errorMessage")
                        Log.e(TAG, "Payment Error: $errorMessage")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    showMessage("Error processing payment response")
                    Log.e(TAG, "JSONException: ${e.message}")
                }
            } else {
                showMessage("No response received")
                Log.e(TAG, "No response received from the payment activity")
            }
        }
    }

    // Helper function to display messages using a Toast
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val REQUEST_CODE_REMOTE_PAY = 10016 // Replace with actual request code
        private const val TAG = "MainActivity"
    }
}
