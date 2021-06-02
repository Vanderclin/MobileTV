package com.mobiletv.app;

import android.annotation.NonNull;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.Channels;
import com.mobiletv.app.MainActivity;
import com.mobiletv.app.R;
import com.mobiletv.update.UpdateChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
	private FirebaseAuth mAuth;
	private String name, email, uid;
	private Uri photoUrl;
	private boolean emailVerified;

	private DatabaseReference mDatabaseReference;
	private List <Channels> mChannelsList;
	private RecyclerView mRecyclerView;
	private int orientationDevice;
	private Toolbar mToolbar;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		checkConnection();
		mToolbar = findViewById(R.id.appbar);
		setSupportActionBar(mToolbar);
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			orientationDevice = 5;
		} else {
			orientationDevice = 3;
		}
		
		mAuth = FirebaseAuth.getInstance();
		if (mAuth.getCurrentUser() != null) {
			name = mAuth.getCurrentUser().getDisplayName();
			email = mAuth.getCurrentUser().getEmail();
			uid = mAuth.getCurrentUser().getUid();
			photoUrl = mAuth.getCurrentUser().getPhotoUrl();
			emailVerified = mAuth.getCurrentUser().isEmailVerified();

			if (name == null || name == "") {
				addName();
			}
			mDatabaseReference = FirebaseDatabase.getInstance().getReference();
			mChannelsList = new ArrayList<>();
			mDatabaseReference.child("channels").orderByChild("title").addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						mChannelsList.clear();
						for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
							Channels mChannels = postSnapshot.getValue(Channels.class);
							mChannelsList.add(mChannels);
						}
						mRecyclerView = findViewById(R.id.recyclerview_id);
						ChannelsAdapter mAdapter = new ChannelsAdapter(MainActivity.this, mChannelsList);
						mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, orientationDevice));
						mRecyclerView.setAdapter(mAdapter);


					}

					@Override
					public void onCancelled(DatabaseError databaseError) {

					}
				});
		} else {
			startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finishAffinity();
		}
		
    }
	
	private void checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			UpdateChecker.checkForDialog(MainActivity.this);
			UpdateChecker.checkForNotification(MainActivity.this);
        }
    }

	private void addName() {
		AlertDialog.Builder mAlertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_username, null);
		mAlertDialogBuilder.setCancelable(false);
        mAlertDialogBuilder.setView(dialogView);

        final EditText name = dialogView.findViewById(R.id.id_dialog_username);
        final Button save = dialogView.findViewById(R.id.id_dialog_username_save);
        final AlertDialog mDialogInterface = mAlertDialogBuilder.create();
        mDialogInterface.show();


        save.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String username = name.getText().toString();
					if (TextUtils.isEmpty(username)) {
						Toast.makeText(getApplicationContext(), "Obrigatorio...", Toast.LENGTH_SHORT).show();
						return;
					} else {
						Map<String, Object> values = new HashMap<>();
						values.put("current_name", name.getText().toString());
						values.put("current_email", email);
						values.put("current_uid", uid);
						mDatabaseReference.child("users").child(uid).setValue(values);
						updateProfile(username);
						mDialogInterface.dismiss();
					}

				}
			});
	}

	private void updateProfile(String username) {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
			.setDisplayName(username)
			//.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
			.build();

		user.updateProfile(profileUpdates)
			.addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						// Log.d(TAG, "User profile updated.");
					}
				}
			});
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_signout:
                FirebaseAuth.getInstance().signOut();
                finishAffinity();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                return true;
			case R.id.menu_profile:
				alertDialogProfile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private void alertDialogProfile() {
		AlertDialog.Builder mAlertDialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialog);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_profile, null);
		mAlertDialogBuilder.setCancelable(true);
        mAlertDialogBuilder.setView(dialogView);

        final TextView displayName = dialogView.findViewById(R.id.id_dialog_profile_name);
        final TextView displayEmail = dialogView.findViewById(R.id.id_dialog_profile_email);
		final ImageView displayVerification = dialogView.findViewById(R.id.id_dialog_profile_verification);
		displayName.setText(name);
		displayEmail.setText(email);
		if (emailVerified == true) {
			displayVerification.setImageResource(R.mipmap.ic_verified);
		} else {
			displayVerification.setImageResource(R.mipmap.ic_unverified);
		}


        final AlertDialog mDialogInterface = mAlertDialogBuilder.create();
        mDialogInterface.show();

		/*
		 save.setOnClickListener(new View.OnClickListener() {
		 @Override
		 public void onClick(View view) {
		 String username = name.getText().toString();
		 if (TextUtils.isEmpty(username)) {
		 Toast.makeText(getApplicationContext(), "Obrigatorio...", Toast.LENGTH_SHORT).show();
		 return;
		 } else {
		 Map<String, Object> values = new HashMap<>();
		 values.put("current_name", name.getText().toString());
		 values.put("current_email", email);
		 values.put("current_uid", uid);
		 mDatabaseReference.child("users").child(uid).setValue(values);
		 updateProfile(username);
		 mDialogInterface.dismiss();
		 }

		 }
		 });
		 */
	}

}

