package com.example.typetwest.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.typetwest.R
import com.example.typetwest.controller.retrofit.RetrofitInstance
import com.example.typetwest.databinding.ActivityTypeScreenBinding
import com.example.typetwest.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TypeScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityTypeScreenBinding
    private var mSeconds: Int = 0
    private var mistakes: Int = 0
    private var correct: Int = 0
    private lateinit var text: String
    private lateinit var countTimer: CountDownTimer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTypeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.SECONDS)) {
            mSeconds = intent.getIntExtra(Constants.SECONDS, 30)
        }

        showProgressDialog()

        val textCall: Call<List<String>> = RetrofitInstance.api.getLoremIpsum()
        textCall.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val textResponseBody = response.body()
                    Log.i("textResponseBody", textResponseBody.toString())
                    if (textResponseBody != null) {
                        text = textResponseBody.toString()
                            .substring(1, textResponseBody.toString().length - 1)
                        val spannable = SpannableString(text)
                        binding.etHintText.setText(spannable)
                        hideProgressDialog()
                    }
                } else {
                    when (response.code()) {
                        400 -> Log.e("Error 400", "Bad Connection")
                        404 -> Log.e("Error 404", "Not Found")
                        else -> Log.e("Error", "Generic Error")
                    }
                    hideProgressDialog()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("Error", t.message.toString())
                hideProgressDialog()
            }
        })


        binding.etTypedText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // перебираем символы введенного текста
                for (i in s.indices) {
                    val typed = s[i]
                    val expected: Char = text[i]
                    if (i >= text.length - 1) {
                        // если пользователь ввел больше символов, чем в заданном тексте, выходим из цикла
                        if (typed == expected) {
                            // символы совпадают, изменяем цвет на зеленый
                            binding.etHintText.editableText.setSpan(
                                ForegroundColorSpan(Color.GREEN),
                                i,
                                i + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            countTimer.onFinish()
                        } else {
                            // символы не совпадают, изменяем цвет на красный
                            binding.etHintText.editableText.setSpan(
                                ForegroundColorSpan(Color.RED),
                                i,
                                i + 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            countTimer.onFinish()
                        }
                        break
                    }
                    // сравниваем символы
                    if (typed == expected) {
                        // символы совпадают, изменяем цвет на зеленый
                        binding.etHintText.editableText.setSpan(
                            ForegroundColorSpan(Color.GREEN),
                            i,
                            i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        // символы не совпадают, изменяем цвет на красный
                        binding.etHintText.editableText.setSpan(
                            ForegroundColorSpan(Color.RED),
                            i,
                            i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                for (i in text.indices) {
                    if (i >= s.length) {
                        // пользователь стер символ, изменяем цвет на серый
                        binding.etHintText.editableText.setSpan(
                            ForegroundColorSpan(Color.GRAY),
                            i,
                            i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        })





        binding.etTypedText.setOnClickListener {
            countTimer.start()
            binding.etTypedText.visibility = View.INVISIBLE
            binding.tvTapOnScreenToStart.visibility = View.GONE
        }

        binding.ivClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


        countTimer = object : CountDownTimer((mSeconds * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                binding.tvTimer.text = (p0 / 1000).toString()
            }

            override fun onFinish() {
                val spans = binding.etHintText.editableText.getSpans(
                    0,
                    binding.etHintText.text.length,
                    ForegroundColorSpan::class.java
                )
                val redCharIndexes = HashSet<Int>()
                val greenCharIndexes = HashSet<Int>()

                for (span in spans) {
                    if (span.foregroundColor == Color.GREEN) {
                        val index = binding.etHintText.editableText.getSpanStart(span)
                        greenCharIndexes.add(index)

                    }
                    if (span.foregroundColor == Color.RED) {
                        val index = binding.etHintText.editableText.getSpanStart(span)
                        redCharIndexes.add(index)

                    }
                }
                correct = greenCharIndexes.size
                mistakes = redCharIndexes.size
                Log.i("mistakes", mistakes.toString())
                //Hide keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etTypedText.windowToken, 0)
                countTimer.cancel()


                val intent = Intent(this@TypeScreenActivity, ResultScreenActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(Constants.MISTAKES, mistakes)
                intent.putExtra(Constants.CORRECT, correct)
                intent.putExtra(Constants.SECONDS, mSeconds)
                startActivity(intent)
                finish()

            }

        }
    }
}