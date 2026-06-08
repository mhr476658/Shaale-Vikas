package com.shaalevikas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.shaalevikas.databinding.ActivityDonorHallBinding
import com.shaalevikas.models.Donor

class DonorHallOfFameActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDonorHallBinding
    private lateinit var db: FirebaseFirestore
    private val donorsList = mutableListOf<Donor>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonorHallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Donor Hall of Fame"
        
        db = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        loadDonors()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewDonors.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadDonors() {
        db.collection("donors")
            .orderBy("pledgedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading donors: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                donorsList.clear()
                value?.documents?.forEach { document ->
                    val donor = document.toObject(Donor::class.java)
                    if (donor != null) {
                        donor.id = document.id
                        donorsList.add(donor)
                    }
                }
                
                updateUI()
            }
    }
    
    private fun updateUI() {
        if (donorsList.isEmpty()) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerViewDonors.visibility = android.view.View.GONE
            return
        }
        
        binding.tvEmptyState.visibility = android.view.View.GONE
        binding.recyclerViewDonors.visibility = android.view.View.VISIBLE
        
        // Simple display for demo - you can create a proper adapter
        val donorText = donorsList.joinToString("\n\n") { donor ->
            "🏆 ${donor.name} (Batch ${donor.batch})\n" +
            "💝 Pledged ₹${donor.pledgedAmount} for: ${donor