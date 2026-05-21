package com.shaalevikas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shaalevikas.databinding.ItemNeedBinding
import com.shaalevikas.models.SchoolNeed
import com.shaalevikas.models.NeedStatus

class NeedAdapter(
    private val needs: List<SchoolNeed>,
    private val onItemClick: (SchoolNeed) -> Unit
) : RecyclerView.Adapter<NeedAdapter.NeedViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedViewHolder {
        val binding = ItemNeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NeedViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: NeedViewHolder, position: Int) {
        holder.bind(needs[position])
    }
    
    override fun getItemCount(): Int = needs.size
    
    inner class NeedViewHolder(private val binding: ItemNeedBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(need: SchoolNeed) {
            binding.tvTitle.text = need.title
            binding.tvDescription.text = need.description
            binding.tvCost.text = "Target: ₹${need.targetAmount}"
            binding.tvCollected.text = "Collected: ₹${need.amountCollected}"
            binding.progressBar.progress = need.progressPercentage
            binding.tvProgress.text = "${need.progressPercentage}% Funded"
            
            when (need.status) {
                NeedStatus.COMPLETED -> {
                    binding.statusChip.text = "Completed"
                    binding.statusChip.chipBackgroundColor = 
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))
                }
                NeedStatus.IN_PROGRESS -> {
                    binding.statusChip.text = "In Progress"
                    binding.statusChip.chipBackgroundColor = 
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF9800"))
                }
                else -> {
                    binding.statusChip.text = "Pending"
                    binding.statusChip.chipBackgroundColor = 
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336"))
                }
            }
            
            // Load image if available
            if (need.imageUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(need.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivNeedImage)
            }
            
            binding.root.setOnClickListener {
                onItemClick(need)
            }
        }
    }
}