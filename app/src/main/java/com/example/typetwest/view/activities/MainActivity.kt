package com.example.typetwest.view.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.typetwest.R
import com.example.typetwest.controller.FirestoreClass
import com.example.typetwest.databinding.ActivityMainBinding
import com.example.typetwest.databinding.DialogAddImageBinding
import com.example.typetwest.model.User
import com.example.typetwest.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.io.IOException



//TODO сделаьб код чище, добаввить график в ResultActivity, реализовать мтеоды onDestroy, написать комменты
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mUserDetails: User
    private var mSeconds: Int = 30
    private lateinit var mDialog: Dialog
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl: String = ""
    private lateinit var mSharedPreferences: SharedPreferences
    private var mAccuracyFromResultScreen: Float? = null
    private var mAvgWpmFromResultScreen: Int? = null
    private var mAvgWpm: Int = 0
    private var mAverageAccuracy: String =  ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showProgressDialog()
        FirestoreClass().loadUserData(this)
        initClickListeners()

        if (intent.hasExtra(Constants.AVG_WPM) && intent.hasExtra(Constants.ACCURACY)) {
            mAccuracyFromResultScreen = intent.getFloatExtra(Constants.ACCURACY, 0.0f)
            mAvgWpmFromResultScreen = intent.getIntExtra(Constants.AVG_WPM, 0)
        }

    }


    private fun initClickListeners() {
        binding.tvSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginScreenActivity::class.java))
            finish()
        }

        binding.ivLeftArrow.setOnClickListener {
            if (mSeconds == 15) {
                Toast.makeText(this, "You cannot reduce the seconds even more", Toast.LENGTH_LONG)
                    .show()
            } else {
                mSeconds -= 15
                binding.tvSeconds.text = "$mSeconds seconds"
            }
        }

        binding.ivRightArrow.setOnClickListener {
            if (mSeconds == 300) {
                Toast.makeText(this, "You cannot increase the seconds more", Toast.LENGTH_LONG)
                    .show()
            } else {
                mSeconds += 15
                binding.tvSeconds.text = "$mSeconds seconds"
            }
        }

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, TypeScreenActivity::class.java)
            intent.putExtra(Constants.SECONDS, mSeconds)
            startActivity(intent)
        }


        binding.civUserImage.setOnClickListener {
            //  Toast.makeText(this, mUserDetails.email, Toast.LENGTH_LONG).show()
            val binding = DialogAddImageBinding.inflate(layoutInflater)
            binding.tvEmail.text = mUserDetails.email
            binding.tvAddImageButton.setOnClickListener {
                /*Checking if user have permission on already
                * if yes, than showImageChooser()
                * */
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showImageChooser()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                }
            }
            val builder = AlertDialog.Builder(this).setView(binding.root)
            mDialog = builder.create()
            mDialog.show()
        }
    }


    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageUrl
        }
        userHashMap[Constants.AVG_WPM] = mAvgWpm
        userHashMap[Constants.ACCURACY] = mAverageAccuracy
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser()
            }
        } else {
            Toast.makeText(
                this,
                "It seems that you've denied permission for storage. You can turn it on in settings",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showImageChooser() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        loadImageGallery.launch(galleryIntent)
    }
    private val loadImageGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mSelectedImageFileUri = result.data!!.data
                try {
                    Glide.with(this).load(mSelectedImageFileUri).centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .into(binding.civUserImage)

                    if (mSelectedImageFileUri != null) {
                        uploadUserImages()
                    } else {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        updateUserProfileData()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mDialog.dismiss()
        }



    //Adding image to FireStorage
    private fun uploadUserImages() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedImageFileUri
                )
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Firebase image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i(
                        "Downloadable image URL",
                        uri.toString()
                    )
                    mProfileImageUrl = uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
        }
    }


    fun updateNavigationUserDetails(user: User) {
        hideProgressDialog()
        mUserDetails = user
        mUserDetails.email = user.email
        mUserDetails.avgWpm = user.avgWpm
        mUserDetails.accuracy = user.accuracy


        Glide.with(this).load(mUserDetails.image).centerCrop()
            .placeholder(R.drawable.image_placeholder)
            .into(findViewById<CircleImageView>(R.id.civ_user_image))


        if (mAccuracyFromResultScreen != null && mAvgWpmFromResultScreen != null) {
            //Making mUserDetails.accuracy from "86,4%" to "86.4"
            val mUserDetailsAccuracyFloat = mUserDetails.accuracy.replace(",", ".").dropLast(1)
            val mAverageAccuracyFloat =
                ((mUserDetailsAccuracyFloat.toFloat() + mAccuracyFromResultScreen!!) / 2)
            mAverageAccuracy = String.format("%.1f", mAverageAccuracyFloat) + "%"
            mAvgWpm = (mUserDetails.avgWpm + mAvgWpmFromResultScreen!!) / 2
            //Passing the new data to Firebase
            updateUserProfileData()
            binding.tvSeconds.text = "$mSeconds seconds"
            binding.tvTotalAccuracy.text = "Total accuracy: ${mAverageAccuracy}"
            binding.tvAvgWpm.text = "Average WPM: ${mAvgWpm}"
        }else{
            binding.tvSeconds.text = "$mSeconds seconds"
            binding.tvTotalAccuracy.text = "Total accuracy: ${mUserDetails.accuracy}"
            binding.tvAvgWpm.text = "Average WPM: ${mUserDetails.avgWpm}"
        }


    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
    }
}

