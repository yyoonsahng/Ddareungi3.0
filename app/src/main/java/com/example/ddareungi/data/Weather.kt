package com.example.ddareungi.data

/**
 * [temp] : 현재 시간 온도
 * [sky] : 하늘 상태 코드(1: 맑음, 2: 구름 조금, 3: 구름 많음, 4: 흐림
 * [pty] : 강수 상태 코드(0: 없음, 1: 비, 2: 비/눈, 3: 눈)
 * [wfKor] : 날씨(한국어, Ex) 흐림, 눈/비, 눈, ...)
 * [pop] : 강수 확률(%)
 */
data class Weather(var temp: Int, var sky: Int, var pty: Int, var wfKor: String, var pop: Int,
                   var code: String, var neighborhood: String)
