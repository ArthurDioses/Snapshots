package com.dioses.snapshots

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Snapshot(
    val id: String = "",
    var title: String = "",
    var photoUrl: String = "",
    var likeLis: Map<String, Boolean> = mutableMapOf()
)
