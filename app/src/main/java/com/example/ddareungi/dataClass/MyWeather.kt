package com.example.ddareungi.dataClass
/*
* temp: 현재 시간 온도
* sky: 하늘상태코드(1: 맑음, 2: 구름조금 , 3: 구름 많음, 4: 흐림
* pty: 강수상태코드(0: 없음 1: 비 , 2: 비/눈 , 3: 눈)
* wfKor:날씨 한국어 ex. 흐림, 눈/비, 눈 , 등등등
* pop: 강수 확률 (퍼센트기준임)
* */
data class MyWeather(var temp:Int, var sky:Int, var pty:Int, var wfKor:String,var pop:Int) {
}