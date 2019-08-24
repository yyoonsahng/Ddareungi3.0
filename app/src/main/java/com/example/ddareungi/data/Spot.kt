package com.example.ddareungi.data

/*
지역기반 제공정보
* addr1: 주소(서울 중구 다동)
* createdtime: 정보 등록일
* firstimage: 대표이미지(원본)(500*333 size)
* firstimage2: 대표이미지(썸네일) ( 150*100 size)
* mapx: gps x좌표 (WGS84 경도 좌표)
* mapy: gps y좌표 (WGS84 위도 좌표)
* modifiedtime: 정보 수정일
* readcount: 조회수
* sigungucode: 지역구 코드(강남-1 , 강동 -2 ,...)
* contentid: 컨텐츠 id 번호
* title: 제목
*
공통정보조회 제공 정보
* homepage: 홈페이지 주소
* tel: 전화번호
* overview: 해당 관광지 설명(개요)
*
추가해야할 것
* 주변 따릉이 대여소 정보
*
*  */
data class Spot(val contentid:Int, var imgOrigin:String, var imgThumb:String, val mapX:Double, val mapY:Double, val title:String, var tel:String, var homepage:String, var overview:String/*주변 대여소 ,val bikeStop:ArrayList<Bike> */) {

    fun deleteTag(){ //overview 태그 없애는 클래스
        this.overview=this.overview
            .replace("<br />","\n")
            .replace("&lt;","")
            .replace("<br>","\n")
            .replace("<strong>","")
            .replace("</strong>","")
            .replace("&gt;","")
            .replace("<b>","")
            .replace("</b>","")
            .replace("&middot;","∙")
    }
}