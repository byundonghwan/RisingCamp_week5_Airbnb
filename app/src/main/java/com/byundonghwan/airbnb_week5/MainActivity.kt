package com.byundonghwan.airbnb_week5

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.byundonghwan.airbnb_week5.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var mapView : MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource : FusedLocationSource
    private lateinit var viewpager : ViewPager2

    private val recyclerView : RecyclerView by lazy{
        findViewById(R.id.recyclerView)
    }
    private val bottomSheetTitleTextView : TextView by lazy{
        findViewById(R.id.bottomSheetTitleTextView)
    }

    private val viewPagerAdapter: HouseViewPagerAdapter = HouseViewPagerAdapter(itemClicked = {

        val intent = Intent()
            .apply{
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "[지금 이 가격에 예약하세요.!] ${it.title} ${it.price} 사진보기 ${it.imgUrl}")
                type= "text/plain"
            }
        startActivity(Intent.createChooser(intent, null)) // 뷰페이저 아이템 클릭시 공유(Intent.createChooser) 실행.
    })

    private val recyclerAdapter = HouseListAdapter()
    private val currentLocationButton : LocationButtonView by lazy{
        binding.currentLocationButton
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네이버 지도 생성.
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        // 네이버 맵 객체를 가져옴. -> OnMapReadyCallback
        mapView.getMapAsync(this)

        // 뷰페이저 설정.
        viewpager = binding.houseViewPager
        viewpager.adapter = viewPagerAdapter

        // bottom_sheet_dialog 숙박 정보 아이템 리사이클러뷰 연결.
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        // 뷰페이저 숙박 아이템 클릭시
        viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedHouseModel = viewPagerAdapter.currentList[position]

                // 위경도 값으로 지도를 이동.
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat, selectedHouseModel.lng))
                    .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }

        })

    }

    // OnMapReadyCallback의 구현 메소드.
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.586748, 126.974706))
        naverMap.moveCamera(cameraUpdate)


        val uiSetting = naverMap.uiSettings

        //기존 현재 위치 버튼을 위로 수정.
        uiSetting.isLocationButtonEnabled = false
        currentLocationButton.map = naverMap

        // 현재 위치 버튼 클릭시 권한 팝업으로 권한 획득.
        locationSource = FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getHouseListFromAPI()


    }

    // Retrofit api 호출
    private fun getHouseListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(HouseService::class.java).also{
            it.getHouseList()
                .enqueue(object:Callback<HouseDto>, Overlay.OnClickListener {
                    override fun onResponse(call: Call<HouseDto>, response: Response<HouseDto>) {
                        if(response.isSuccessful.not()){
                            return
                        }
                        response.body()?.let{ dto->
                            Log.d("Retrofit", dto.toString())
                            // for-each문으로 데이터 하나씩 가져옴
                            dto.items.forEach { houseModel->
                                // 네이버지도에 마커를 표시.
                                val marker = Marker()
                                marker.position = LatLng(houseModel.lat, houseModel.lng)

                                // 마커 클릭시
                                marker.onClickListener = this@MainActivity

                                marker.map = naverMap
                                marker.tag = houseModel.id
                                marker.icon = MarkerIcons.BLACK
                                marker.iconTintColor = Color.RED
                            }
                            viewPagerAdapter.submitList(dto.items)
                            recyclerAdapter.submitList(dto.items)
                            bottomSheetTitleTextView.text = "${dto.items.size}개의 숙소"
                        }
                    }

                    override fun onFailure(call: Call<HouseDto>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                    override fun onClick(p0: Overlay): Boolean {
                        TODO("Not yet implemented")
                    }


                })
        }
    }

    // overlay는 마커의 총 집합체.
    override fun onClick(overlay: Overlay): Boolean {
        // houseModel의 id값 태그 .
        val selectedModel = viewPagerAdapter.currentList.firstOrNull{
            it.id == overlay.tag // true 반환
        }
        selectedModel?.let{
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewpager.currentItem = position
        }
        return true

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE){
            return
        }

        if(locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)){
            if(!locationSource.isActivated){
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }



}