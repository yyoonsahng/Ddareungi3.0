package com.example.ddareungi

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class RequestHttpURLConnection {

    fun request(_url: String): String {
        val urlConn: HttpURLConnection? = null
        try {
            var url: URL = URL(_url)
            var urlConn = url.openConnection() as HttpURLConnection
            urlConn.requestMethod = "GET"
            urlConn.setRequestProperty("Accept-Charset", "UTF-8")
            urlConn.setRequestProperty("Accept", "application/json")

            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return urlConn.getResponseCode().toString() + "  " + urlConn.responseMessage
            //읽어온 결과물 리턴.
            // 요청한 URL의 출력물을 BufferedReader로 받는다.
            var reader = BufferedReader(InputStreamReader(urlConn.getInputStream(), "UTF-8"))
            var page = ""
            while (true) {
                var line = reader.readLine()
                if (line != null)
                    page += line
                else
                    break
            }
            return page;
        } catch (e: MalformedURLException) { // for URL
            e.printStackTrace()
        } catch (e: IOException) { // for openConnection()
            e.printStackTrace()
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }
        return "ERROR"
    }
}
