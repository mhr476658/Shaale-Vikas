package com.shaalevikas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shaalevikas.adapters.NeedAdapter
import com.shaalevikas.databinding.ActivityMainBinding
import com.shaalevikas.models.SchoolNeed
import com.shaalevikas.models.NeedStatus

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupUI()
        loadStats()
        loadRecentNeeds()
        
        // Check if user is admin (headmaster)
        checkAdminStatus()
    }
    
    private fun setupUI() {
        binding.fabAddNeed.setOnClickListener {
            if (isAdmin()) {
                startActivity(Intent(this, AddNeedActivity::class.java))
            } else {
                Toast.makeText(this, "Only Headmaster can add needs", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnViewAllNeeds.setOnClickListener {
            startActivity(Intent(this, NeedsDashboardActivity::class.java))
        }
        
        binding.btnDonorHall.setOnClickListener {
            startActivity(Intent(this, DonorHallOfFameActivity::class.java))
        }
        
        binding.cardAlumniLogin.setOnClickListener {
            showAlumniLoginDialog()
        }
    }
    
    private fun loadStats() {
        db.collection("needs")
            .get()
            .addOnSuccessListener { documents ->
                val totalNeeds = documents.size()
                val completedNeeds = documents.count { 
                    it.toObject(SchoolNeed::class.java).status == NeedStatus.COMPLETED 
                }
                val totalRaised = documents.sumOf { 
                    it.toObject(SchoolNeed::class.java).amountCollected 
                }
                
                binding.tvTotalNeeds.text = totalNeeds.toString()
                binding.tvCompletedProjects.text = completedNeeds.toString()
                binding.tvTotalRaised.text = "₹${String.format("%.2f", totalRaised)}"
            }
    }
    
    private fun loadRecentNeeds() {
        db.collection("needs")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documents ->
                val needs = documents.map { it.toObject(SchoolNeed::class.java) }
                // Display in recent needs section
                binding.tvRecentNeeds.text = needs.joinToString("\n") { "• ${it.title}" }
            }
    }
    
    private fun checkAdminStatus() {
        // For demo purposes, check if email is headmaster@school.com
        val user = auth.currentUser
        if (user?.email == "headmaster@school.com") {
            binding.fabAddNeed.show()
        } else {
            binding.fabAddNeed.hide()
        }
    }
    
    private fun isAdmin(): Boolean {
        return auth.currentUser?.email == "headmaster@school.com"
    }
    
    private fun showAlumniLoginDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alumni_login, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Alumni Login / Register")
            .setView(dialogView)
            .setPositiveButton("Continue") { _, _ ->
                val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etName).text.toString()
                val batch = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etBatch).text.toString()
                
                if (name.isNotBlank()) {
                    // Store alumni info in SharedPreferences
                    getSharedPreferences("ShaaleVikas", MODE_PRIVATE).edit().apply {
                        putString("alumni_name", name)
                        putString("alumni_batch", batch)
                        apply()
                    }
                    Toast.makeText(this, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
}