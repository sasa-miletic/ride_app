package com.example.rideapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.MyHolder> {

    Context context;
    List<ModelRide> rideList;

    public RideAdapter(Context context, List<ModelRide> rideList) {
        this.context = context;
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_rides, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String distance = rideList.get(position).getDistance();
        String topSpeed = rideList.get(position).getTopSpeed();
        String duration = rideList.get(position).getTime();

        holder.distanceTv.setText(distance);
        holder.topSpeedTv.setText(topSpeed);
        holder.durationTv.setText(duration);
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }


    public class MyHolder extends RecyclerView.ViewHolder {

        TextView distanceTv, topSpeedTv, durationTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            distanceTv = itemView.findViewById(R.id.distance);
            topSpeedTv = itemView.findViewById(R.id.topSpeed);
            durationTv = itemView.findViewById(R.id.duration);
        }
    }
}
