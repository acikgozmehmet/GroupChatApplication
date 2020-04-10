package server;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class WeatherForecast {

    private static String[][] temperature =null;
    private static Response response = null;
    private static final String TOKEN = "PLEASE_PUT_YOUR_TOKEN_HERE";

    private static String GetCityKeyFromCitySearch(String city) {
        RestAssured.baseURI = "http://dataservice.accuweather.com";
        String basePath = "/locations/v1/cities/search";

        Response response = RestAssured.given().when().basePath(basePath)
                .queryParam("q", city)
                .queryParam("apikey", TOKEN)
                .then().when().get();

        String key = response.jsonPath().get("[0].Key");
        return key;
    }


    private static void GETWeatherForecast(String city) throws Exception {
        String locationKey = GetCityKeyFromCitySearch(city);
        String basePath = "/forecasts/v1/daily/5day/{locationKey}";

        try {
            response = RestAssured.given().queryParam("apikey", TOKEN).
                    when().basePath(basePath).pathParam("locationKey", locationKey)
                    .then().when().get();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return;
        }

        if (response.statusCode() != 200) {
            System.out.println("Status code in API call is" + response.statusCode());
            System.out.println("The AccuWeather API free service is not available now.");
            temperature = null;
            return;
        }

        JsonPath jsonPath = new JsonPath(response.asString());
        response = null;
        List<String> DailyForecastsList = jsonPath.get("DailyForecasts");

        int numRows = DailyForecastsList.size();
        temperature = new String[numRows][3];

        for (int i = 0; i < numRows; i++) {
            String date = jsonPath.get("DailyForecasts[" + i + "].Date").toString().substring(0, 10);
            String minValue = jsonPath.get("DailyForecasts[" + i + "].Temperature.Minimum.Value").toString();
            String maxValue = jsonPath.get("DailyForecasts[" + i + "].Temperature.Maximum.Value").toString();

            temperature[i][0] = date;
            temperature[i][1] = minValue;
            temperature[i][2] = maxValue;
        }

//        System.out.println("Arrays.deepToString(temperature) = " + Arrays.deepToString(temperature));
    }

    public static String reportForecast(String city) throws Exception {
        GETWeatherForecast(city);
        String table = null;

        if (temperature != null) {
            table = "\n" + city + "\nDate\t\tMin. Temp.(F)\t\tMax. Temp.(F)\n";

            for (int i = 0; i < temperature.length; i++) {
                for (int j = 0; j < temperature[i].length; j++) {
                    table += temperature[i][j] + "\t\t";
                }
                table =table.trim();
                table = "\n"+table+"\n";
            }

            temperature = null;
        }
        else
        {
            table = "Not available now. Sorry for the inconvenience. ";
        }

        return table;
    }

    public static void main(String[] args) throws Exception {
        String s = reportForecast("Virginia Beach");
        System.out.println(s);

    }

    public static String readFileAsString(String fileName) throws Exception {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

}
