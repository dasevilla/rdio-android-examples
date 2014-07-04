package com.rdio.android.example.intents;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        private BroadcastReceiver rdioBroadcastReceiver = new BroadcastReceiver() {
            private String TAG = "RdioBroadcastReceiver";

            @Override
            public void onReceive(Context context, Intent intent) {
                String track = intent.getStringExtra("track");
                String artist = intent.getStringExtra("artist");
                String album = intent.getStringExtra("album");
                int position = intent.getIntExtra("position", 0);
                int duration = intent.getIntExtra("duration", 0);
                String rdioTrackKey = intent.getStringExtra("rdioTrackKey");
                String rdioSourceKey = intent.getStringExtra("rdioSourceKey");
                boolean isPaused = intent.getBooleanExtra("isPaused", false);
                boolean isPlaying = intent.getBooleanExtra("isPlaying", false);

                Log.i(TAG, intent.getAction() + " '" + track + "'(" + rdioTrackKey + ")" + " by '" + artist + "' on '" + album + "'(" + rdioSourceKey + ")" + " " + position + "/" + duration + " isPaused:" + isPaused + " isPlaying:" + isPlaying);
            }

        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            IntentFilter filter = new IntentFilter("com.rdio.android.playstatechanged");
            getActivity().registerReceiver(rdioBroadcastReceiver, filter);
            filter = new IntentFilter("com.rdio.android.metachanged");
            getActivity().registerReceiver(rdioBroadcastReceiver, filter);

            // Toggle between play and pause
            final Button toggleBtn = (Button) rootView.findViewById(R.id.toggle_btn);
            toggleBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = sendRdioPlayerControlIntent("rdio.android.action.playercontrol.toggle");
                    toggleBtn.getContext().sendBroadcast(intent);
                }
            });

            // Play
            final Button playBtn = (Button) rootView.findViewById(R.id.play_btn);
            playBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = sendRdioPlayerControlIntent("rdio.android.action.playercontrol.play");
                    playBtn.getContext().sendBroadcast(intent);
                }
            });

            // Pause
            final Button pauseBtn = (Button) rootView.findViewById(R.id.pause_btn);
            pauseBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = sendRdioPlayerControlIntent("rdio.android.action.playercontrol.pause");
                    pauseBtn.getContext().sendBroadcast(intent);
                }
            });

            // Skip forward
            final Button skipForwardBtn = (Button) rootView.findViewById(R.id.skipforward_btn);
            skipForwardBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = sendRdioPlayerControlIntent("rdio.android.action.playercontrol.skipforward");
                    skipForwardBtn.getContext().sendBroadcast(intent);
                }
            });

            // Skip backward
            final Button skipBackwardBtn = (Button) rootView.findViewById(R.id.skipbackward_btn);
            skipBackwardBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = sendRdioPlayerControlIntent("rdio.android.action.playercontrol.skipbackward");
                    skipBackwardBtn.getContext().sendBroadcast(intent);
                }
            });

            return rootView;
        }

        public Intent sendRdioPlayerControlIntent(String control){
            Intent intent = new Intent();
            intent.setAction("rdio.android.action.playercontrol");
            intent.putExtra("com.rdio.android.playercontrolkey", control);
            return intent;
        }

    }

}
