package com.byundonghwan.airbnb_week5

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.byundonghwan.airbnb_week5.databinding.ActivityMainBinding
import com.byundonghwan.airbnb_week5.databinding.ItemHouseBinding
import com.byundonghwan.airbnb_week5.databinding.ItemHouseDetailViewpagerBinding

class HouseListAdapter : ListAdapter<HouseModel, HouseListAdapter.ItemViewHolder>(diffUtil) {

    inner class ItemViewHolder(private val binding: ItemHouseBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(houseModel: HouseModel){
            binding.titleTextView.text = houseModel.title.orEmpty()
            binding.priceTextView.text = houseModel.price
            Glide.with(binding.thumbnailImageView).load(houseModel.imgUrl)
                .transform(CenterCrop(), RoundedCorners(dpTopx(binding.thumbnailImageView.context, 12)))
                .into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemHouseBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    // 픽셀을 dp로 변환하는 함수.
    private fun dpTopx(context : Context, dp : Int):Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
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