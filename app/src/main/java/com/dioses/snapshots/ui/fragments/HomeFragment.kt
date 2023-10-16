package com.dioses.snapshots.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dioses.snapshots.utils.FragmentAux
import com.dioses.snapshots.R
import com.dioses.snapshots.SnapshotsApplication.Companion.PATH_SNAPSHOT
import com.dioses.snapshots.SnapshotsApplication.Companion.PROPERTY_LIKE_LIST
import com.dioses.snapshots.entities.Snapshot
import com.dioses.snapshots.databinding.FragmentHomeBinding
import com.dioses.snapshots.databinding.ItemSnapshotBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment(), FragmentAux {

    private lateinit var mBinding: FragmentHomeBinding
    private lateinit var mFirebaseAdapter: FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var mSnapshotsRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentHomeBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebase()
        setupAdapter()
        setupRecyclerView()
    }

    private fun setupFirebase() {
        mSnapshotsRef = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun setupAdapter() {
        val query = mSnapshotsRef
        val options =
            FirebaseRecyclerOptions.Builder<Snapshot>().setQuery(query) {
                val snapshot = it.getValue(Snapshot::class.java)
                snapshot!!.id = it.key!!
                snapshot
            }.build()

        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Snapshot, SnapshotHolder>(options) {
            private lateinit var mContext: Context
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SnapshotHolder {
                mContext = parent.context
                val view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_snapshot, parent, false)
                return SnapshotHolder(view)
            }

            override fun onBindViewHolder(holder: SnapshotHolder, position: Int, model: Snapshot) {
                val snapshot = getItem(position)
                with(holder) {
                    setListener(snapshot)
                    binding.tvTitle.text = snapshot.title
                    binding.cbLike.text = snapshot.likeList.keys.size.toString()
                    FirebaseAuth.getInstance().currentUser?.let {
                        binding.cbLike.isChecked =
                            snapshot.likeList.containsKey(it.uid)
                    }
                    Glide.with(mContext)
                        .load(snapshot.photoUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(binding.imgPhoto)
                }
            }

            override fun onDataChanged() {
                super.onDataChanged()
                mBinding.progressBar.visibility = View.GONE
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupRecyclerView() {
        mLayoutManager = LinearLayoutManager(context)

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            itemAnimator = null
            layoutManager = mLayoutManager
            adapter = mFirebaseAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter.stopListening()
    }

    override fun refresh() {
        mBinding.recyclerView.smoothScrollToPosition(0)
    }

    private fun deleteSnapshot(snapshot: Snapshot) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialog_delete_title)
                .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                    mSnapshotsRef.child(snapshot.id).removeValue()
                }
                .setNegativeButton(R.string.dialog_delete_cancel, null)
                .show()
        }
    }

    private fun setLike(snapshot: Snapshot, checked: Boolean) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
        if (checked) {
            databaseReference.child(snapshot.id).child(PROPERTY_LIKE_LIST)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(checked)
        } else {
            databaseReference.child(snapshot.id).child(PROPERTY_LIKE_LIST)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(null)
        }
    }

    inner class SnapshotHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemSnapshotBinding.bind(view)

        fun setListener(snapshot: Snapshot) {
            binding.btnDelete.setOnClickListener {
                deleteSnapshot(snapshot)
            }
            binding.cbLike.setOnCheckedChangeListener { _, checked ->
                setLike(snapshot, checked)
            }
        }
    }

}