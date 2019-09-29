package com.example.ddareungi.bookmark

import android.util.Log
import com.example.ddareungi.data.Bookmark
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import java.util.*

class BookmarkPresenter(
    val dataRepository: DataRepository,
    val bookmarkView: BookmarkContract.View,
    val locationPermissionGranted: Boolean
) : BookmarkContract.Presenter {

    lateinit var bookmarks: ArrayList<Bookmark>
    var firstLoad = true

     var isWeather=false
     var isBike=false
    var isAll=false
    init {
        bookmarkView.presenter = this
    }

    override fun start() {
        bookmarks = dataRepository.bookmarkDataBase.getAllUser()
        loadData()
        firstLoad = false
    }

    override fun loadData() {
        //SplashActivity에서 데이터 받아오는데 실패한 경우에 isReposInit = false
        val isReposInit = dataRepository.isReposInit
        dataRepository.networkState=true
        //fragment가 생성될 때 호출
        if (firstLoad) {
            //데이터베이스에서 북마크된 정류소 불러와서 자전거 수 정보 업데이트
            loadBookmarks()

            //정상적으로 데이터를 받아온 경우
            if (isReposInit) {
                //날씨 이미지, 미세먼지 텍스트 세팅
                setWeatherViews()
            } else {
                //정상적으로 데이터를 받아오지 못한 경우
                bookmarkView.showCheckNetwork()
            }
        }
        //새로고침 버튼 누른 경우
        else {
            //지역 정보 업데이트
            bookmarkView.showLoadingIndicator(true, true)
            bookmarkView.initLocation(dataRepository) //리스터 추가해서 전체적으로 fail이 한번만 나도 아예 fail로 처리
            //외부 데이터를 한 번은 정상적으로 받아왔을 때
            if (isReposInit) {
                bookmarkView.showLoadingIndicator(true, true)
                //날씨, 지역 정보만 웹에 요청
                dataRepository.refreshBike(object : DataSource.LoadDataCallback {
                    override fun onDataLoaded() {
                        isBike=true
                        isAll=true
                        if(isBike && isWeather) {
                            bookmarkView.showLoadingIndicator(false, false)
                            setWeatherViews()
                            loadBookmarks()
                        }
                    }

                    override fun onNetworkNotAvailable() {
                        bookmarkView.showLoadingIndicator(false, false)
                        bookmarkView.showCheckNetwork()
                        bookmarkView.showLoadDataError()
                    }

                })
            } else {

                //외부 데이터를 가지고 있는게 없으므로 전체 데이터를 웹에 요청

                dataRepository.initRepositoryForBookmarkFrag(object : DataSource.LoadDataCallback {
                    override fun onDataLoaded() {
                        isAll=true
                        isBike=true
                        if(isWeather&&isAll) {
                            bookmarkView.showLoadingIndicator(false, false)
                            setWeatherViews()
                            loadBookmarks()
                        }
                    }

                    override fun onNetworkNotAvailable() {
                        bookmarkView.showLoadingIndicator(false, false)
                        bookmarkView.showCheckNetwork()
                        bookmarkView.showLoadDataError()
                    }

                })
            }
            isAll=false
            isWeather=false
            isBike=false
        }
    }

    override fun loadBookmarks() {
        for (bookmark in bookmarks) {
            for (bike in dataRepository.bikeList) {
                if (bookmark.rentalOffice == bike.stationName) {
                    bookmark.leftBike = bike.parkingBikeTotCnt
                    break
                }
            }
        }
        if (bookmarks.isEmpty()) {
            bookmarkView.showNoBookmark(bookmarks)
            bookmarkView.showLoadingByBikeStatus(false, true)
        } else {
            bookmarkView.showBookmarkedList(bookmarks)
            bookmarkView.showLoadingByBikeStatus(false, false)
        }
    }

    override fun deleteBookmark(deletedBookmarkPosition: Int) {
        dataRepository.deleteBookmarkInDatabase(deletedBookmarkPosition)

        if (bookmarks.isEmpty()) {
            bookmarkView.showNoBookmark(bookmarks)

        } else {
            bookmarkView.showBookmarkedList(bookmarks)

        }
    }

    override fun openMapFrag(clickedRentalOffice: String) {
        bookmarkView.showClickedBookmarkInMapFrag(dataRepository, clickedRentalOffice)
    }

    override fun setWeatherViews() {
        val neighborhoodText = "현재 ${dataRepository.weather.neighborhood}은"
        val dustText =
            "${dataRepository.weather.temp}℃  ${dataRepository.weather.wfKor}\n미세먼지는 ${dataRepository.dust.idex_nm}입니다"
        val imageId = dataRepository.weather.matchImage()

        bookmarkView.showWeatherView(neighborhoodText, dustText, imageId)
    }

    override fun processLocation(
        locality: String,
        neighborhood: String,
        weatherFile: Scanner,
        dustFile: Scanner
    ) {
        dataRepository.initLocationCode(weatherFile, dustFile, locality, neighborhood)
    }

    override fun setIsWeather(){ isWeather=!isWeather}
    override fun getIsAll(): Boolean {return isAll}
    override fun getIsWeather():Boolean{return isWeather}
    override fun getIsBike():Boolean{return isBike}
}