package es.esy.thunderworm.weather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResponse;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_LOCATION = 1;

    int CONDITION_UNKNOWN = 0;
    int CONDITION_CLEAR = 1;
    int CONDITION_CLOUDY = 2;
    int CONDITION_FOGGY = 3;
    int CONDITION_HAZY = 4;
    int CONDITION_ICY = 5;
    int CONDITION_RAINY = 6;
    int CONDITION_SNOWY = 7;
    int CONDITION_STORMY = 8;
    int CONDITION_WINDY = 9;

    final private int conditionDrawable[] = {R.drawable.unknown,
            R.drawable.sun,
            R.drawable.cloudy,
            R.drawable.foggy,
            R.drawable.haze,
            R.drawable.icy,
            R.drawable.rain,
            R.drawable.snowy,
            R.drawable.storm,
            R.drawable.windy
    };

    final private String conditionComment[] = {"Unable to get weather conditions " + getEmojiByUnicode(0x1F613),  //sad emoji unicode
            "Take your sunglasses with you, the weather is clear and sunny" + getEmojiByUnicode(0x1F60E),
            "Pleasant cloudy day" + getEmojiByUnicode(0x2601),
            "Drive safely, foggy weather today" + getEmojiByUnicode(0x1F698),
            "Haze all around!",
            "Beware! There's ice.",
            "Unbrella needed! There's Rain outside.",
            "Snowy weather",
            "Don't go outside, its stormy",
            "Pleasant Windy weather"

    };

    final private int themesArray[] = {R.style.AppTheme,
            R.style.Sunny,
            R.style.Cloudy,
            R.style.Cloudy,
            R.style.Cloudy,
            R.style.Icy,
            R.style.Rainy,
            R.style.Icy,
            R.style.Cloudy,
            R.style.AppTheme
    };

    private TextView mLocationText,mTemperature,mFeels,mComments,mCity;
    private ImageView mConditionImage;
    private SharedPreferences mPrefs;

    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs  = getSharedPreferences("MyThemePrefs",MODE_PRIVATE);
       setTheme(themesArray[mPrefs.getInt("theme",0)]);
        //setTheme(mPrefs.getBoolean("USE_DARK_THEME",false) ? R.style.AppThemeDark : R.style.AppThemeLight);

        setContentView(R.layout.activity_main);


        mLinearLayout = (LinearLayout) findViewById(R.id.mainLayout);



        getSupportActionBar().setElevation(0);

        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        client.connect();



        mLocationText = (TextView) findViewById(R.id.txtLocation);
        mConditionImage = (ImageView) findViewById(R.id.imgCondition);
        mTemperature = (TextView) findViewById(R.id.txtTemp);
        mFeels = (TextView) findViewById(R.id.txtFeels);
        mComments = (TextView) findViewById(R.id.txtComment);
        mCity = (TextView) findViewById(R.id.txtCityName);

        checkAndRequestWeatherPermissions();

    }

    private void getLocationSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getLocation()
                    .addOnSuccessListener(new OnSuccessListener<LocationResponse>() {
                        @Override
                        public void onSuccess(LocationResponse locationResponse) {
                            Location location = locationResponse.getLocation();
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if(null!=listAddresses&&listAddresses.size()>0){
                                    String cLocation = listAddresses.get(0).getAddressLine(0);
                                    String city = listAddresses.get(0).getSubLocality();
                                    mLocationText.setText(city);
                                    mCity.setText(listAddresses.get(0).getLocality());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } else {
            checkAndRequestWeatherPermissions();
        }
    }

    private void getWeatherSnapshot() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Awareness.getSnapshotClient(this).getWeather()
                    .addOnSuccessListener(new OnSuccessListener<WeatherResponse>() {
                        @Override
                        public void onSuccess(WeatherResponse weatherResponse) {
                            Weather weather = weatherResponse.getWeather();
                            weather.getConditions();
                           // mLogFragment.getLogView().println("Weather: " + weather);
                            Log.d("Weather",weather.toString());

                            mTemperature.setText(""+Math.round(weather.getTemperature(Weather.CELSIUS)) + (char) 0x00B0 + "C" );
                            mFeels.setText("Feels like "+Math.round(weather.getFeelsLikeTemperature(Weather.CELSIUS)) + (char) 0x00B0 + "C");

                           mConditionImage.setImageDrawable(getDrawable(conditionDrawable[weather.getConditions()[0]]));
                           mComments.setText(conditionComment[weather.getConditions()[0]]);


                           if(mPrefs.getInt("theme",0) != weather.getConditions()[0]) {

                               SharedPreferences.Editor prefEditor = mPrefs.edit();
                               prefEditor.putInt("theme", weather.getConditions()[0]);
                               prefEditor.apply();

                               startActivity(new Intent(MainActivity.this, MainActivity.class));
                               finish();
                           }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Unable to get permissions, skipping", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            checkAndRequestWeatherPermissions();
        }
    }

    private void checkAndRequestWeatherPermissions() {
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.i("Weather", "Permission previously denied and app shouldn't ask again.  Skipping" +
                        " weather snapshot.");
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_LOCATION
                );
            }
        } else {

            getWeatherSnapshot();
            getLocationSnapshot();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getWeatherSnapshot();
                    getLocationSnapshot();
                } else {
                    Toast.makeText(this, "Permission Denied, cannot continue.", Toast.LENGTH_SHORT).show();
                    Log.i("Weather", "Location permission denied.  Weather snapshot skipped.");
                }
            }
        }
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndRequestWeatherPermissions();
    }
}
