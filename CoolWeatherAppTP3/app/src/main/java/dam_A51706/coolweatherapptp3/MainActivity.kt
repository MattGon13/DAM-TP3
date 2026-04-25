package dam_A51706.coolweatherapptp3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dam_A51706.coolweatherapptp3.databinding.ActivityMainBinding
import kotlin.collections.get
import kotlin.text.get


class MainActivity : AppCompatActivity() {
    val day= true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {

        when ( resources . configuration . orientation ) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) {
                    setTheme (R.style.Theme_Day )
                } else {
                    setTheme (R.style.Theme_Night )
                }
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) {
                    setTheme(R.style.Theme_Day_Land )
                } else {
                    setTheme(R.style.Theme_Night_Land )
                }
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        weatherViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(WeatherViewModel::class.java)
        weatherViewModel.weatherInfo.observe(this) {weatherData -> updateUI(weatherData)}

        val latitudeInput: EditText =  binding.latitudeInput
        val longitudeInput: EditText = binding.longitudeInput
        val updateButton: Button = binding.button

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)
        getLocation(latitudeInput,longitudeInput)

        updateButton.setOnClickListener {
            val lat = latitudeInput.text.toString().toFloatOrNull()
            val log = longitudeInput.text.toString().toFloatOrNull()
            if(lat != null && log != null){
                weatherViewModel.fetchWeatherData(lat, log).start()
            }
        }
    }

    private fun getLocation(inputLat: EditText, inputLog: EditText): MutableList<String>{
        val latlogValues = mutableListOf<String>()
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.getResult()
                    if(location != null){
                        val lat = location.latitude.toFloat()
                        val log = location.longitude.toFloat()

                        inputLat.setText(lat.toString())
                        inputLog.setText(log.toString())

                        weatherViewModel.fetchWeatherData(lat, log).start()
                    }
                }
            }
            else{
                Toast.makeText(this, getString(R.string.please_turn_location_on), Toast.LENGTH_LONG).show()
                val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }
        return latlogValues
    }

    private fun updateUI ( request : WeatherData ) {
        runOnUiThread {
            val weatherImage:ImageView = binding.weatherImage
            val pressure:TextView = binding.seaLevelValue
            val windDirection:TextView = binding.windDirectionValue
            val windSpeed:TextView = binding.windSpeedValue
            val temperature:TextView = binding.temperatureValue
            val time:TextView = binding.timeValue

            pressure.text = "${request.hourly.pressure_msl.get(12)} hPa"
            windDirection.text = "${request.current_weather.winddirection}"
            windSpeed.text = "${request.current_weather.windspeed} km/h"
            temperature.text = "${request.current_weather.temperature} ºC"
            time.text = "${request.current_weather.time}"

            val mapt = getWeatherInfoCodeMap(this)
            val wCode = mapt[request.current_weather.weathercode]
            val wImage = when (wCode?.description) {
                "CLEAR_SKY",
                "MAINLY_CLEAR",
                "PARTLY_CLOUDY" -> if(day) wCode?.image + "day" else wCode?.image + " night "
                else -> wCode?.image
            }
            val res = getResources()
            val resID = res.getIdentifier(wImage,"drawable", packageName )
            if(resID != 0){
                weatherImage.setImageResource(resID)
            }
            else{
                weatherImage.setImageResource(R.drawable.fog)
            }
        }
    }
}