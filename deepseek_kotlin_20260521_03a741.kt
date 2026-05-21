package com.shaalevikas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shaalevikas.databinding.ActivityAddNeedBinding
import com.shaalevikas.models.SchoolNeed
import com.shaalevikas.models.NeedStatus
import java.util.UUID

class AddNeedActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAddNeedBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add New Need"
        
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        
        binding.btnSelectImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        
        binding.btnSubmit.setOnClickListener {
            submitNeed()
        }
    }
    
    private fun submitNeed() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val targetAmount = binding.etTargetAmount.text.toString().toDoubleOrNull() ?: 0.0
        
        if (title.isEmpty() || description.isEmpty() || targetAmount <= 0) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedImageUri == null) {
            saveNeedToFirestore(title, description, targetAmount, "")
            return
        }
        
        // Upload image first
        uploadImage { imageUrl ->
            saveNeedToFirestore(title, description, targetAmount, imageUrl)
        }
    }
    
    private fun uploadImage(onSuccess: (String) -> Unit) {
        val imageRef = storage.reference.child("need_images/${UUID.randomUUID()}.jpg")
        
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                onSuccess("")
            }
    }
    
    private fun saveNeedToFirestore(title: String, description: String, targetAmount: Double, imageUrl: String) {
        val need = SchoolNeed(
            title = title,
            description = description,
            targetAmount = targetAmount,
            amountCollected = 0.0,
            imageUrl = imageUrl,
            status = NeedStatus.PENDING
        )
        
        db.collection("needs").add(need)
            .addOnSuccessListener {
                Toast.makeText(this, "Need added successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add need: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.ivSelectedImage.setImageURI(selectedImageUri)
            binding.ivSelectedImage.visibility = android.view.View.VISIBLE
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}