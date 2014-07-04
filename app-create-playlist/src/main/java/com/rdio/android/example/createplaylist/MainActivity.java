package com.rdio.android.example.createplaylist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rdio.android.api.OAuth1WebViewActivity;
import com.rdio.android.api.Rdio;
import com.rdio.android.api.RdioApiCallback;
import com.rdio.android.api.RdioListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity implements RdioListener {

    private static final String appKey = "YOUR_CONSUMER_KEY";
    private static final String appSecret = "YOUR_CONSUMER_SECRET";

    private static Rdio rdio;

    private static String accessToken = null;
    private static String accessTokenSecret = null;

    private static final String PREF_ACCESSTOKEN = "prefs.accesstoken";
    private static final String PREF_ACCESSTOKENSECRET = "prefs.accesstokensecret";

    public static final String TAG = "RdioCreatePlaylist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        if (rdio == null) {
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            accessToken = settings.getString(PREF_ACCESSTOKEN, null);
            accessTokenSecret = settings.getString(PREF_ACCESSTOKENSECRET, null);

            rdio = new Rdio(appKey, appSecret, accessToken, accessTokenSecret, this, this);

            if (accessToken == null || accessTokenSecret == null) {
                // If either one is null, reset both of them
                accessToken = null;
                accessTokenSecret = null;

                Intent myIntent = new Intent(this, OAuth1WebViewActivity.class);
                myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_KEY, appKey);
                myIntent.putExtra(OAuth1WebViewActivity.EXTRA_CONSUMER_SECRET, appSecret);
                startActivityForResult(myIntent, 1);

            } else {
                Log.d(TAG, "Found cached credentials:");
                Log.d(TAG, "Access token: " + accessToken);
                Log.d(TAG, "Access token secret: " + accessTokenSecret);
                rdio.prepareForPlayback();
            }

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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
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
		Editor editor = settings.edit();
		editor.putString(PREF_ACCESSTOKEN, accessToken);
		editor.putString(PREF_ACCESSTOKENSECRET, accessTokenSecret);
		editor.apply();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void createPlaylist(View view) {
        Log.v(TAG, "Creating a playlist");

        List<NameValuePair> args = new LinkedList<NameValuePair>();
        args.add(new BasicNameValuePair("name", "My Sample Playlist"));
        args.add(new BasicNameValuePair("description", "This was created from an Android app"));
        args.add(new BasicNameValuePair("tracks", "t1,t2"));
        args.add(new BasicNameValuePair("isPublished", "false"));

        rdio.apiCall("createPlaylist", args, new RdioApiCallback() {
            @Override
            public void onApiFailure(String method, Exception e) {
                Log.e(TAG, "API method failed " + method + " " + e.getMessage());
            }

            @Override
            public void onApiSuccess(JSONObject jsonObject) {
                try {
                Log.v(TAG, "API method finished " + jsonObject.toString(2));
                } catch (JSONException e) {
                }
            }
        });
    }
}
