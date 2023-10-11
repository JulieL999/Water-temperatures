package com.example.watertemperatures

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.Instant

class HelperClassForGettingTemperatures {
    fun prepareAPICall(coord: LatLng): String {
        // fetch temperature from API!
        // https://api.tomorrow.io/v4/weather/forecast?location=42.3478,-71.0466&apikey=fdyRHqGggkm6gI4nM4LX6M89sobGS63N'
        val startInstant = Instant.now() // Replace this with your actual start time
        val endInstant = Instant.now() // Replace this with your actual end time

        val params = "waterTemperature"
        val queryParams = mapOf(
            "lat" to coord.latitude.toString(),
            "lng" to coord.longitude.toString(),
            "params" to params,
            "start" to startInstant.toEpochMilli().toString(), // Convert to UTC timestamp
            "end" to endInstant.toEpochMilli().toString()
        ) // Convert to UTC timestamp)
        val urlBuilder = URLBuilder()
        return urlBuilder.buildUrl(
            "https://api.stormglass.io",
            "/v2/weather/point",
            queryParams
        )
    }

    fun makeAPICall(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader(
                "Authorization",
                "9ddde4f8-5fbb-11ee-a654-0242ac130002-9ddde55c-5fbb-11ee-a654-0242ac130002"
            )
            .get()
            .build()

        val responseBody = client.newCall(request).execute().body
        val gsonString = responseBody!!.string()
        Log.i("RESPONSE", gsonString)
        return gsonString
    }


    suspend fun getActualWaterTemperatures(url: String) : Double {
        val scope = CoroutineScope(Dispatchers.Default)
        val result = scope.async {
            withContext(Dispatchers.IO) {
                val resp = makeAPICall(url)
                JSONObject(resp).getJSONArray("hours").getJSONObject(0)
                    .getJSONObject("waterTemperature").getDouble("sg")
            }
        }
        return result.await()
    }
}