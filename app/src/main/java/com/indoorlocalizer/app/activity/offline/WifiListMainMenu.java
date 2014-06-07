package com.indoorlocalizer.app.activity.offline;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.ListAps;
import com.indoorlocalizer.app.activity.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.db.DbManager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@SuppressWarnings("NullableProblems")
public class WifiListMainMenu extends ListActivity {

    private static final String[] FROM = {DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES, DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY};

    private static final int[] TO = {R.id.ssid, R.id.bssid, R.id.capabilities,
            R.id.level,R.id.frequency};

    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;
    private List<ScanResult> wifiList;
    private SimpleAdapter mAdapter;
    private List<Map<String, Object>> mModel = new LinkedList<Map<String, Object>>();

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_scanner);
        mAdapter = new SimpleAdapter(this, mModel, R.layout.custom_list_item, FROM, TO);
        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                final TextView outputTextView = (TextView) view;
                // We have to detect which is the item and show it
                switch (view.getId()) {
                    case R.id.ssid:
                        String ssid = (String) o;
                        outputTextView.setText(getResources().getString(R.string.ssid_value_pattern, ssid));
                        break;
                    case R.id.bssid:
                        String bssid = (String) o;
                        outputTextView.setText(getResources().getString(R.string.bssid_value_pattern, bssid));
                        break;
                    case R.id.capabilities:
                        String capabilities = (String) o;
                        outputTextView.setText(getResources().getString(R.string.capabilities_value_pattern, capabilities));
                        break;
                    case R.id.level:
                        Integer level = (Integer) o;
                        outputTextView.setText(getResources().getString(R.string.level_value_pattern, level));
                        break;
                    case R.id.frequency:
                        Integer frequency = (Integer) o;
                        outputTextView.setText(getResources().getString(R.string.frequency_value_pattern, frequency));
                        break;
                }
                return true;
            }
        });
        getListView().setAdapter(mAdapter);
        // Initiate wifi service manager
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }

        // wifi scanned value broadcast receiver
        receiverWifi = new WifiReceiver();

        // Register broadcast receiver
        // Broadcast receiver will automatically call when number of wifi connections changed
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
    }

    private void showFingerPrints(){
        Intent showAPs=new Intent(this,ListAps.class);
        startActivity(showAPs);
    }
    private void createRP(){
        Intent createRP=new Intent(this,DataRetriever.class);
        startActivity(createRP);
    }
    //rpID=-1 means that the RP isn't set.
    public void saveFingerprint() {
        DbManager dbManager=new DbManager(getApplicationContext());
        try {
            dbManager.open();
            for(ScanResult res:wifiList){
                dbManager.addWifi(new AccessPoint("foo",-1,res.SSID,res.BSSID,res.capabilities,res.level,res.frequency));
            }
            dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.wifi_scanner,menu);
        return true;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_list_option:
                Toast.makeText(this, "Refresh in action", Toast.LENGTH_SHORT).show();
                mainWifi.startScan();
                mModel.clear();
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.save_fingerprint_option:
                saveFingerprint();
                return true;

            case R.id.get_stored_fingerprint_option:
                showFingerPrints();
                return true;
            case R.id.create_rp_option:
                createRP();
                return true;
        }
            return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    // Broadcast receiver class called its receive method 
    // when number of wifi connections changed

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            wifiList = mainWifi.getScanResults();
            for (ScanResult result:wifiList) {
                final Map<String, Object> item = new HashMap<String, Object>();
                item.put("ssid", result.SSID);
                item.put("bssid", result.BSSID);
                item.put("capabilities", result.capabilities);
                item.put("level", result.level);
                item.put("frequency", result.frequency);
                //Sometimes i get a compatibility error... dunno why... for now set min api lvl 17
                //item.put("timestamp",result.timestamp);
                mModel.add(item);
            }
            mAdapter.notifyDataSetChanged();
            getListView().setAdapter(mAdapter);
        }
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getApplicationContext(), "Selected position: " + position, Toast.LENGTH_SHORT).show();
    }

}