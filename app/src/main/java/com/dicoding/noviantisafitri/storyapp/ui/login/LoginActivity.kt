package com.dicoding.noviantisafitri.storyapp.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.noviantisafitri.storyapp.R
import com.dicoding.noviantisafitri.storyapp.data.di.ViewModelFactory
import com.dicoding.noviantisafitri.storyapp.data.preference.SessionModel
import com.dicoding.noviantisafitri.storyapp.databinding.ActivityLoginBinding
import com.dicoding.noviantisafitri.storyapp.ui.main.MainActivity
import com.dicoding.noviantisafitri.storyapp.ui.welcome.WelcomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var factory: ViewModelFactory
    private val loginViewModel: LoginViewModel by viewModels { factory }
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        setupViewModel()
        startAnimation()
        setupAction()
        showToast()
    }

    private fun startAnimation() {
        val cardImg = ObjectAnimator.ofFloat(binding.cardImg, View.ALPHA, 1f).setDuration(300)
        val title = ObjectAnimator.ofFloat(binding.tvTitleLogin, View.ALPHA, 1f).setDuration(300)
        val message = ObjectAnimator.ofFloat(binding.tv2ndtitleLogin, View.ALPHA, 1f).setDuration(300)
        val email = ObjectAnimator.ofFloat(binding.tvEmailLogin, View.ALPHA, 1f).setDuration(300)
        val emailInput = ObjectAnimator.ofFloat(binding.tlEmailLogin, View.ALPHA, 1f).setDuration(300)
        val password = ObjectAnimator.ofFloat(binding.tvPasswordLogin, View.ALPHA, 1f).setDuration(300)
        val passwordInput = ObjectAnimator.ofFloat(binding.tlPasswordLogin, View.ALPHA, 1f).setDuration(300)
        val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(cardImg, title, message, email, emailInput, password, passwordInput, loginButton)
            startDelay = 500
        }.start()
    }

    private fun setupAction() {
        binding.apply {
            btnLogin.setOnClickListener {
                if (edLoginEmail.length() == 0 || edLoginPassword.length() == 0) {
                    edLoginEmail.error = getString(R.string.required_field)
                    edLoginPassword.error = getString(R.string.required_field)
                } else {
                    postText()
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun dismissLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showLoginFailedDialog() {
        AlertDialog.Builder(this).apply {
            dismissLoading()
            setTitle(getString(R.string.login_failed))
            setMessage("Please try again")
            setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }


    private fun setupView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar

        supportActionBar?.apply {
            title = getString(R.string.title_login)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupViewModel() {
        factory = ViewModelFactory.getInstance(this)
    }

    private fun showToast() {
        loginViewModel.toastText.observe(this@LoginActivity) {
            it.getContentIfNotHandled()?.let { toastText ->
                if (toastText.equals("success", ignoreCase = true)) {
                    moveActivity()
                } else {
                    showLoginFailedDialog()
                }
            }
        }
    }

    private fun moveActivity() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }

    private fun postText() {
        showLoading()
        binding.apply {
            loginViewModel.postLogin(
                edLoginEmail.text.toString(),
                edLoginPassword.text.toString()
            )
        }

        loginViewModel.loginResponse.observe(this@LoginActivity) { response ->
            dismissLoading()
            if (response != null && !response.error) {
                saveSession(
                    SessionModel(
                        response.loginResult?.name.toString(),
                        AUTH_KEY + " " + (response.loginResult?.token.toString()),
                        true
                    )
                )
            } else {
                dismissLoading()
            }
        }
    }


    private fun saveSession(session: SessionModel) {
        loginViewModel.saveSession(session)
    }

    companion object {
        private const val AUTH_KEY = "Bearer"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
