package com.indoorlocalizer.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.indoorlocalizer.app.activity.offline.WifiListMainMenu;


public class LocatorSelector extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator_selector);
        ToggleButton toggleWifi=(ToggleButton)findViewById(R.id.wifi_toggleButton);
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) {
            toggleWifi.setTextOn("ON");
        } else if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
            toggleWifi.setTextOn("OFF");
        }
        toggleWifi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                toggleWifi();
            }
        });
        final Button dataRetrieveButton=(Button) findViewById(R.id.data_retriving_button);
        dataRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retriveData();
            }
        });
    }

    private void toggleWifi() {
        ToggleButton toggleWifi=(ToggleButton)findViewById(R.id.wifi_toggleButton);
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) {
            wifiManager.setWifiEnabled(false);
            toggleWifi.setTextOn("OFF");
        } else if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
            toggleWifi.setTextOn("ON");
        }
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
    private void retriveData(){
        Intent retrieveDataIntent=new Intent(this,WifiListMainMenu.class);
        startActivity(retrieveDataIntent);
    }
}
