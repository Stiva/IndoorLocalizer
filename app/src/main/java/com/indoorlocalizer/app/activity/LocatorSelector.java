package com.indoorlocalizer.app.activity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.offline.OfflineOptionsListMainMenu;
import com.indoorlocalizer.app.activity.online.Localization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * This activity is the first that appear when splash images gets off.
 * It gives you the possibility to choose between the offline and the online services of the
 * Android indoor localizer.
 * OFFLINE MENU:
 * Data retrieving, Database management, Fingerprint creation
 * ONLINE MENU:
 * Localization on the map
 */
public class LocatorSelector extends ListActivity {
    private ToggleButton toggleWifi;
    private WifiManager wifiManager;
    private static final String[] FROM = {"option_name","option_description"};
    private static final int[] TO = {R.id.option_name, R.id.option_description};
    private List<Map<String, Object>> mModel = new LinkedList<Map<String, Object>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator_selector);
        toggleWifi=(ToggleButton)findViewById(R.id.wifi_toggleButton);
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) {
            toggleWifi.toggle();
        }
        toggleWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleWifi();
            }
        });
        createOptionList();
        SimpleAdapter mAdapter = new SimpleAdapter(this, mModel, R.layout.option_list_item, FROM, TO);
        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                final TextView outputTextView = (TextView) view;
                // We have to detect which is the item and show it
                switch (view.getId()) {
                    case R.id.option_name:
                        String optionName = (String) o;
                        outputTextView.setText(getResources().getString(R.string.option_name_pattern, optionName));
                        break;
                    case R.id.option_description:
                        String optionDescription = (String) o;
                        outputTextView.setText(getResources().getString(R.string.option_description_pattern, optionDescription));
                        break;
                }
                return true;
            }
        });
        getListView().setAdapter(mAdapter);
    }

    private void toggleWifi() {
        if (wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED) {
            wifiManager.setWifiEnabled(false);
            toggleWifi.toggle();
            toggleWifi.setChecked(false);
        } else if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
            toggleWifi.toggle();
            toggleWifi.setChecked(true);
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
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position){
            case 0:
                retrieveData();
                break;
            case 1:
                localize();
                break;
        }
    }

    private void retrieveData(){
        Intent retrieveDataIntent=new Intent(this,OfflineOptionsListMainMenu.class);
        startActivity(retrieveDataIntent);
    }

    private void localize(){
        Intent localizationIntent=new Intent(this.getApplicationContext(),Localization.class);
        startActivity(localizationIntent);
    }

    private void createOptionList(){
        Map<String, Object> item = new HashMap<String, Object>();
        //TODO: XML integration for option menu persistence
        item.put("option_name","Retrieve");
        item.put("option_description","Retrieve data for a new map/reference point");
        mModel.add(item);

    }
}
