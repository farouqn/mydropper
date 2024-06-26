package co.drop.dropper.historyRecycler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.drop.dropper.R;
import co.drop.dropper.R;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders>{

    private List<HistoryObject> itemList;

    private Context context;

    public HistoryAdapter(List<HistoryObject> itemList, Context context){

        this.itemList = itemList;

        this.context = context;
    }
    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_items, null, false);

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutView.setLayoutParams(lp);

        HistoryViewHolders rcv = new HistoryViewHolders(layoutView);

        return rcv;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolders holder, int position) {

        holder.rideId.setText(itemList.get(position).getRideId());

        holder.time.setText(itemList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}