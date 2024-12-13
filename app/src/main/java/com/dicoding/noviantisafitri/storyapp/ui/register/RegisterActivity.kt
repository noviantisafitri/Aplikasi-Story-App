package com.dicoding.noviantisafitri.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.noviantisafitri.storyapp.R
import com.dicoding.noviantisafitri.storyapp.data.di.ViewModelFactory
import com.dicoding.noviantisafitri.storyapp.databinding.ActivityRegisterBinding
import com.dicoding.noviantisafitri.storyapp.ui.main.MainActivity
import com.dicoding.noviantisafitri.storyapp.ui.welcome.WelcomeActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        startAnimation()
        setupAction()
        observeViewModel()
    }

    private fun setupView() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.title_register)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun startAnimation() {
        val cardImg = ObjectAnimator.ofFloat(binding.cardImg, View.ALPHA, 1f).setDuration(300)
        val title = ObjectAnimator.ofFloat(binding.tvTitleRegister, View.ALPHA, 1f).setDuration(300)
        val name = ObjectAnimator.ofFloat(binding.tvNameRegister, View.ALPHA, 1f).setDuration(300)
        val nameInput = ObjectAnimator.ofFloat(binding.tlNameRegister, View.ALPHA, 1f).setDuration(300)
        val email = ObjectAnimator.ofFloat(binding.tvEmailRegister, View.ALPHA, 1f).setDuration(300)
        val emailInput = ObjectAnimator.ofFloat(binding.tlEmailRegister, View.ALPHA, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(binding.tvPasswordRegister, View.ALPHA, 1f).setDuration(300)
        val passwordInput = ObjectAnimator.ofFloat(binding.tlPasswordRegister, View.ALPHA, 1f).setDuration(300)
        val registerButton = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(
                cardImg, title, name, nameInput, email, emailInput, password, passwordInput, registerButton
            )
            startDelay = 300
            start()
        }
    }


    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            if (isValidInput()) {
                postDataInput()
            }
        }
    }

    private fun isValidInput(): Boolean {
        binding.apply {
            var isValid = true

            if (edRegisterName.text.isNullOrEmpty()) {
                tlNameRegister.error = getString(R.string.required_field)
                isValid = false
            } else {
                tlNameRegister.error = null
            }

            if (edRegisterEmail.text.isNullOrEmpty()) {
                tlEmailRegister.error = getString(R.string.required_field)
                isValid = false
            } else {
                tlEmailRegister.error = null
            }

            if (edRegisterPassword.text.isNullOrEmpty()) {
                tlPasswordRegister.error = getString(R.string.required_field)
                isValid = false
            } else {
                tlPasswordRegister.error = null
            }

            return isValid
        }
    }

    private fun postDataInput() {
        binding.apply {
            registerViewModel.dataRegister(
                edRegisterName.text.toString(),
                edRegisterEmail.text.toString(),
                edRegisterPassword.text.toString()
            )
        }
    }

    private fun observeViewModel() {
        registerViewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                showRegisterFailedDialog()
            }
        }

        registerViewModel.registerResponse.observe(this) { response ->
            if (response?.error == false) {
                moveActivity()
            }
        }

        registerViewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            btnRegister.isEnabled = !isLoading
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showRegisterFailedDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.login_failed))
            setMessage("Please try again")
            setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun moveActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @Deprecated("onBackPressed() is deprecated, use onBackPressedDispatcher.onBackPressed() instead.", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
