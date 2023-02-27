package com.byundonghwan.airbnb_week5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byundonghwan.airbnb_week5.databinding.ActivityMainBinding
import com.byundonghwan.airbnb_week5.databinding.ItemHouseDetailViewpagerBinding

class HouseViewPagerAdapter(val itemClicked : (HouseModel) -> Unit) : ListAdapter<HouseModel, HouseViewPagerAdapter.ItemViewHolder>(diffUtil) {

    inner class ItemViewHolder(private val binding: ItemHouseDetailViewpagerBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(houseModel: HouseModel){
            binding.titleTextView.text = houseModel.title.orEmpty()
            binding.priceTextView.text = houseModel.price

            // 뷰페이저 아이템 클릭시 공유 기능 활성화.
            binding.root.setOnClickListener {
                itemClicked(houseModel)
            }

            Glide.with(binding.thumbnailImageView).load(houseModel.imgUrl).into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemHouseDetailViewpagerBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        // 리사이클러뷰가 뷰 홀더가 변경되었을 때 바꿀지말지 판단하는 변수
        val diffUtil = object : DiffUtil.ItemCallback<HouseModel>(){

            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem == newItem
            }

        }
    }
}