package ua.ies.myweatherradar.app;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.ies.myweatherradar.app.IpmaCityForecast;
import ua.ies.myweatherradar.app.IpmaService;
import java.util.logging.Logger;
import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * demonstrates the use of the IPMA API for weather forecast
 */
public class WeatherStarter {

    private static String CITY_NAME = "Aveiro";
    private static int CITY_ID = 1010550;
    /*
    loggers provide a better alternative to System.out.println
    https://rules.sonarsource.com/java/tag/bad-practice/RSPEC-106
     */
    private static final Logger logger = Logger.getLogger(WeatherStarter.class.getName());

    public static void  main(String[] args ) {

        CITY_NAME = args[0];
        /*
        get a retrofit instance, loaded with the GSon lib to convert JSON into objects
         */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.ipma.pt/open-data/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String sURL = "https://api.ipma.pt/open-data/distrits-islands.json"; //just a string

        try {
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();
        
            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootobj = root.getAsJsonObject();
            JsonArray data = rootobj.getAsJsonArray("data");
            Iterator<JsonElement> cities_it = data.iterator();
            
            while (cities_it.hasNext()){
                JsonElement city = cities_it.next();
                JsonObject city_obj = city.getAsJsonObject();
                int id = city_obj.get("globalIdLocal").getAsInt();
                String name = city_obj.get("local").getAsString();
                if (name.equals(CITY_NAME)){
                    CITY_ID = id;
                    break;
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        IpmaService service = retrofit.create(IpmaService.class);
        Call<IpmaCityForecast> callSync = service.getForecastForACity(CITY_ID);

        try {
            Response<IpmaCityForecast> apiResponse = callSync.execute();
            IpmaCityForecast forecast = apiResponse.body();

            if (forecast != null) {
                logger.info( 
                    "Tempo para hoje:\n" + 
                    "Temperatura máxima: " + forecast.getData().listIterator().next().getTMax() + " ºC\n" +
                    "Temperatura mínima: " + forecast.getData().listIterator().next().getTMin() + " ºC\n" +
                    "Direção do vento: " + forecast.getData().listIterator().next().getPredWindDir() + "\n" +
                    "Intensidade do vento: Classe " + forecast.getData().listIterator().next().getClassWindSpeed() + "\n" + 
                    "Probabilidade de precipitação: " + forecast.getData().listIterator().next().getPrecipitaProb() + " % \n");
            } else {
                logger.info( "No results!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}