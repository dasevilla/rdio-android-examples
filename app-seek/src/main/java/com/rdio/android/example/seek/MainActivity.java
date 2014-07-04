package com.rdio.android.example.seek;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.rdio.android.api.OAuth1WebViewActivity;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioListener;

import java.io.IOException;

public class MainActivity extends Activity implements RdioListener {

    private static final String TAG = "RdioSeekExample";

    private static final String appKey = "YOUR_CONSUMER_KEY";
    private static final String appSecret = "YOUR_CONSUMER_SECRET";

    private static String accessToken = null;
    private static String accessTokenSecret = null;

    private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
    private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

    private static Rdio rdio;

    private MediaPlayer player;

    private SeekBar seekBar;

    private Handler seekHandler = new Handler();
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = (SeekBar) getWindow().getDecorView().findViewById(R.id.seekBar);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    debugTrack("SeekBar");
                    int mCurrentPosition = player.getCurrentPosition();
                    seekBar.setProgress(mCurrentPosition);
                }
                seekHandler.postDelayed(this, 1000);
            }
        };

        final Button prepareBtn = (Button) getWindow().getDecorView().findViewById(R.id.prepare_btn);
        final Button playBtn = (Button) getWindow().getDecorView().findViewById(R.id.play_btn);
        final Button pauseBtn = (Button) getWindow().getDecorView().findViewById(R.id.pause_btn);
        final Button seekBtn = (Button) getWindow().getDecorView().findViewById(R.id.seek_btn);
        prepareBtn .setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                debugTrack("Prepare button");
                if (player != null) {
                    player.release();
                    player = null;
                    playBtn.setEnabled(false);
                    pauseBtn.setEnabled(false);
                    seekBtn.setEnabled(false);
                }
                player = rdio.getPlayerForTrack("t2714517", "a224383", true);
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        Log.i(TAG, "OnPreparedListener duration: " + mp.getDuration());
                        seekBar.setMax(mp.getDuration());
                    }
                });
                try {
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    player.release();
                    player = null;
                    return;
                }

                playBtn.setEnabled(true);
                pauseBtn.setEnabled(true);
                seekBtn.setEnabled(true);
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                    player.start();
                    debugTrack("Play button");
                }
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                    player.pause();
                    debugTrack("Pause button");
                }
            }
        });

        seekBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                    player.seekTo(30000);
                    debugTrack("Seek button");
                }
            }
        });

        // Initialize our Rdio object.  If we have cached access credentials, then use them - otherwise
        // Initialize w/ null values and the user will be prompted (if the Rdio app is installed), or
        // we'll fallback to 30s samples.
        if (rdio == null) {
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            accessToken = settings.getString(PREF_ACCESSTOKEN, null);
            accessTokenSecret = settings.getString(PREF_ACCESSTOKENSECRET, null);

            rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret, this, this);

            if (accessToken == null || accessTokenSecret == null) {
                // If either one is null, reset both of them
                accessToken = accessTokenSecret = null;
                Intent myIntent = new Intent(MainActivity.this,
                        OAuth1WebViewActivity.class);
                myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_KEY, appKey);
                myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_SECRET, appSecret);
                MainActivity.this.startActivityForResult(myIntent, 1);

            } else {
                Log.d(TAG, "Found cached credentials:");
                Log.d(TAG, "Access token: " + accessToken);
                Log.d(TAG, "Access token secret: " + accessTokenSecret);
                rdio.prepareForPlayback();
            }
        }
    }

    public void debugTrack(String message) {
        if (player != null) {
            String position = String.valueOf(player.getCurrentPosition());
            String duration = String.valueOf(player.getDuration());
            Log.v(TAG, message + ": position: " + position + "/" + duration);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.v(TAG, "Login success");
                if (data != null) {
                    accessToken = data.getStringExtra("token");
                    accessTokenSecret = data.getStringExtra("tokenSecret");
                    rdio.setTokenAndSecret(accessToken, accessTokenSecret);
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (data != null) {
                    String errorCode = data.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_CODE);
                    String errorDescription = data.getStringExtra(OAuth1WebViewActivity.EXTRA_ERROR_DESCRIPTION);
                    Log.v(TAG, "ERROR: " + errorCode + " - " + errorDescription);
                }
                accessToken = null;
                accessTokenSecret = null;
            }
        }
    }

    @Override
    public void onRdioReadyForPlayback() {
        Log.i(TAG, "Ready for playback");
        final Button prepareBtn = (Button) getWindow().getDecorView().findViewById(R.id.prepare_btn);
        prepareBtn.setEnabled(true);
        seekHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void onRdioUserPlayingElsewhere() {

    }

    @Override
    public void onRdioAuthorised(String accessToken, String accessTokenSecret) {
        Log.i(TAG, "Application authorised, saving access token & secret.");
        Log.d(TAG, "Access token: " + accessToken);
        Log.d(TAG, "Access token secret: " + accessTokenSecret);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_ACCESSTOKEN, accessToken);
		editor.putString(PREF_ACCESSTOKENSECRET, accessTokenSecret);
        editor.apply();

        rdio.prepareForPlayback();
    }
}
