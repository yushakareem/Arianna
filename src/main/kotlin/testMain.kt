import org.openweathermap.api.UrlConnectionDataWeatherClient
import org.openweathermap.api.query.*
import java.net.InetAddress
import java.io.File
import com.maxmind.geoip2.DatabaseReader
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.*
import kotlin.random.Random

object testMain {
    @JvmStatic
    fun main(args: Array<String>) {
        /*val w = Weather()
        val myW = w.getWeather()
        println(myW.weather[0])*/
        val randomValues = List(10) { Random.nextInt(0, 10) }
        val a = (1..10).shuffled().subList(0,5)
        println(a)
        println(randomValues)

    }

}