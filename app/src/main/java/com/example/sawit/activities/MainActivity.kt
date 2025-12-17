package com.example.sawit.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.fragments.ActivitiesFragment
import com.example.sawit.fragments.FieldsFragment
import com.example.sawit.fragments.HomeFragment
import com.example.sawit.fragments.PredictFragment
import com.example.sawit.fragments.PredictionHistoryFragment
import com.example.sawit.fragments.ProfileFragment
import com.example.sawit.fragments.TopHeaderFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : AppCompatActivity() {
    /**
     * ini merupakan activity utama kita yang berguna untuk mengatur tampilan utama setelah pengguna berhasil masuk ke dalam aplikasi di dashboard
     *
     * - menampilkan fragment yang sesuai berdasarkan tab animatedbottombar yang dipilih oleh user
     * - mengatur tampilan header atas (TopHeaderFragment) yang menyesuaikan judul halaman
     * - mengatur animasi transisi antar-fragment agar transisi jadi smooth
     * - menampilkan indikator loading saat data awal sedang dimuat (dengan sengaja).
     *
     * kami memilih untuk menggunakan beberapa fragment (HomeFragment, FieldsFragment, dsb.)
     * agar setiap fitur utama dapat dipisahkan dengan jelas dan mudah dikelola secara modular.
     *
     * selain itu, penggunaan AnimatedBottomBar memberi kami efek animasi modern pada navigasi bawah,
     * menggantikan BottomNavigationView standar dengan tampilan yang lebih menarik.
     */
    private lateinit var bottomBar: AnimatedBottomBar
    private lateinit var topHeaderFragment: TopHeaderFragment
    private lateinit var homeFragment: HomeFragment
    private lateinit var fieldsFragment: FieldsFragment
    private lateinit var activitiesFragmment: ActivitiesFragment
    private lateinit var predictionsFragment: PredictionHistoryFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        topHeaderFragment = TopHeaderFragment()
        homeFragment = HomeFragment()
        fieldsFragment = FieldsFragment()
        activitiesFragmment = ActivitiesFragment()
        predictionsFragment = PredictionHistoryFragment()
        profileFragment = ProfileFragment()

        show(homeFragment)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        lifecycleScope.launch {
            showLoading(true)
            supportFragmentManager.beginTransaction().replace(R.id.fl_top_header, topHeaderFragment)
                .commit()

            delay(2500)

            supportFragmentManager.executePendingTransactions()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            showLoading(false)
        }
        topHeaderFragment.setTopHeaderFragmentTitle("Dashboard")

        AnimatedBottomBar.Badge(
            text = "99",
            backgroundColor = Color.RED,
            textColor = Color.GREEN,
            textSize = 12
        )

        bottomBar = findViewById(R.id.abb_bottom_bar)

        bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                when (newTab.id) {
                    R.id.tab_home -> {
                        show(homeFragment)
                        topHeaderFragment.setTopHeaderFragmentTitle("Dashboard")
                    }

                    R.id.tab_fields -> {
                        show(fieldsFragment)
                        topHeaderFragment.setTopHeaderFragmentTitle("Fields")
                    }

                    R.id.tab_activity -> {
                        show(activitiesFragmment)
                        topHeaderFragment.setTopHeaderFragmentTitle("Activities")
                    }

                    R.id.tab_predict -> {
                        show(predictionsFragment)
                        topHeaderFragment.setTopHeaderFragmentTitle("Prediction")
                    }

                    R.id.tab_profile -> {
                        show(profileFragment)
                        topHeaderFragment.setTopHeaderFragmentTitle("Profile")
                    }
                }
            }

            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                //
            }
        })

    }

    private fun showLoading(show: Boolean) {
        val overlay = findViewById<View>(R.id.cpi_loading_overlay)
        val progress = findViewById<CircularProgressIndicator>(R.id.cpi_loading_indicator)

        overlay.visibility = if (show) View.VISIBLE else View.GONE
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun show(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left)
        transaction.replace(R.id.fl_scroll_view_content, fragment).commit()
    }

    fun navigateToFieldsFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_in_left,
            R.anim.slide_in_left,
            R.anim.slide_in_right
        )
        transaction.replace(R.id.fl_scroll_view_content, fieldsFragment)
        transaction.addToBackStack(null)
        transaction.commit()
        topHeaderFragment.setTopHeaderFragmentTitle("Fields")
        bottomBar.selectTabById(R.id.tab_fields, true)
    }

    fun navigateToActivitiesFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_in_left,
            R.anim.slide_in_left,
            R.anim.slide_in_right
        )
        transaction.replace(R.id.fl_scroll_view_content, activitiesFragmment)
        transaction.addToBackStack(null)
        transaction.commit()
        topHeaderFragment.setTopHeaderFragmentTitle("Activities")
        bottomBar.selectTabById(R.id.tab_activity, true)
    }
}