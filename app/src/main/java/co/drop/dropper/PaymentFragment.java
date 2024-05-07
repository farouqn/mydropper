package co.drop.dropper;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class PaymentFragment extends Fragment implements View.OnClickListener{
        public static PaymentFragment newInstance() {
            PaymentFragment fragment = new PaymentFragment();
            return fragment;
        }

        private ImageView backPayment;
        private RelativeLayout relPay;
        private TextView todayEarn;
        private DatabaseReference earningDatabase;


        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            backPayment = view.findViewById(R.id.backPayment) ;
            backPayment.setOnClickListener(this);


            relPay = view.findViewById(R.id.relPay);

            todayEarn = view.findViewById(R.id.todayEarn);

            earningDatabase = FirebaseDatabase.getInstance().getReference().child("History");

            earningDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    int sum = 0;

                    for (DataSnapshot ds : snapshot.getChildren()){

                        Map<String,Object> map = (Map<String, Object>) ds.getValue();

                        Object cost = map.get("cost");

                        double cvalue = Double.valueOf(String.valueOf(cost));

                        sum += cvalue;

                        todayEarn.setText("Today's earnings: ₦" + "" + String.valueOf(sum));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_payment, container,false);


        }

    @Override
    public void onClick(View view) {

            if(view == backPayment){
                relPay.setVisibility(View.GONE);
            }

    }
}