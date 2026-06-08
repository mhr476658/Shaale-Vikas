package com.shaalevikas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.shaalevikas.adapters.NeedAdapter
import com.shaalevikas.databinding.ActivityNeedsDashboardBinding
import com.shaalevikas.models.SchoolNeed

class NeedsDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNeedsDashboardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NeedAdapter
    private val needsList = mutableListOf<SchoolNeed>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNeedsDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "School Needs Dashboard"
        
        db = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        loadNeeds()
        
        binding.swipeRefresh.setOnRefreshListener {
            loadNeeds()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = NeedAdapter(needsList) { need ->
            // On need click - show pledge dialog or details
            showPledgeDialog(need)
        }
        binding.recyclerViewNeeds.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNeeds.adapter = adapter
    }
    
    private fun loadNeeds() {
        binding.swipeRefresh.isRefreshing = true
        
        db.collection("needs")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                binding.swipeRefresh.isRefreshing = false
                
                if (error != null) {
                    Toast.makeText(this, "Error loading needs: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                needsList.clear()
                value?.documents?.forEach { document ->
                    val need = document.toObject<SchoolNeed>()
                    if (need != null) {
                        need.id = document.id
                        needsList.add(need)
                    }
                }
                adapter.notifyDataSetChanged()
                
                if (needsList.isEmpty()) {
                    binding.tvEmptyState.visibility = android.view.View.VISIBLE
                    binding.recyclerViewNeeds.visibility = android.view.View.GONE
                } else {
                    binding.tvEmptyState.visibility = android.view.View.GONE
                    binding.recyclerViewNeeds.visibility = android.view.View.VISIBLE
                }
            }
    }
    
    private fun showPledgeDialog(need: SchoolNeed) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pledge, null)
        val tvNeedTitle = dialogView.findViewById<TextView>(R.id.tvNeedTitle)
        val tvNeedDesc = dialogView.findViewById<TextView>(R.id.tvNeedDesc)
        val etAmount = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etAmount)
        val etMessage = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMessage)
        
        tvNeedTitle.text = need.title
        tvNeedDesc.text = "Target: ₹${need.targetAmount} | Remaining: ₹${need.remainingAmount}"
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Make a Pledge")
            .setView(dialogView)
            .setPositiveButton("Pledge") { _, _ ->
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                val message = etMessage.text.toString()
                
                if (amount > 0 && amount <= need.remainingAmount) {
                    processPledge(need, amount, message)
                } else {
                    Toast.makeText(this, "Please enter a valid amount (max: ₹${need.remainingAmount})", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        dialog.show()
    }
    
    private fun processPledge(need: SchoolNeed, amount: Double, message: String) {
        val sharedPref = getSharedPreferences("ShaaleVikas", MODE_PRIVATE)
        val alumniName = sharedPref.getString("alumni_name", "Anonymous Alumni")
        val alumniBatch = sharedPref.getString("alumni_batch", "Unknown Batch")
        
        val donor = Donor(
            name = alumniName ?: "Anonymous Alumni",
            batch = alumniBatch ?: "Unknown Batch",
            pledgedAmount = amount,
            needId = need.id,
            needTitle = need.title,
            message = message
        )
        
        // Save donor to Firestore
        db.collection("donors").add(donor)
            .addOnSuccessListener {
                // Update need's collected amount
                val newAmount = need.amountCollected + amount
                db.collection("needs").document(need.id)
                    .update("amountCollected", newAmount)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Thank you for your pledge of ₹$amount!", Toast.LENGTH_LONG).show()
                        
                        // Show success dialog
                        showPledgeSuccessDialog(amount, need.title)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to process pledge: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun showPledgeSuccessDialog(amount: Double, needTitle: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Pledge Successful!")
            .setMessage("Thank you for pledging ₹$amount towards '$needTitle'. Your contribution will help improve education infrastructure in rural schools.")
            .setPositiveButton("View Hall of Fame") { _, _ ->
                startActivity(Intent(this, DonorHallOfFameActivity::class.java))
            }
            .setNegativeButton("Continue") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}