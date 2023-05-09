package com.example.typetwest.view.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typetwest.R
import com.example.typetwest.databinding.DialogProgressBinding
import com.google.firebase.auth.FirebaseAuth

open class BaseActivity : AppCompatActivity() {
    private lateinit var mProgressDialog: Dialog

    fun showProgressDialog(text: String = "Please, wait") {
        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        binding.tvProgressText.text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }

}