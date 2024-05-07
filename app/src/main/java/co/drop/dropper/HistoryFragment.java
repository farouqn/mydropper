package co.drop.dropper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import co.drop.dropper.historyRecycler.HistoryAdapter;
import co.drop.dropper.historyRecycler.HistoryObject;

public class HistoryFragment extends Fragment implements View.OnClickListener{
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    private ImageView backHistory;
    private RelativeLayout relHist;
    private RecyclerView mHistoryRecycler;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;
    private String userId;
    private ProgressDialog progressDialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mHistoryRecycler = view.findViewById(R.id.recyclerHistory);

        mHistoryRecycler.setNestedScrollingEnabled(false);

        mHistoryRecycler.setHasFixedSize(true);

        mHistoryLayoutManager = new LinearLayoutManager(HistoryFragment.this.getActivity());

        mHistoryRecycler.setLayoutManager(mHistoryLayoutManager);

        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), HistoryFragment.this.getActivity());

        mHistoryRecycler.setAdapter(mHistoryAdapter);

        getUserHistoryId();

        backHistory = view.findViewById(R.id.backHistory);

        backHistory.setOnClickListener(this);

        relHist = view.findViewById(R.id.relHist);
    }

    private void getUserHistoryId() {

        progressDialog.setMessage("Loading history");
        progressDialog.show();

        DatabaseReference carrierHistoryDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Carriers").child(userId).child("History");

        carrierHistoryDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());

                        progressDialog.dismiss();
                    }

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Oops! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void FetchRideInformation(String key) {

        DatabaseReference historyDb = FirebaseDatabase.getInstance().getReference().child("History").child(key);

        historyDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    String rideId = dataSnapshot.getKey();

                    Long timestamp = 0L;

                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(child.getKey().equals("timestamp")){

                            timestamp = Long.valueOf(child.getValue().toString());
                        }
                    }

                    HistoryObject object = new HistoryObject(rideId, getDate(timestamp));

                    resultsHistory.add(object);

                    mHistoryAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        calendar.setTimeInMillis(timestamp * 1000);

        String date = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString();

        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();

    private ArrayList<HistoryObject> getDataSetHistory() {

        return resultsHistory;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container,false);

    }

    @Override
    public void onClick(View v) {

        if(v == backHistory){
            relHist.setVisibility(View.GONE);
        }
    }
}