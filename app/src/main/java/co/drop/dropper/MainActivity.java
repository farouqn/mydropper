package co.drop.dropper;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Routing;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.*;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        OnMapReadyCallback, RoutingListener, com.google.android.gms.location.LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    private DrawerLayout drawer_layout;
    private ImageView navbarToggle;
    private GoogleMap mMap;
    private FirebaseAuth firebaseAuth;
    private FrameLayout screen_area;
    private RelativeLayout navSearch;
    private Button requestPick;
    private String destination;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mapFragment;
    private String shipperId = "";
    private LatLng destinationLatLng;
    private RelativeLayout mShipperInfo;
    private TextView mShipperName;
    private TextView mShipperPhone, mShipperDestination;
    private int status = 0;
    private float rideDistance;
    private LatLng pickupLatLng;
    private TextView un;
    RelativeLayout relative;
    BottomSheetBehavior bottomSheetBehavior;
    private TextView RecipientNumber;
    private String receipt;
    private String address;
    private String cost;
    private TextView Address;
    private FusedLocationProviderClient mFusedLocationClient;
    private Switch mWorkingSwitch;
    private Button accept;
    private Button reject;
    private RelativeLayout req;
    private RelativeLayout bottomview;
    private TextView Cost;
    MediaPlayer mp;
    private TextView cancel;
    private RelativeLayout bottomcomplete;
    private Spinner payM;
    private Button deliveryComplete;
    private DatabaseReference paymentInfo;
    private String paymentM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);


        } else {

            if (mapFragment != null) {

                mapFragment.getMapAsync(MainActivity.this);

            }
        }

        mWorkingSwitch = findViewById(R.id.workingSwitch);

        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    connectCarrier(); //TODO: SET TOAST HERE

                    Toast.makeText(MainActivity.this, "You are currently available", Toast.LENGTH_SHORT).show();

                } else {

                    disconnectCarrier();
                }
            }
        });

        mp = MediaPlayer.create(this, R.raw.vibrate);

        Cost = findViewById(R.id.Cost);

        bottomview = findViewById(R.id.bottomview);

        req = findViewById(R.id.req);

        cancel = findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelTrip();
            }
        });

        //accept = findViewById(R.id.accept);

        //reject = findViewById(R.id.reject);

        /*reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cancelTrip();

            }
        });

        */

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        paymentInfo = FirebaseDatabase.getInstance().getReference().child("Customer Requests");

        Address = findViewById(R.id.Address);

        RecipientNumber = findViewById(R.id.RecipientNumber);

        RecipientNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialContactPhone();
            }

            private void dialContactPhone() {

                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", RecipientNumber.getText().toString(), null)));

            }
        });

        payM = findViewById(R.id.payM);

        bottomcomplete = findViewById(R.id.bottomcomplete);

        deliveryComplete = findViewById(R.id.deliveryComplete);

        deliveryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                savePaymentInfo();

                recordTrip();

                endTrip();

                bottomcomplete.setVisibility(View.GONE);

                bottomview.setVisibility(View.VISIBLE);

            }
        });

        relative = findViewById(R.id.bottomview);

        bottomSheetBehavior = BottomSheetBehavior.from(relative);

        requestPick = findViewById(R.id.requestPick);

        mShipperDestination = findViewById(R.id.ShipperDestination);

        mShipperName = findViewById(R.id.ShipperName);

        mShipperPhone = findViewById(R.id.ShipperPhone);

        mShipperPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialContactPhone();

            }

            private void dialContactPhone() {

                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mShipperPhone.getText().toString(), null)));

            }
        });

        mShipperInfo = findViewById(R.id.shipperInfo);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        polylines = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {

            mapFragment.getMapAsync(this);

        }


        screen_area = findViewById(R.id.screen_area);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();


        if (user == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        navbarToggle = findViewById(R.id.navbarToggle);
        navbarToggle.setOnClickListener(this);

        drawer_layout = findViewById(R.id.drawer_layout);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);
        TextView email = navHeaderView.findViewById(R.id.email);
        un = navHeaderView.findViewById(R.id.un);
        un.setText(user.getDisplayName());
        email.setText(user.getEmail());

        requestPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        erasePolyLines();

                        if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {
                            getRouteToPickup(destinationLatLng);
                        }

                        requestPick.setText("Delivery Completed");

                        bottomview.setVisibility(View.GONE);

                        bottomcomplete.setVisibility(View.VISIBLE);

                        /*

                    case 2:

                        recordTrip();

                        endTrip();

                        break;

                        */
            }
        });

        getAssignedShipper();

    }

    private void getAssignedShipper() {

        String carrierId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference assignedShipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(carrierId).child("Customer Requests").child("shippingId");

        assignedShipperRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    status = 1;

                    shipperId = dataSnapshot.getValue().toString();

                    getAssignedShipperPickupLocation();

                    getAssignedShipperDestination();

                    getAssignedShipperInfo();

                    getRecipient();

                } else {

                    endTrip();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    Marker pickupMarker;

    private DatabaseReference assignedShipperPickupLocationRef;

    private ValueEventListener assignedShipperPickupLocationRefListener;

    private void getAssignedShipperPickupLocation() {

        assignedShipperPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("Customer Requests").child(shipperId).child("l");

        assignedShipperPickupLocationRefListener = assignedShipperPickupLocationRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && !shipperId.equals("")) {

                    List<Object> map = (List<Object>) dataSnapshot.getValue();

                    double locationLat = 0;

                    double locationLng = 0;

                    if (map.get(0) != null) {

                        locationLat = Double.parseDouble(map.get(0).toString());

                    }

                    if (map.get(1) != null) {

                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    pickupLatLng = new LatLng(locationLat, locationLng);

                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location)));

                    getRouteToPickup(pickupLatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToPickup(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null) {
            Routing routing = new Routing.Builder()
                    .key(getString(R.string.google_maps_key))
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();

        }
    }

    private void getRecipient() {

        DatabaseReference rec = FirebaseDatabase.getInstance().getReference().child("Customer Requests");

        rec.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("Recipient") != null) {

                        receipt = map.get("Recipient").toString();

                        RecipientNumber.setText("" + receipt);

                    } else {
                        RecipientNumber.setText("--");
                    }

                    if (map.get("Address") != null) {

                        address = map.get("Address").toString();

                        Address.setText("" + address);

                    } else {
                        Address.setText("--");
                    }

                    if (map.get("Cost") != null) {

                        cost = map.get("Cost").toString();

                        Cost.setText("₦" + cost.substring(0, Math.min(cost.length(), 5)));

                    } else {
                        Cost.setText("--");
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });
    }

    private void getAssignedShipperDestination() {

        String carrierId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference assignedShipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(carrierId).child("Customer Requests");

        assignedShipperRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("destination") != null) {

                        destination = map.get("destination").toString();

                        mShipperDestination.setText("" + destination);

                    } else {
                        mShipperDestination.setText("Destination: --");
                    }

                    Double destinationLat = 0.0;

                    Double destinationLng = 0.0;

                    if (map.get("destinationLat") != null) {

                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }

                    if (map.get("destinationLng") != null) {

                        destinationLng = Double.valueOf(map.get("destinationLng").toString());

                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedShipperInfo() {

        mShipperInfo.setVisibility(View.VISIBLE);

        mp.start();

        DatabaseReference mShipperDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperId);

        mShipperDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map.get("Name") != null) {

                        mShipperName.setText(map.get("Name").toString());
                    }

                    if (map.get("Phone") != null) {

                        mShipperPhone.setText(map.get("Phone").toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void endTrip() {

        requestPick.setText("Picked Item");

        erasePolyLines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference carrierRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userId).child("Customer Requests");

        carrierRef.removeValue();

        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer Requests");

        GeoFire geoFire = new GeoFire(customerRef);

        geoFire.removeLocation(shipperId);

        shipperId = "";

        rideDistance = 0;

        if (pickupMarker != null) {

            pickupMarker.remove();
        }

        erasePolyLines();

        shipperId = "";

        if (pickupMarker != null) {
            pickupMarker.remove();
        }

        if (assignedShipperPickupLocationRefListener != null) {

            assignedShipperPickupLocationRef.removeEventListener(assignedShipperPickupLocationRefListener);

        }

        mShipperInfo.setVisibility(View.GONE);
        mShipperName.setText("");
        mShipperPhone.setText("");
        mShipperDestination.setText("Destination: --");

    }

    private void savePaymentInfo(){

        paymentM = payM.getSelectedItem().toString().trim();

        HashMap map = new HashMap();

        map.put("Payment Method", paymentM);

        paymentInfo.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void cancelTrip() {

        //reject.setVisibility(View.GONE);

        //accept.setVisibility(View.GONE);

        requestPick.setText("Picked Item");

        erasePolyLines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference carrierRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userId).child("Customer Requests");

        carrierRef.removeValue();

        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer Requests");

        GeoFire geoFire = new GeoFire(customerRef);

        geoFire.removeLocation(shipperId);

        shipperId = "";

        rideDistance = 0;

        if (pickupMarker != null) {

            pickupMarker.remove();
        }

        erasePolyLines();

        shipperId = "";

        if (pickupMarker != null) {
            pickupMarker.remove();
        }

        if (assignedShipperPickupLocationRefListener != null) {

            assignedShipperPickupLocationRef.removeEventListener(assignedShipperPickupLocationRefListener);

        }

        mShipperInfo.setVisibility(View.GONE);
        mShipperName.setText("");
        mShipperPhone.setText("");
        mShipperDestination.setText("Destination: --");

    }

    private void recordTrip() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference carrierRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userId).child("History");

        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(shipperId).child("History");

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History");

        String requestId = historyRef.push().getKey();

        carrierRef.child(requestId).setValue(true);

        shipperRef.child(requestId).setValue(true);

        HashMap map = new HashMap();

        map.put("carrier", userId);

        map.put("shipper", shipperId);

        map.put("rating", 0);

        map.put("timestamp", getCurrentTimeStamp());

        map.put("destination", destination);

        map.put("location/from/lat", pickupLatLng.latitude);

        map.put("location/from/lng", pickupLatLng.longitude);

        map.put("location/to/lat", destinationLatLng.latitude);

        map.put("location/to/lng", destinationLatLng.longitude);

        map.put("distance", rideDistance);

        map.put("cost" , cost);

        map.put("payment method", paymentM);

        historyRef.child(requestId).updateChildren(map);

    }

    private Long getCurrentTimeStamp() {

        Long timestamp = System.currentTimeMillis()/1000;

        return timestamp;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }else{

            checkLocationPermission();

        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!shipperId.equals("") && mLastLocation!=null && location != null){
                        rideDistance += mLastLocation.distanceTo(location)/1000;
                    }
                    mLastLocation = location;


                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("carriersAvailable");

                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("carriersWorking");

                    GeoFire geoFireAvailable = new GeoFire(refAvailable);

                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (shipperId){
                        case "":
                            geoFireWorking.removeLocation(userId);

                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);

                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                            break;
                    }
                }
            }
        }
    };


    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int id) {
        Fragment fragment = null;

        switch (id) {

            case R.id.navPayment:
                fragment = new PaymentFragment();
                break;

            case R.id.navHelp:
                fragment = new HelpFragment();

                break;

            case R.id.navHistory:
                fragment = new HistoryFragment();

                break;

            case R.id.navSettings:
                fragment = new SettingsFragment();

                break;

            case R.id.navLog:
                fragment = new LogoutFragment();
                break;


        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_main, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();

        displaySelectedScreen(id);

        return true;
    }

    @Override
    public void onClick(View view) {

        if (view == navbarToggle) {
            drawer_layout.openDrawer(Gravity.LEFT);
        }

    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT);
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolyLines(){

        for(Polyline line : polylines){

            line.remove();
        }

        polylines.clear();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext() != null) {

            if (!shipperId.equals("")) {

                rideDistance += mLastLocation.distanceTo(location) / 1000;        //TODO: FIX ERROR HERE
            }

            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("carriersAvailable");

            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("carriersWorking");

            GeoFire geoFireAvailable = new GeoFire(refAvailable);

            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (shipperId) {
                case "":

                    geoFireWorking.removeLocation(userId);

                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new
                            GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });

                    break;

                default:

                    geoFireAvailable.removeLocation(userId);

                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });

                    break;
            }
        }
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

            private void connectCarrier(){
                checkLocationPermission();
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }

            private void disconnectCarrier(){
                if(mFusedLocationClient != null){

                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);

                }
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("carriersAvailable");

                GeoFire geoFire = new GeoFire(ref);

                geoFire.removeLocation(userId);
            }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);


        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended ( int i){

    }

    final int LOCATION_REQUEST_CODE = 1;


    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);

                }else{

                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference data = FirebaseDatabase.getInstance().getReference("carriersAvailable");

        GeoFire geoFire = new GeoFire(data);

        geoFire.removeLocation(userId);
    }
}

