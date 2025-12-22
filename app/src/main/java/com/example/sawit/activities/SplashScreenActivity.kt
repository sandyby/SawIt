package com.example.sawit.activities

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.sawit.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    private var isReadyToProceed = false
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition { !isReadyToProceed }
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView: View = splashScreenViewProvider.view
            splashView.animate().translationY(-splashView.height.toFloat() / 2)
                .alpha(0f)
                .setDuration(350L)
                .withEndAction {
                    splashScreenViewProvider.remove()
                }.start()
        }
        checkAuthAndProceed()
    }

    private fun checkAuthAndProceed(){
        val auth = FirebaseAuth.getInstance()
        val authSharedPref = getSharedPreferences("AuthSession", MODE_PRIVATE)
        val isRememberMeOn = authSharedPref.getBoolean("rememberMe", false)
        val currentUser = auth.currentUser

        lifecycleScope.launch {
            if (currentUser != null && isRememberMeOn) {
                userViewModel.listenForUserUpdates()

                userViewModel.userProfile.filterNotNull().first().let {
                    isReadyToProceed = true
                    goToActivity(MainActivity::class.java)
                }
            } else {
                if (currentUser != null && !isRememberMeOn) {
                    userViewModel.logout()
                }
                isReadyToProceed = true
                goToActivity(LoginActivity::class.java)
            }
        }
    }

    private fun goToActivity(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}