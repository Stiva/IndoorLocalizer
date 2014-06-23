package com.indoorlocalizer.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.SettingsActivity;
import com.indoorlocalizer.app.activity.offline.OfflineOptionsListMainMenu;
import com.indoorlocalizer.app.activity.online.Localization;

/*
 * This activity is the first that appear when splash images gets off.
 * It gives you the possibility to choose between the offline and the online services of the
 * Android indoor localizer.
 * OFFLINE MENU:
 * Data retrieving, Database management, Fingerprint creation
 * ONLINE MENU:
 * Localization on the map
 */
public class LocatorSelector extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator_selector);
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
            Toast.makeText(this,"Wifi must be enabled to run this application",Toast.LENGTH_LONG).show();
        }
        ImageView retrieveImageView=(ImageView)findViewById(R.id.retrieve_start_image);
        retrieveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveData();
            }
        });
        ImageView localizeImageView=(ImageView)findViewById(R.id.localize_start_image);
        localizeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localize();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.locator_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }



    private void retrieveData(){
        Intent retrieveDataIntent=new Intent(this,OfflineOptionsListMainMenu.class);
        startActivity(retrieveDataIntent);
    }

    private void localize(){
        Intent localizationIntent=new Intent(this.getApplicationContext(),Localization.class);
        startActivity(localizationIntent);
    }
}
