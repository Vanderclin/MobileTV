package com.mobiletv.app;

import android.Manifest;
import android.annotation.NonNull;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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
import com.mobiletv.app.SignUpActivity;
import com.mobiletv.update.UpdateChecker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocationListener {
	private FirebaseAuth mAuth;
	private String name, email, uid;
	private Uri photoUrl;
	private boolean emailVerified;

	private DatabaseReference mDatabaseReference;
	private List <Channels> mChannelsList;
	private RecyclerView mRecyclerView;
	private int orientationDevice;

	private AdView mAdView;
	
	// Get Location
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected String current_location;
	private boolean mValue;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		checkConnection();
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			orientationDevice = 5;
		} else {
			orientationDevice = 3;
		}
		
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
		mDatabaseReference = FirebaseDatabase.getInstance().getReference();
		
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
			
			mChannelsList = new ArrayList<>();
			mDatabaseReference.child("channels").orderByChild("title").addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						mChannelsList.clear();
						for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
							Channels mChannels = postSnapshot.getValue(Channels.class);
							mChannelsList.add(mChannels);
						}
						mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_id);
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
		
		
		MobileAds.initialize(this, getString(R.string.id_app));
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
		
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
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
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
		current_location = String.valueOf(location.getLatitude() +","+ location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Log.d("Latitude", "disable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Log.d("Latitude", "enable");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Log.d("Latitude", "status");
	}

}

