package com.dioses.snapshots.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dioses.snapshots.R
import com.dioses.snapshots.SnapshotsApplication
import com.dioses.snapshots.databinding.FragmentProfileBinding
import com.dioses.snapshots.utils.FragmentAux
import com.firebase.ui.auth.AuthUI

class ProfileFragment : Fragment(), FragmentAux {

    private lateinit var mBinding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentProfileBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
        setupButton()
    }

    private fun setupButton() {
        mBinding.btnLogout.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        context?.let {
            AuthUI.getInstance().signOut(it).addOnCompleteListener {
                Toast.makeText(
                    context,
                    getString(R.string.profile_logout_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun refresh() {
        with(mBinding) {
            tvName.text = SnapshotsApplication.currentUser.displayName
            tvEmail.text = SnapshotsApplication.currentUser.email
        }
    }
}