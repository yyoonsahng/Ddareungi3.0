package com.example.ddareungi.bookmark

import com.example.ddareungi.bookmark.BookmarkContract
import com.example.ddareungi.data.Bookmark
import com.example.ddareungi.data.source.DataRepository
import com.example.ddareungi.data.source.DataSource
import java.util.*

class BookmarkPresenter(val dataRepository: DataRepository, val bookmarkView: BookmarkContract.View, val locationPermissionGranted: Boolean)
    : BookmarkContract.Presenter {

    lateinit var bookmarks: ArrayList<Bookmark>
    var firstLoad = true

    init {
        bookmarkView.presenter = this
    }

    override fun start() {
        dataRepository.addBookmarkToDatabase("108. 서교동 사거리")
        dataRepository.addBookmarkToDatabase("729. 서부식자재마트 건너편")
        bookmarks = dataRepository.bookmarkDataBase.getAllUser()
        loadData()
        firstLoad = false
    }

    override fun loadData() {
        //SplashActivity에서 데이터 받아오는데 실패한 경우에 isReposInit = false
        val isReposInit = dataRepository.isReposInit

        //fragment가 생성될 때 호출
        if(firstLoad) {
            //데이터베이스에서 북마크된 정류소 불러와서 자전거 수 정보 업데이트
            loadBookmarks()

            //정상적으로 데이터를 받아온 경우
            if(isReposInit) {
                //날씨 이미지, 미세먼지 텍스트 세팅
                setWeatherViews()
            }
            else {
                //정상적으로 데이터를 받아오지 못한 경우
                bookmarkView.showCheckNetwork()
            }
        }
        //새로고침 버튼 누른 경우
        else
        {
            //지역 정보 업데이트
            bookmarkView.initLocation(locationPermissionGranted)
            //외부 데이터를 한 번은 정상적으로 받아왔을 때
            if(isReposInit) {
                bookmarkView.showLoadingIndicator(true, false)
                //날씨, 지역 정보만 웹에 요청
                dataRepository.refreshForBookmarkFrag(object: DataSource.LoadDataCallback {

                    override fun onDataLoaded() {
                        setWeatherViews()

                        loadBookmarks()

                        bookmarkView.showLoadingIndicator(false, false)
                    }

                    override fun onNetworkNotAvailable() {
                        bookmarkView.showLoadingIndicator(false, false)

                        bookmarkView.showLoadDataError()
                    }

                })
            }
            else {
                bookmarkView.showLoadingIndicator(true, true)
                //외부 데이터를 가지고 있는게 없으므로 전체 데이터를 웹에 요청
                dataRepository.initRepository(object: DataSource.LoadDataCallback {
                    override fun onDataLoaded() {
                        setWeatherViews()

                        loadBookmarks()

                        bookmarkView.showLoadingIndicator(false, false)
                    }

                    override fun onNetworkNotAvailable() {
                        bookmarkView.showLoadingIndicator(false, false)

                        bookmarkView.showCheckNetwork()

                        bookmarkView.showLoadDataError()
                    }

                })
            }
        }
    }

    private fun loadBookmarks() {
        for(bookmark in bookmarks) {
            for(bike in dataRepository.bikeList) {
                if(bookmark.rentalOffice == bike.stationName) {
                    bookmark.leftBike = bike.parkingBikeTotCnt
                    break
                }
            }
        }
        if(bookmarks.isEmpty()) {
            bookmarkView.showNoBookmark(bookmarks)
        } else {
            bookmarkView.showBookmarkedList(bookmarks)
        }
    }

    override fun deleteBookmark(deletedBookmarkPosition: Int) {
        dataRepository.deleteBookmarkInDatabase(deletedBookmarkPosition)
        //bookmarks.removeAt(deletedBookmarkPosition)

        if(bookmarks.isEmpty()) {
            bookmarkView.showNoBookmark(bookmarks)
        } else {
            bookmarkView.showBookmarkedList(bookmarks)
        }
    }

    override fun openMapFrag(clickedRentalOffice: String) {
        bookmarkView.showClickedBookmarkInMapFrag(dataRepository, clickedRentalOffice)
    }

    fun setWeatherViews() {
        val dustText = "${dataRepository.weather.neighborhood}의 미세먼지는\n${dataRepository.dust.idex_nm}입니다"
        val imageId = dataRepository.weather.matchImage()

        bookmarkView.showWeatherView(dustText, imageId)
    }

    override fun processLocation(locality: String, neighborhood: String, weatherFile: Scanner, dustFile: Scanner) {
        dataRepository.initLocationCode(weatherFile, dustFile, locality, neighborhood)
    }
}