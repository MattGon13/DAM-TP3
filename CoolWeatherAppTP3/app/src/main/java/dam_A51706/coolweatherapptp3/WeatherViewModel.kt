package dam_A51706.coolweatherapptp3
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL

class WeatherViewModel(application: Application): AndroidViewModel(application) {

    private var _weatherData = MutableLiveData<WeatherData>()
    val weatherInfo: LiveData<WeatherData>
        get() = _weatherData

    private fun WeatherAPI_Call ( lat: Float , long: Float ) : WeatherData {
        val reqString = buildString {
            append ("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m")
        }
        val url = URL( reqString);
        url.openStream().use {
            val request = Gson().fromJson(InputStreamReader (it ,"UTF-8") ,WeatherData::class.java )
            return request
        }
    }

    fun fetchWeatherData ( lat: Float , long : Float ) : Thread {
        return Thread {
            val weather = WeatherAPI_Call (lat , long )
            _weatherData.postValue(weather)
        }
    }
}