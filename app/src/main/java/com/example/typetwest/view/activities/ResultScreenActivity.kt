package com.example.typetwest.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.typetwest.R
import com.example.typetwest.controller.FirestoreClass
import com.example.typetwest.databinding.ActivityResultScreenBinding
import com.example.typetwest.utils.Constants

class ResultScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityResultScreenBinding
    private var mistakes = 0
    private var correct = 0
    private var seconds = 0
    private var minutes: Float = 0.0f

    private var grossWpm: Float = 0f
    private var netWpm: Float = 0f
    private var accuracy: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.MISTAKES) && intent.hasExtra(Constants.CORRECT)) {
            mistakes = intent.getIntExtra(Constants.MISTAKES, 0)
            correct = intent.getIntExtra(Constants.CORRECT, 0)
            seconds = intent.getIntExtra(Constants.SECONDS, 0)
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        calculateValues()

        binding.tvTotalAccuracy.text = "Accuracy: ${String.format("%.1f", accuracy)}%"
        binding.tvAvgWpm.text = "Average WPM: $netWpm"
        binding.tvMistakes.text = "Mistakes: $mistakes"

        updateUserProfileData()
    }


    private fun calculateValues(){
        minutes = (seconds.toFloat() / 60.toFloat())
        grossWpm = (correct + mistakes / 5) / minutes
        netWpm = grossWpm - (mistakes / minutes)
        accuracy = (netWpm / grossWpm) * 100
        if(accuracy.isNaN()){
            accuracy = 0.0f
        }

        Log.i("Minutes", "$minutes")
        Log.i("Seconds", "$seconds")
        Log.i("grossWpm", "$grossWpm")
        Log.i("netWpm", "$netWpm")
        Log.i("accuracy", "$accuracy")
    }

    private fun updateUserProfileData() {
        showProgressDialog()
        val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.AVG_WPM] = netWpm.toInt()
            userHashMap[Constants.ACCURACY] = String.format("%.1f", accuracy) + "%"
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}