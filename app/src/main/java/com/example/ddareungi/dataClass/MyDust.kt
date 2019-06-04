package com.example.ddareungi.dataClass
/*
* pm10: 미세먼지 농도
* pm25: 초미세먼지 농도
* idex_nm: 통합대기환경등급 ex. 좋음, 보통 , ...
* idex_mval: 통합대기환경지수
* */
data class MyDust(var pm10:Double, var pm25:Double, var idex_nm:String,var idex_mvl:Double, var localty:String) {
}