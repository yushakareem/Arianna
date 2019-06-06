import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.NetworkInterface
import jdk.nashorn.tools.ShellFunctions.input
import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
import org.apache.commons.io.IOUtils
import java.io.StringWriter


object testMain {
    @JvmStatic
    fun main(args: Array<String>) {
        /*val weather = Weather()
        val actualW = weather.getWeather()

        println(actualW.weather[0].id.toInt())*/


        val url2 = URL("https://vocalinterface.firebaseio.com/installation_test_name/VocalInterface.json")
        val conn2 = url2.openConnection() as HttpURLConnection
        //conn2.requestMethod = "PATCH"
        conn2.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn2.doOutput = true
        conn2.setRequestProperty("Content-Type", "application/json")
        conn2.setRequestProperty("Accept", "application/json")
        val input = "{\"ViHasIP\": \"puppola\"}"

        val os = conn2.getOutputStream()
        os.write(input.toByteArray())
        os.flush()

        /*if (conn2.responseCode != HttpURLConnection.HTTP_CREATED) {
            throw RuntimeException("Failed : HTTP error code : " + conn2.responseCode)
        }*/

        val br = BufferedReader(InputStreamReader(conn2.inputStream))

        var output: String
        println("Output from Server .... \n")
        do{
            output = br.readLine()
            println(output)
        }while(output != null)

        conn2.disconnect()






        val result = StringBuilder()
        val url = URL("https://vocalinterface.firebaseio.com/installation_test_name/VocalInterface/sotaHasIP.json")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"


        val writer = StringWriter()
        IOUtils.copy(conn.inputStream, writer)
        val theString = writer.toString()
        println(theString)







    }
}


