package com.example.android.sunshine.app;

import android.app.Dialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * Created by Milka Ferezliyska on 06-Jul-16.
 * A placeholder fragment containing a simple view.
*/
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> adapter;

    public ForecastFragment() {
    }

    // Handle menu actions
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.help) {
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("Sofia", "Bulgaria");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] daysForecast = {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Foggy - 70 / 46",
                "Wednesday - Cloudy - 72 / 63",
                "Thursday - Rainy - 64 / 51",
                "Friday - Foggy - 70 / 46",
                "Saturday - Sunny - 76 / 68",
                "Sunday - Sunny - 76 / 68",
                "Monday - Sunny - 76 / 68",
                "Tuesday - Sunny - 76 / 68",
                "Wednesday - Sunny - 76 / 68",
                "Thursday - Sunny - 76 / 68",
        };

        ArrayList<String> allDays = new ArrayList<>(Arrays.asList(daysForecast));

        adapter = new ArrayAdapter<String>(
                getActivity(), // Context is fragment activity
                R.layout.list_item_forecast, // ID of list item layout (xml filename)
                R.id.list_item_forecast_textview, // ID of text view in the above xml file
                allDays // forecast data to be fetched
        );

        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(adapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, String, Void> {
        private final String appID = "c15b32be9ce73ba89ac3f92bec8785e3";

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            int numDays = 7;
            String format = "json";
            String unit = "metric".toLowerCase();

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the url
                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/city";
                final String CITY_PARAM = "q";
                final String COUNTRY_PARAM = "country";
                final String UNITS_PARAM = "units";
                final String FORMAT_PARAM = "mode";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri dataUrl = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(CITY_PARAM, params[0].toLowerCase())
                        .appendQueryParameter(COUNTRY_PARAM, params[1].toLowerCase())
                        .appendQueryParameter(UNITS_PARAM, unit)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, appID)
                        .build();

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(dataUrl.toString());
                Log.v(LOG_TAG, "Built URI " + dataUrl.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Response to JSON" + forecastJsonStr);

            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

    }
}