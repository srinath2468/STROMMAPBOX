package com.example.testingmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import com.mapbox.services.android.navigation.v5.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.NavigationConstants;
import com.mapbox.services.android.navigation.v5.RouteProgress;
import com.mapbox.services.android.navigation.v5.models.RouteLegProgress;
import com.mapbox.services.android.navigation.v5.osrm.TextInstructions;
import com.mapbox.services.android.navigation.v5.models.RouteStepProgress;
import com.mapbox.services.android.navigation.v5.osrm.TokenizedInstructionHook;
import com.mapbox.services.android.navigation.v5.listeners.AlertLevelChangeListener;
import com.mapbox.services.android.navigation.v5.listeners.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.listeners.OffRouteListener;
import com.mapbox.services.android.navigation.v5.listeners.ProgressChangeListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;


import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;


import com.mapbox.services.commons.models.Position;



import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

import static com.example.testingmap.R.string.acces_token;

//import com.mapbox.services.android.testapp.R;
//import com.mapbox.services.android.testapp.Utils;
//import com.mapbox.services.commons.ServicesException;

public class MainActivity extends AppCompatActivity  implements PermissionsListener{



    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private Polyline mapMatchedRoute;


    private double maxTurnCompletionOffset;
    private double maneuverZoneRadius;

    private int mediumAlertInterval;
    private int highAlertInterval;

    private double minimumMediumAlertDistanceDriving;
    private double minimumMediumAlertDistanceCycling;
    private double minimumMediumAlertDistanceWalking;
    private double minimumHighAlertDistanceDriving;
    private double minimumHighAlertDistanceCycling;
    private double minimumHighAlertDistanceWalking;

    private double maximumDistanceOffRoute;
    private double deadReckoningTimeInterval;
    private double maxManipulatedCourseAngle;

    private double userLocationSnapDistance;
    private int secondsBeforeReroute;

    //@NavigationProfiles.Profile
    private String profile;

    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(acces_token));

        setContentView(R.layout.activity_main);


        //********************************* MAP ONLY FROM NOW ON************************************************
        //getting location and saving it
        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();


        //final Position man2 = new Position.fromCoordinates(-3.601845, 37.184080); // some y location//

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);



        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;

                floatingActionButton = (FloatingActionButton) findViewById(R.id.location_fab);
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (map != null) {
                            toggleGps(!map.isMyLocationEnabled());
                        }
                    }
                });
                final Position man2 = Position.fromCoordinates(-118.24233, 34.05332);
                final Position man4 = Position.fromCoordinates(-118.49666, 34.01114);


                //
                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Icon icon = iconFactory.fromResource(R.drawable.mapbox_compass_icon);
                Icon i2 = iconFactory.fromResource(R.drawable.mapbox_logo_icon);
                Icon i3 = iconFactory.fromResource(R.drawable.mapbox_mylocation_icon_default);


                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(man2.getLatitude(), man2.getLongitude()))
                        .title("Start")
                        .snippet("Start Position")
                        .icon(i2));
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(man4.getLatitude(), man4.getLongitude()))
                        .title("End")
                        .snippet("Destination")
                        .icon(i3));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(man2.getLatitude(), man2.getLongitude()))
                        .zoom(10)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);

                try {
                    //getRoute(man, man3);

                } catch (ServicesException servicesException) {
                    servicesException.printStackTrace();
                }


            }

        });
    }

// Should add events when the navigation event has started i.e when the user requests navigation
    NavigationEventListener Navigation = new NavigationEventListener() {
        @Override
        public void onRunning(boolean running) {

        }
    };





    public void enableLocation(boolean enabled) {
        if (enabled) {

            // If we have the last location of the user, we can move the camera to that position.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            final Location lastLocation = locationEngine.getLastLocation();
            if (lastLocation != null) {

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 10));

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final MapboxMap mapboxMap) {
                        map = mapboxMap;

                        //diff program


                       // final Position man = Position.fromCoordinates(-3.588098, 37.176164);


                        double tom = lastLocation.getLatitude();
                        double jerry = lastLocation.getLongitude();
                        final Position man = Position.fromCoordinates(jerry, tom);
                        final Position man2 = Position.fromCoordinates(-118.24233, 34.05332);
                        final Position man4 = Position.fromCoordinates(-118.49666, 34.01114);

                        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
                        autocomplete.setAccessToken(Mapbox.getAccessToken());
                        autocomplete.setType(GeocodingCriteria.TYPE_POI);
                        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
                                                              @Override
                                                              public void onFeatureClick(CarmenFeature feature) {
                                                                  hideOnScreenKeyboard();
                                                                  Position man3 = feature.asPosition();
                                                                  updateMap(man3.getLatitude(), man3.getLongitude());


                                                                  //
                                                                  IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                                                                  Icon icon = iconFactory.fromResource(R.drawable.mapbox_compass_icon);
                                                                  Icon i2 = iconFactory.fromResource(R.drawable.mapbox_logo_icon);
                                                                  Icon i3 = iconFactory.fromResource(R.drawable.mapbox_mylocation_icon_default);


                                                                  mapboxMap.addMarker(new MarkerOptions()
                                                                          .position(new LatLng(man.getLatitude(), man.getLongitude()))
                                                                          .title("Start")
                                                                          .snippet("Starts Position")
                                                                          .icon(i2));
                                                                  mapboxMap.addMarker(new MarkerOptions()
                                                                          .position(new LatLng(man3.getLatitude(), man3.getLongitude()))
                                                                          .title("End")
                                                                          .snippet("Destination")
                                                                          .icon(i3));
                                                                  CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                          .target(new LatLng(man3.getLatitude(), man3.getLongitude()))
                                                                          .zoom(15)
                                                                          .build();
                                                                  map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);

                                                                  try {
                                                                      getRoute(man, man3);

                                                                  } catch (ServicesException servicesException) {
                                                                      servicesException.printStackTrace();
                                                                  }


                                                              }

                                                          }



                        );
                    }
                });

            }


            locationEngineListener = new LocationEngineListener() {
                @Override
                public void onConnected() {
                    // No action needed here.
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 1));
                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }



    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            PermissionsManager permissionsManager = new PermissionsManager((PermissionsListener) this);
            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager.requestLocationPermissions(this);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }



    private void getRoute(Position man2, Position man4) {


        MapboxDirections client = new MapboxDirections.Builder()
                .setSteps(true)
                .setOrigin(man2)
                .setOverview(DirectionsCriteria.OVERVIEW_FULL)
                .setDestination(man4)
                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                .setAccessToken(Mapbox.getAccessToken())
                .build();


        client.enqueueCall(new retrofit2.Callback<DirectionsResponse>() {
                               @Override
                               public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                                   Log.d(TAG, "Response code" + response.code());
                                   if (response.body() == null) {
                                       Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                                       return;
                                   } else if (response.body().getRoutes().size() < 1) {
                                       Log.e(TAG, "No routes found");
                                       return;
                                   }

                                   currentRoute = response.body().getRoutes().get(0);
                                   Log.d(TAG, "Distance: " + currentRoute.getDistance());
                                   Toast.makeText(
                                           MainActivity.this,
                                           "Route is " + currentRoute.getDistance() + " meters long.",
                                           Toast.LENGTH_SHORT).show();



                                  drawRoute(currentRoute);





                               }


                               @Override
                               public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                                   Log.e(TAG, "Error: " + throwable.getMessage());
                                   Toast.makeText(MainActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();


                               }

                           }

        );


    }



    private void drawRoute(DirectionsRoute route) {

        //Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude() ,
                    coordinates.get(i).getLongitude());

        }

map.addPolyline(new PolylineOptions()
.add(points)
.color(Color.BLACK)
.width(4));


            // Draw Points on MapView





    }



    private void updateMap(double latitude, double longitude) {
        // Build marker
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Geocoder result"));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(1)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
        // Animate camera to geocoder result location

    }


    private void hideOnScreenKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private double computeDistance(Waypoint from, Waypoint to) {
        double dLat = Math.toRadians((to.getLatitude()/10) - (from.getLatitude()/10));
        double dLon = Math.toRadians((to.getLongitude()/10) - (from.getLongitude()/10));
        double lat1 = Math.toRadians(from.getLatitude()/10);
        double lat2 = Math.toRadians(to.getLatitude()/10);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double R = 3960;

        double distance = R * c;
        return distance;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }
}



