package co.drop.dropper.historyRecycler;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import co.drop.dropper.HistorySingleActivity;
import co.drop.dropper.MainActivity;
import co.drop.dropper.R;
import co.drop.dropper.HistorySingleActivity;
import co.drop.dropper.R;

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideId;
    public TextView time;

    public HistoryViewHolders(View itemView){

        super(itemView);

        itemView.setOnClickListener(this);

        rideId = itemView.findViewById(R.id.rideId);

        time = itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);

        Bundle bundle = new Bundle();

        bundle.putString("rideId", rideId.getText().toString());

        intent.putExtras(bundle);

        v.getContext().startActivity(intent);

    }
}
