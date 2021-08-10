package com.mobiletv.app;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.MessageView;
import com.mobiletv.app.PlayerActivity;
import com.mobiletv.app.R;
import com.mobiletv.video.MediaController;
import com.mobiletv.video.VideoView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity implements VideoView.VideoViewCallback {

    private static final String TAG = "PlayerActivity";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

    private VideoView mVideoView;
	private MediaController mMediaController;

    private View mBottomLayout;
    private View mVideoLayout;

    private int mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;

	private ListView mListView;
	private EditText mEditText;
	private ImageButton mImageButton;
	private ToggleButton mToggleButton;

	private List<MessageView> messageView;

	private DatabaseReference mDatabaseReference;

	private String title, description, thumbnail, url, key;
	private FirebaseAuth mAuth;
	private String name, email, uid;
	private Uri photoUrl;
	private boolean emailVerified;
	private AudioManager mAudioManager;

	private AdView mAdViewDescription, mAdViewVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
		mAuth = FirebaseAuth.getInstance();
		if (mAuth.getCurrentUser() != null) {
			name = mAuth.getCurrentUser().getDisplayName();
			email = mAuth.getCurrentUser().getEmail();
			uid = mAuth.getCurrentUser().getUid();
			photoUrl = mAuth.getCurrentUser().getPhotoUrl();
			emailVerified = mAuth.getCurrentUser().isEmailVerified();
		}
		
		MobileAds.initialize(this, getString(R.string.id_app));
        mAdViewDescription = (AdView) findViewById(R.id.adViewDescription);
		mAdViewVideo = (AdView) findViewById(R.id.adViewVideo);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdViewDescription.loadAd(adRequest);
		mAdViewVideo.loadAd(adRequest);
		
		
		
		
        mVideoLayout = findViewById(R.id.video_layout);
        mBottomLayout = findViewById(R.id.bottom_layout);
		mToggleButton = (ToggleButton) findViewById(R.id.muted_button);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mMediaController = (MediaController) findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
		
		mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mAudioManager = (AudioManager) getSystemService(PlayerActivity.AUDIO_SERVICE);
					if(isChecked)
					{
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
					}
					else
					{
						mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
					}

				}
			});


		Intent intent = getIntent();
        title = intent.getExtras().getString("title");
		description = intent.getExtras().getString("description");
        thumbnail = intent.getExtras().getString("thumbnail");
        url = intent.getExtras().getString("url");
		key = intent.getExtras().getString("key");

		mMediaController.setTitle(title);
		mVideoView.start();


		mDatabaseReference = FirebaseDatabase.getInstance().getReference();
		messageView = new ArrayList<>();
		mListView = (ListView) findViewById(R.id.message_view);
		mEditText = (EditText) findViewById(R.id.message_in);
		mImageButton = (ImageButton) findViewById(R.id.message_send);

		mDatabaseReference.child("channels").child(key).addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					TextView mChannel = (TextView) findViewById(R.id.channel_name);
					mChannel.setText(title);
					messageView.clear();
					for (DataSnapshot postSnapshot : dataSnapshot.child("comments").getChildren()) {
						MessageView proList = postSnapshot.getValue(MessageView.class);
						messageView.add(proList);
					}

					final MessageAdapter mAdapter = new MessageAdapter(PlayerActivity.this, messageView);
					mListView.setAdapter(mAdapter);
					mListView.post(new Runnable() {
							@Override
							public void run() {
								mListView.setSelection(mAdapter.getCount() - 1);
							}
						});

				}

				@Override
				public void onCancelled(DatabaseError databaseError) {

				}
			});

		mImageButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String message = mEditText.getText().toString();
					Date today = Calendar.getInstance().getTime();
					SimpleDateFormat formatter = new SimpleDateFormat("hh:mm - dd/MM/yyyy");
					String time = formatter.format(today);
					if (!TextUtils.isEmpty(message)) {
						String push = mDatabaseReference.push().getKey();
						Map<String, Object> values = new HashMap<>();
						values.put("key", key);
						values.put("name", name);
						values.put("message", message);
						values.put("time", time);
						mDatabaseReference.child("channels").child(key).child("comments").child(push).setValue(values);
						mEditText.setText("");
					} else {
						Toast.makeText(PlayerActivity.this, getString(R.string.type_a_message), Toast.LENGTH_LONG).show();
					}
				}
			});

	}
	
	@Override
    public void onResume() {
        mVideoView.start();
        super.onResume();
		if (mAdViewDescription != null || mAdViewVideo != null) {
            mAdViewDescription.resume();
			mAdViewVideo.resume();
        }
    }

    @Override
    protected void onPause() {
		if (mAdViewDescription != null || mAdViewVideo != null) {
            mAdViewDescription.pause();
			mAdViewVideo.pause();
        }
        super.onPause();
        Log.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            mVideoView.pause();
        }
    }

	@Override
    public void onDestroy() {
		finish();
		if (mAdViewDescription != null || mAdViewVideo != null) {
            mAdViewDescription.destroy();
			mAdViewVideo.destroy();
        }
        super.onDestroy();
    }

    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
				@Override
				public void run() {
					int width = mVideoLayout.getWidth();
					cachedHeight = (int) (width * 405f / 720f);
					ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
					videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
					videoLayoutParams.height = cachedHeight;
					mVideoLayout.setLayoutParams(videoLayoutParams);
					mVideoView.setVideoPath(url);
					mVideoView.requestFocus();
				}
			});
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
        Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
    }


    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.GONE);

        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
		
		switchAdView(!isFullscreen);
    }
	
    private void switchAdView(boolean show) {
        if (getSupportActionBar() != null) {
            if (show) {
                // getSupportActionBar().show();
				mAdViewDescription.setVisibility(View.VISIBLE);
				mAdViewVideo.setVisibility(View.GONE);
            } else {
                // getSupportActionBar().hide();
				mAdViewDescription.setVisibility(View.GONE);
				mAdViewVideo.setVisibility(View.VISIBLE);
				
            }
        }
    }
	
	
    @Override
    public void onPause(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
		
	}

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingStart VideoView callback");
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingEnd VideoView callback");
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

}
