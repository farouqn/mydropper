package co.drop.dropper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener, View.OnClickListener {

    private String rideId, currentUserId, shipperId, carrierId, userCarrierOrShipper;

    private TextView tripLocation;

    private TextView tripDistance;

    private TextView tripDate;

    private TextView tripName;

    private TextView tripPhone;

    private TextView tripCost;

    private DatabaseReference historyRideInfoDb;

    private DatabaseReference feedDb;

    private LatLng destinationLatLng, pickupLatLng;

    private GoogleMap mMap;

    private SupportMapFragment mMapFragment;

    private String distance;

    private String deliveryCost;

    private ProgressDialog progressDialog;

    private Button save;

    private EditText feedback;

    private ImageView send;

    private Spinner paySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        feedback = findViewById(R.id.feedback);

        send = findViewById(R.id.send);  //TODO: FIX ERROR HERE

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String feed = feedback.getText().toString();

                progressDialog.setMessage("Saving Information");

                progressDialog.show();

                Map feedInfo = new HashMap();

                feedInfo.put("Feedback", feed);

                feedDb.updateChildren(feedInfo)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {

                                if(task.isSuccessful()){

                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"Thank you!", Toast.LENGTH_SHORT).show();
                                    finish();

                                }else{
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            }
                        });

            }
        });

        save = findViewById(R.id.save);

        save.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);

        polylines = new ArrayList<>();

        rideId = getIntent().getExtras().getString("rideId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        tripLocation = findViewById(R.id.tripLocation);

        tripDistance = findViewById(R.id.tripDistance);

        tripDate = findViewById(R.id.tripDate);

        tripName = findViewById(R.id.tripName);

        tripPhone = findViewById(R.id.tripPhone);

        tripCost = findViewById(R.id.tripCost);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);

        feedDb = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);

        getRideInformation();

    }

    private void Save(){

        DatabaseReference mPaymentStatus = FirebaseDatabase.getInstance().getReference().child("PaymentStat");

        Map payStatus = new HashMap();

        String payStat = paySpinner.getSelectedItem().toString();

        payStatus.put("Payment", payStat);

            mPaymentStatus.updateChildren(payStatus)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){

                        Toast.makeText(HistorySingleActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

                    }else{

                        Toast.makeText(HistorySingleActivity.this, "Please try again", Toast.LENGTH_SHORT).show();


                    }
                }
            });

        }

    /*
        private void getCurrentUserInfo(){

          DatabaseReference  currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Shippers").child(currentUserId);

          currentUserDB.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                  if (dataSnapshot.exists()) {

                      Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                      if (map.get("Name") != null) {

                       firstName = map.get("Name").toString();

                      }

                      if(map.get("Last Name") != null);

                        lastName = map.get("Last Name").toString();
                  }

              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError) {

              }
          });


        }
    */
    private void getRideInformation() {
        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    progressDialog.setMessage("Loading");
                    progressDialog.show();

                    for (DataSnapshot child:dataSnapshot.getChildren()){

                        if (child.getKey().equals("carrier")){       //

                            shipperId = child.getValue().toString();

                            if(!shipperId.equals(currentUserId)){

                                userCarrierOrShipper = "Shippers";    //

                                getUserInformation("Carriers", shipperId);   //
                            }
                        }
                        if (child.getKey().equals("shipper")){  //

                            carrierId = child.getValue().toString();

                            if(!carrierId.equals(currentUserId)){

                                userCarrierOrShipper = "Carriers";  //

                                getUserInformation("Shippers", carrierId);  //
                            }
                        }
                        if (child.getKey().equals("timestamp")){

                            tripDate.setText("Date: " + getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if (child.getKey().equals("destination")){

                            tripLocation.setText("Destination: "+child.getValue().toString());
                        }

                        if (child.getKey().equals("distance")){

                            distance = child.getValue().toString();

                            tripDistance.setText("Distance: " + distance.substring(0, Math.min(distance.length(),5)) + "km");
                        }

                        if(child.getKey().equals("cost")){

                            deliveryCost = child.getValue().toString();

                            tripCost.setText("Cost: ₦" + deliveryCost);
                        }

                        if (child.getKey().equals("location")){

                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));

                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));

                            if(destinationLatLng != new LatLng(0,0)){

                                getRouteToMarker();

                                progressDialog.dismiss();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                progressDialog.dismiss();

                Toast.makeText(HistorySingleActivity.this, "Please check your connection and try again", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {

        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);

        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("Name") != null){

                        tripName.setText(map.get("Name").toString());

                    }

                    if(map.get("Phone") != null){

                        tripPhone.setText(map.get("Phone").toString());

                    }

                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String getDate(Long time) {

        Calendar cal = Calendar.getInstance(Locale.getDefault());

        cal.setTimeInMillis(time*1000);

        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();

        return date;
    }

    private void getRouteToMarker() {

        Routing routing = new Routing.Builder()

                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng, destinationLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap=googleMap;
    }

    private List<Polyline> polylines;

    private static final int[] COLORS = new int[]{R.color.colorBlack};
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


        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(pickupLatLng);

        builder.include(destinationLatLng);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;

        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location)));

        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location)));

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
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onClick(View v) {

        if(v==save){

            Save();
        }
    }
}
