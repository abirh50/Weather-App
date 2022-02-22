package com.example.liveweather;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WeatherDataService {

    public static final String QUERY_FOR_CITY_ID = "https://www.metaweather.com/api/location/search/?query=";
    public static final String QUERY_FOR_CITY_WEATHER_BY_ID = "https://www.metaweather.com/api/location/";

    Context context;
    String cityID;

    public WeatherDataService(Context context){
        this.context = context;
    }

    public interface VolleyResponseListener {
        void onError(String message);

        void onResponse(String cityID);
    }

    public void getCityID(String cityName, VolleyResponseListener volleyResponseListener) {
        String url = QUERY_FOR_CITY_ID + cityName;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        cityID = "";
                        try {
                            JSONObject cityInfo = response.getJSONObject(0);
                            cityID = cityInfo.getString("woeid");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        volleyResponseListener.onResponse(cityID);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError("Error Occured");
            }
        });

        // Add the request to the RequestQueue.
        requestSingleton.getInstance(context).addToRequestQueue(request);
    }

    public interface WeatherByIDResponse {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityWeatherByID(String cityID, WeatherByIDResponse weatherByIDResponse) {
        List<WeatherReportModel> weatherReportModels = new ArrayList<>();

        String url = QUERY_FOR_CITY_WEATHER_BY_ID + cityID;

        // get JSON object
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray consolidated_weather_list = response.getJSONArray("consolidated_weather");

                            for (int i = 0; i < consolidated_weather_list.length(); i++) {
                                WeatherReportModel one_day_weather = new WeatherReportModel();

                                JSONObject day_data = consolidated_weather_list.getJSONObject(i);
                                one_day_weather.setId(day_data.getInt("id"));
                                one_day_weather.setWeather_state_name(day_data.getString("weather_state_name"));
                                one_day_weather.setWeather_state_abbr(day_data.getString("weather_state_abbr"));
                                one_day_weather.setWind_direction_compass(day_data.getString("wind_direction_compass"));
                                one_day_weather.setCreated(day_data.getString("created"));
                                one_day_weather.setApplicable_date(day_data.getString("applicable_date"));
                                one_day_weather.setMin_temp(day_data.getLong("min_temp"));
                                one_day_weather.setMax_temp(day_data.getLong("max_temp"));
                                one_day_weather.setThe_temp(day_data.getLong("the_temp"));
                                one_day_weather.setWind_speed(day_data.getLong("wind_speed"));
                                one_day_weather.setWind_direction(day_data.getLong("wind_direction"));
                                one_day_weather.setAir_pressure(day_data.getLong("air_pressure"));
                                one_day_weather.setHumidity(day_data.getInt("humidity"));
                                one_day_weather.setVisibility(day_data.getLong("visibility"));
                                one_day_weather.setPredictability(day_data.getInt("predictability"));

                                weatherReportModels.add(one_day_weather);
                            }

                            weatherByIDResponse.onResponse(weatherReportModels);

                        } catch (JSONException e) {
                            weatherByIDResponse.onError(e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add the request to the RequestQueue.
        requestSingleton.getInstance(context).addToRequestQueue(request);
    }

    // callback to return to MainActivity.java
    public interface WeatherByNameResponseCallback {
        void onError(String message);

        void onResponse(List<WeatherReportModel> weatherReportModels);
    }

    public void getCityWeatherByName(String cityName, WeatherByNameResponseCallback weatherByNameResponseCallback){
        // fetch city ID from api using city name
        getCityID(cityName, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String cityID) {
                // now we have the city ID
                getCityWeatherByID(cityID, new WeatherByIDResponse() {
                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(List<WeatherReportModel> weatherReportModels) {
                        weatherByNameResponseCallback.onResponse(weatherReportModels);
                    }
                });
            }
        });
    }
}
