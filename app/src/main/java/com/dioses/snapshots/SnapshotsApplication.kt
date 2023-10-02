package com.dioses.snapshots

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class SnapshotsApplication : Application() {
    companion object {
        const val RC_GALLERY = 18
        const val PATH_SNAPSHOT = "snapshots"

        lateinit var currentUser: FirebaseUser
    }
}