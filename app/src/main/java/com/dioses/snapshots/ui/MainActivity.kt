package com.dioses.snapshots.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.dioses.snapshots.utils.FragmentAux
import com.dioses.snapshots.R
import com.dioses.snapshots.SnapshotsApplication
import com.dioses.snapshots.databinding.ActivityMainBinding
import com.dioses.snapshots.ui.fragments.AddFragment
import com.dioses.snapshots.ui.fragments.HomeFragment
import com.dioses.snapshots.ui.fragments.ProfileFragment
import com.dioses.snapshots.utils.MainAux
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), MainAux {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mActiveFragment: Fragment
    private var mFragmentManager: FragmentManager? = null
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    private val authResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.welcome), Toast.LENGTH_SHORT).show()
            } else {
                if (IdpResponse.fromResultIntent(it.data) == null) {
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setupAuth()
    }

    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                authResult.launch(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            listOf(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                        ).build()
                )
            } else {
                SnapshotsApplication.currentUser = it.currentUser!!
                val fragmentProfile =
                    mFragmentManager?.findFragmentByTag(ProfileFragment::class.java.name)
                fragmentProfile?.let {
                    (it as FragmentAux).refresh()
                }
                if (mFragmentManager == null) {
                    mFragmentManager = supportFragmentManager
                    setupBottomNav(mFragmentManager!!)
                }
            }
        }
    }

    private fun setupBottomNav(fragmentManager: FragmentManager) {

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        fragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment).commit()
        fragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name).hide(addFragment)
            .commit()
        fragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name)
            .hide(homeFragment)
            .commit()

        mBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment)
                        .commit()
                    mActiveFragment = homeFragment
                    true
                }

                R.id.action_add -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment)
                        .commit()
                    mActiveFragment = addFragment
                    true
                }

                R.id.action_profile -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment)
                        .commit()
                    mActiveFragment = profileFragment
                    true
                }

                else -> false
            }
        }
        mBinding.bottomNav.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.action_home -> (homeFragment as FragmentAux).refresh()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    /*
     *   MainAux
     * */
    override fun showMessage(resId: Int, duration: Int) {
        Snackbar.make(mBinding.root, resId, duration)
            .setAnchorView(mBinding.bottomNav)
            .show()
    }
}