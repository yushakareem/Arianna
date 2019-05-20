import org.openweathermap.api.UrlConnectionDataWeatherClient
import org.openweathermap.api.query.*
import java.net.InetAddress
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.model.CityResponse
import org.openweathermap.api.model.currentweather.CurrentWeather
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.*

class Weather() {
    private lateinit var ipLocalization : CityResponse
    init {
        val whatismyip = URL("http://checkip.amazonaws.com")
        val ipStream = BufferedReader(InputStreamReader(
                whatismyip.openStream()))

        val ip = ipStream.readLine()
        val database = File("/Users/tommasaso/Documents/Tesi/Arianna/src/main/resources/DbGeoIP2/GeoLite2-City.mmdb")
        val reader = DatabaseReader.Builder(database).build()
        val ipAddress = InetAddress.getByName(ip)
        ipLocalization = reader.city(ipAddress)
    }


    fun getWeather() : CurrentWeather{
        val client = UrlConnectionDataWeatherClient("a2e34f3454529599de1f4a3b602a6505")
        val currentWeatherOneLocationQuery = QueryBuilderPicker.pick()
                .currentWeather()                                               // get current weather
                .oneLocation()                                                  // for one location
                .byCityName(ipLocalization.city.names.getValue("en"))      // city name
                .countryCode(ipLocalization.country.isoCode)        // in Country Code
                .type(Type.ACCURATE)                // with Accurate search
                .language(Language.ENGLISH)         // in English language
                .responseFormat(ResponseFormat.JSON)// with JSON response format
                .unitFormat(UnitFormat.METRIC)      // in metric units
                .build()
        val currentWeather = client.getCurrentWeather(currentWeatherOneLocationQuery)

        return currentWeather
    }


}