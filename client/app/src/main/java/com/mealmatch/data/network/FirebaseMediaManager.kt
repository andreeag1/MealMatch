package com.mealmatch.data.network

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseMediaManager {
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    fun uploadMedia(
        uri: Uri,
        storagePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child(storagePath)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun deleteMedia(
        storagePath: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child(storagePath)
        fileRef.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}