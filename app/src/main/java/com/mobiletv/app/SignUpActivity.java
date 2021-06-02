package com.mobiletv.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.VideoListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

public class SignUpActivity extends AppCompatActivity {

    private EditText Email, Password;
    private FirebaseAuth mAuth;
    private ProgressBar ProgressBar;
    private Button ButtonSignUp, ButtonSignIn;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        Email = findViewById(R.id.email_signup);
        Password = findViewById(R.id.password_signup);
        ProgressBar = findViewById(R.id.progress_signup);
        ButtonSignUp= findViewById(R.id.button_signup);
		ButtonSignIn = findViewById(R.id.button_new_signin);
		
		ButtonSignIn.setOnClickListener(new View.OnClickListener() {
			
				@Override
				public void onClick(View v) {
					startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
				}
			});
        ButtonSignUp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					String email = Email.getText().toString().trim();
					String password = Password.getText().toString().trim();

					if (TextUtils.isEmpty(email)) {
						Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
						return;
					}

					if (TextUtils.isEmpty(password)) {
						Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
						return;
					}

					if (password.length() < 6) {
						Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_LONG).show();
						return;
					}

					ProgressBar.setVisibility(View.VISIBLE);
					//create user
					mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                ProgressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.the_email_address_is_already_in_use_by_another_account),
												   Toast.LENGTH_LONG).show();
                                } else {
									startActivity(new Intent(SignUpActivity.this, MainActivity.class));
									finish();
                                }
                            }
                        });

				}
			});
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProgressBar.setVisibility(View.GONE);
    }
	
	public void showInterstitial(View view) {
        startActivity(new Intent(this, OtherActivity.class));

        StartAppAd.showAd(this);
    }

	public void showRewardedVideo() {
		final StartAppAd rewardedVideo = new StartAppAd(this);

		rewardedVideo.setVideoListener(new VideoListener() {
				@Override
				public void onVideoCompleted() {
					// Grant the reward to user
				}
			});

		rewardedVideo.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
				@Override
				public void onReceiveAd(Ad ad) {
					rewardedVideo.showAd();
				}

				@Override
				public void onFailedToReceiveAd(Ad ad) {
					// Can't show rewarded video
				}
			});
	}
}
