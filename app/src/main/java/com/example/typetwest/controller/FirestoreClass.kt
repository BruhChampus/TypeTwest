package com.example.typetwest.controller

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.typetwest.R
import com.example.typetwest.model.User
import com.example.typetwest.utils.Constants
import com.example.typetwest.view.activities.LoginScreenActivity
import com.example.typetwest.view.activities.MainActivity
import com.example.typetwest.view.activities.ResultScreenActivity
import com.example.typetwest.view.fragments.SignInFragment
import com.example.typetwest.view.fragments.SignUpFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFirestoreClass = FirebaseFirestore.getInstance()

    fun registerUser(fragment: SignUpFragment, userInfo: User) {
        mFirestoreClass.collection(Constants.USERS).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                fragment.userRegisteredSuccess()
            }.addOnFailureListener {
                Log.e(fragment.javaClass.simpleName, "Error writing document")
            }
    }


    fun loadUserData(activity: Activity) {
        mFirestoreClass.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!
                when (activity) {
                    is LoginScreenActivity -> {
                        val fragmentManager = activity.supportFragmentManager
                        val fragment =
                            fragmentManager.findFragmentById(R.id.fl_login_screen_fragment_container) as SignInFragment
                        fragment.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> activity.updateNavigationUserDetails(loggedInUser)
                }
            }
    }


    //Updates user data passing hashmap
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        mFirestoreClass.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                when (activity) {
                    is MainActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Profile data have been updated successfully"
                        )
                        Toast.makeText(
                            activity,
                            "Profile data have been updated successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        activity.profileUpdateSuccess()
                    }

                    is ResultScreenActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Profile data have been updated successfully"
                        )
                        activity.hideProgressDialog()
                    }
                }

            }.addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ResultScreenActivity -> activity.hideProgressDialog()

                }
                Log.e(activity.javaClass.simpleName, "Error updating profile data")
            }
    }


    fun getCurrentUserId(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }
}