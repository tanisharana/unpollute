package com.example.trashdetection.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trashdetection.R;
import com.example.trashdetection.SplashActivity;
import com.example.trashdetection.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    TextView profile_tv;
    EditText username, bio_et;
    ImageView edit_img;
    Button save, logout;
    DatabaseReference reference;
    FirebaseUser fuser;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        username = view.findViewById(R.id.username);
        profile_tv = view.findViewById(R.id.profile_tv);
        bio_et = view.findViewById(R.id.bio_et);
        edit_img = view.findViewById(R.id.edit_image);
        save = view.findViewById(R.id.save_btn);
        logout = view.findViewById(R.id.logout);


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        edit_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setVisibility(View.VISIBLE);
                username.setEnabled(true);
                bio_et.setEnabled(true);
                username.setSelection(username.getText().length());
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username.setEnabled(false);
                bio_et.setEnabled(false);


                reference.child("bio").setValue(bio_et.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            //   Toast.makeText(getContext(),"Profile Updated...", Toast.LENGTH_SHORT);
                        }
                        else{
                            //   Toast.makeText(getContext(),"Unable to Save...", Toast.LENGTH_SHORT);

                        }
                    }
                });



                reference.child("username").setValue(username.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getContext(),"Profile Updated...", Toast.LENGTH_SHORT);
                        }
                        else{
                            Toast.makeText(getContext(),"Unable to Save...", Toast.LENGTH_SHORT);

                        }
                    }
                });

                save.setVisibility(View.GONE);
            }

        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(requireActivity(), SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isAdded()){
                    User user = dataSnapshot.getValue(User.class);
                    username.setText(user.getUsername());
                    bio_et.setText(user.getBio());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

}