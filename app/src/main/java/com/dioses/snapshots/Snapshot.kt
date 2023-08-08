package com.dioses.snapshots

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Snapshot(
    var id: String = "",
    var title: String = "",
    var photoUrl: String = "",
    var likeLis: Map<String, Boolean> = mutableMapOf()
)
