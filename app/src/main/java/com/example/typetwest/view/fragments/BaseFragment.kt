package com.example.typetwest.view.fragments

import android.app.Dialog
import androidx.fragment.app.Fragment
import com.example.typetwest.R
import com.example.typetwest.databinding.DialogProgressBinding

open class BaseFragment: Fragment() {
    private lateinit var mProgressDialog: Dialog


    fun showProgressDialog(text: String = "Please, wait") {
        val binding = DialogProgressBinding.inflate(layoutInflater)
        mProgressDialog = Dialog(requireContext())
        mProgressDialog.setContentView(R.layout.dialog_progress)
        binding.tvProgressText.text = text
        mProgressDialog.show()
    }

    fun hideProgressDialog() {
        mProgressDialog.dismiss()
    }
}