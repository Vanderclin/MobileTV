package com.mobiletv.app;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.annotation.*;
import android.support.v7.app.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.mobiletv.app.*;
import com.mobiletv.app.R;

public class SignInActivity extends AppCompatActivity {

    private EditText Email, Password;
    private FirebaseAuth mAuth;
    private ProgressBar ProgressBar;
    private Button ButtonSignIn, ButtonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		authInstance();
        setContentView(R.layout.activity_signin);
		Email = findViewById(R.id.email_signin);
        Password = findViewById(R.id.password_signin);
        ProgressBar = findViewById(R.id.progress_signin);
        ButtonSignIn = findViewById(R.id.button_signin);
		ButtonSignUp = findViewById(R.id.button_new_account);
		
        mAuth = FirebaseAuth.getInstance();
        ButtonSignUp.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
				}
			});
        ButtonSignIn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String email = Email.getText().toString();
					final String password = Password.getText().toString();

					if (TextUtils.isEmpty(email)) {
						Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
						return;
					}

					if (TextUtils.isEmpty(password)) {
						Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
						return;
					}

					ProgressBar.setVisibility(View.VISIBLE);

					//authenticate user
					mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                ProgressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    if (password.length() < 8) {
                                        Password.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(SignInActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
				}
			});
    }
	private void authInstance() {
		mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finishAffinity();
        }
	}

	
}
