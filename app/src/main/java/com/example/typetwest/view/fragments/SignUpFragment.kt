package com.example.typetwest.view.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.typetwest.R
import com.example.typetwest.controller.FirestoreClass
import com.example.typetwest.databinding.FragmentSignInBinding
import com.example.typetwest.databinding.FragmentSignUpBinding
import com.example.typetwest.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class SignUpFragment : BaseFragment() {

    private lateinit var binding: FragmentSignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentSignUpBinding.inflate(layoutInflater)
        binding.tvSignIn.setOnClickListener {
            val fragmentSignIn = SignInFragment.newInstance("", "")
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fl_login_screen_fragment_container, fragmentSignIn)?.commit()
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }
        return binding.root
    }


    private fun registerUser() {
        val email = binding.etEnterEmail.text.toString().trim { it < ' ' }
        val password = binding.etEnterPassword.text.toString().trim { it < ' ' }
        val confirmedPassword = binding.etConfirmPassword.text.toString().trim { it < ' ' }

        if (password != confirmedPassword) {
            Toast.makeText(
                requireContext(),
                "Password and confirm password are different!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (validateForm(email, password, confirmedPassword)) {
                showProgressDialog()
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser? = task.result.user
                            val registeredEmail = firebaseUser!!.email
                            val user = User(id = firebaseUser.uid, email = registeredEmail!!)
                            FirestoreClass().registerUser(this, user)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                task.exception!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideProgressDialog()
                        }
                    }
            }

        }
    }

    fun userRegisteredSuccess() {
        hideProgressDialog()
        Toast.makeText(
            requireContext(),
            "You've successfully registered",
            Toast.LENGTH_LONG
        ).show()
        FirebaseAuth.getInstance().signOut()

        val fragmentSignIn = SignInFragment.newInstance("", "")
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fl_login_screen_fragment_container, fragmentSignIn)?.commit()
    }


    private fun validateForm(email: String, password: String, confirmedPassword: String): Boolean {
        return when {

            TextUtils.isEmpty(email) -> {
                Snackbar.make(requireView(), "Please enter an email", 500).show()
                false
            }

            TextUtils.isEmpty(password) -> {
                Snackbar.make(requireView(), "Please enter a password", 500).show()
                false
            }

            TextUtils.isEmpty(confirmedPassword) -> {
                Snackbar.make(requireView(), "Please enter a password confirmation", 500).show()
                false
            }
            else -> true
        }
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}