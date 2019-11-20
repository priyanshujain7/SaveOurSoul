package com.ritik.saveoursoul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedContactsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    EmergencyAdapter adapter;
    List<Emergency> emergencies = new ArrayList<>();
    private Dialog addContactDialog;
    EditText conName, conNumber;
    Button save;
    FloatingActionButton fab;
    DatabaseReference databaseReference;
    DatabaseReference databaseReference1;
    DatabaseReference databaseReference2;
    String userId = FirebaseAuth.getInstance().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_contacts);


        databaseReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(userId);


        addContactDialog = new Dialog(SavedContactsActivity.this);
        addContactDialog.setContentView(R.layout.dialog);
        addContactDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        conName = addContactDialog.findViewById(R.id.con_name);
        conNumber = addContactDialog.findViewById(R.id.con_number);
        save = addContactDialog.findViewById(R.id.button_save);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactDialog.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conName.getText().toString().isEmpty()) {
                    conName.setError("Required");
                } else if (conNumber.getText().toString().isEmpty()) {
                    conNumber.setError("Required");
                } else if (conNumber.getText().toString().length()!=10) {
                    conNumber.setError("Enter valid no.");
                } else {
                    saveData(conName.getText().toString().trim(), conNumber.getText().toString().trim());
                    conName.setText("");
                    conNumber.setText("");
                    addContactDialog.cancel();
                }
            }
        });

        showData();
        recyclerConfig();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                        final int deleteItem = position;
                        new AlertDialog.Builder(SavedContactsActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are you sure?")
                                .setMessage("Do you really want to delete this contact")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        databaseReference2 = FirebaseDatabase.getInstance().getReference("Contacts").child(userId).child(emergencies.get(deleteItem).getId());
                                        databaseReference2.removeValue();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                })
        );
    }


    public void saveData(String n, String p) {
        String itemId = databaseReference.push().getKey();
        Emergency e = new Emergency(itemId, p, n);
        databaseReference.child(itemId).setValue(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showData() {

        databaseReference1 = FirebaseDatabase.getInstance().getReference("Contacts").child(userId);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emergencies.clear();
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Emergency emergency = d.getValue(Emergency.class);
                    emergencies.add(emergency);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recyclerConfig() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        adapter = new EmergencyAdapter(emergencies);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finishAffinity();
        finish();
    }
}

