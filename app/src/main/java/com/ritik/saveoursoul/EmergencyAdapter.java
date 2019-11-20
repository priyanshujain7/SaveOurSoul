package com.ritik.saveoursoul;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder> {

    public List<Emergency> emergencies;

    public EmergencyAdapter(List<Emergency> emergencies) {
        this.emergencies = emergencies;
    }

    public static class EmergencyViewHolder extends RecyclerView.ViewHolder{

        public TextView Name;
        public TextView Number;

        public EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            Name = itemView.findViewById(R.id.name);
            Number = itemView.findViewById(R.id.number);
        }
        //
    }


    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {


        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.emergency , viewGroup , false);
        EmergencyViewHolder emergencyViewHolder = new EmergencyViewHolder(view);
        return emergencyViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder emergencyViewHolder, int i) {


        emergencyViewHolder.Name.setText(emergencies.get(i).getName());
        emergencyViewHolder.Number.setText(emergencies.get(i).getNumber());
    }

    @Override
    public int getItemCount() {
        return emergencies.size();
    }

}