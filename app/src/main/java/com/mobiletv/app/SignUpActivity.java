package com.mobiletv.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.mobiletv.app.MainActivity;
import com.mobiletv.app.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText Email, Password;
    private FirebaseAuth mAuth;
    private ProgressBar ProgressBar;
    private Button ButtonSignUp, ButtonSignIn;
	private boolean mValue;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();
		openFirstDialog();
        Email = (EditText) findViewById(R.id.email_signup);
        Password = (EditText) findViewById(R.id.password_signup);
        ProgressBar = (ProgressBar) findViewById(R.id.progress_signup);
        ButtonSignUp= (Button) findViewById(R.id.button_signup);
		ButtonSignIn = (Button) findViewById(R.id.button_new_signin);
		
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
	
	private void openFirstDialog() {
        mValue = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("mValue", true);

        if (mValue) {
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(this);

			mAlertDialog.setIcon(R.mipmap.ic_launcher);
            mAlertDialog.setTitle(getString(R.string.dialog_title));
            mAlertDialog.setMessage(getString(R.string.dialog_message));
			mAlertDialog.setCancelable(false);
			
			
			
			mAlertDialog.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
            mAlertDialog.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						openDialogPermission();
					}
				});
            mAlertDialog.show();
            // getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("mValue", false).commit();
        }
    }
	
	private void openDialogPermission() {
		if (ContextCompat.checkSelfPermission(SignUpActivity.this,
											  Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
			if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){ 
			ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
			}else{
				ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
			}
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults){
		switch (requestCode){
			case 1: {
					if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
						if (ContextCompat.checkSelfPermission(SignUpActivity.this,
															  Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
							Toast.makeText(this, "Obrigado! Com isso iremos trabalhar na busca de canais em sua região", Toast.LENGTH_SHORT).show();
							getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("mValue", false).commit();
						}
					}else{
						Toast.makeText(this, "Infelizmente não podemos fazer nada, mas você pode tentar de novo assim que voltar.", Toast.LENGTH_SHORT).show();
					}
					return;
				}
		}
	}
	
}
