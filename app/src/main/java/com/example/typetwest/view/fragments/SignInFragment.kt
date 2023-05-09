package com.example.typetwest.view.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.typetwest.R
import com.example.typetwest.databinding.FragmentSignInBinding
import com.example.typetwest.model.User
import com.example.typetwest.utils.Constants
import com.example.typetwest.view.activities.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class SignInFragment : BaseFragment() {

    private lateinit var binding: FragmentSignInBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        auth = FirebaseAuth.getInstance()


        binding.tvSignUp.setOnClickListener {
            val fragmentSignUp = SignUpFragment.newInstance("", "")
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fl_login_screen_fragment_container, fragmentSignUp)?.commit()
        }


        binding.btnLogin.setOnClickListener {
            signInRegisteredUser()
        }

        return binding.root
    }



    private fun signInRegisteredUser() {
        val email: String = binding.etEnterEmail.text.toString().trim { it < ' ' }
        val password: String = binding.etEnterPassword.text.toString().trim { it < ' ' }
        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constants.SIGN_IN_FRAGMENT, "signInWithEmail:success")
                         startActivity(Intent(requireContext(), MainActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Constants.SIGN_IN_FRAGMENT, "signInWithEmail:failure", task.exception)
                        Toast.makeText(requireContext(), "Wrong login or password", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {

            TextUtils.isEmpty(email) -> {
                Snackbar.make(requireView(), "Please enter an email", 500).show()
                false
            }

            TextUtils.isEmpty(password) -> {
                Snackbar.make(requireView(), "Please enter a password", 500).show()
                false
            }
            else -> true
        }
    }



    fun signInSuccess(user:User){
        hideProgressDialog()
        startActivity(Intent(requireContext() ,MainActivity::class.java))
        activity?.finish()
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}