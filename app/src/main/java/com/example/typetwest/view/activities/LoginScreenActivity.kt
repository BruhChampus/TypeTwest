package com.example.typetwest.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.typetwest.R
import com.example.typetwest.controller.FirestoreClass
import com.example.typetwest.databinding.ActivityLoginScreenBinding
import com.example.typetwest.view.fragments.SignInFragment
import com.example.typetwest.view.fragments.SignUpFragment

class LoginScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /*
        * if user is already logged in he proceeds to MainActivity
        * */
        val currentUserId = FirestoreClass().getCurrentUserId()
        if (currentUserId.isNotEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
        }else{
            val fragmentSignIn = SignInFragment.newInstance("", "")
            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_login_screen_fragment_container, fragmentSignIn).commit()
        }
    }
}