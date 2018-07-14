# Weather
A simple weather application(android) to demonstrate the use of Google Awareness Api for Weather and Location.

# Getting Started
To get started, add the Google Awareness Api dependency to the app module of Gradle.
```
implementation 'com.google.android.gms:play-services-awareness:15.0.1'
```
This will get you all you need to start using Awareness Api.

We will specifically be using Snapshot Api for this application.

## Getting Location
Getting the location is quiet easy, just call getLocation() from the SnapshotClient.
```
Awareness.getSnapshotClient(this).getLocation()
```
This needs to have ACCESS_FINE_LOCATION permission, so don't forget to include it in your Manifest.

To get location you need to have the right permission, so checking the permission before using it is required.

Adding a Success Listener will give the LocationResponse which can be used to get the location.
```
Awareness.getSnapshotClient(this).getLocation()
                    .addOnSuccessListener(new OnSuccessListener<LocationResponse>() {
                        @Override
                        public void onSuccess(LocationResponse locationResponse) {
                            Location location = locationResponse.getLocation();
                            //use as required
                        }
                    });
```

## Getting Weather
It's the same as getting the location. It also needs the ACCESS_FINE_LOCATION permission so adding a permission check before calling it is required.

To get the weather call the getWeather() from the SnapshotClient.
```
Awareness.getSnapshotClient(this).getWeather()
```

Adding Success Listener will do all the other job.

```
Awareness.getSnapshotClient(this).getWeather()
                    .addOnSuccessListener(new OnSuccessListener<WeatherResponse>() {
                        @Override
                        public void onSuccess(WeatherResponse weatherResponse) {
                            Weather weather = weatherResponse.getWeather();
                            //use as required
                        }
                    });
```

# Notes
* You need to generate API KEY from Google Developer Console
* If you are cloning the repository, don't forget to add your API key in the manifest.