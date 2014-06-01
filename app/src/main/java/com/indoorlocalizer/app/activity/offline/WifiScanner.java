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
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
@SuppressWarnings("NullableProblems")
public class WifiScanner extends ListActivity {

    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;

    private static final String[] FROM = {"ssid", "bssid", "capabilities", "level", "frequency","timestamp"};

    private static final int[] TO = {R.id.list_item_ssid, R.id.list_item_bssid, R.id.list_item_capabilities,
            R.id.list_item_level, R.id.list_item_timestamp};

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
                    case R.id.list_item_ssid:
                        String ssid = (String) o;
                        outputTextView.setText(getResources().getString(R.string.ssid_value_pattern,ssid));
                        break;
                    case R.id.list_item_bssid:
                        String bssid = (String) o;
                        outputTextView.setText(getResources().getString(R.string.bssid_value_pattern, bssid));
                        break;
                    case R.id.list_item_capabilities:
                        String capabilities = (String) o;
                        outputTextView.setText(getResources().getString(R.string.capabilities_value_pattern, capabilities));
                        break;
                    case R.id.list_item_level:
                        Integer level = (Integer) o;
                        outputTextView.setText(getResources().getString(R.string.level_value_pattern, level));
                        break;
                    case R.id.list_item_frequency:
                        Integer frequency = (Integer) o;
                        outputTextView.setText(getResources().getString(R.string.frequency_value_pattern, frequency));
                        break;
                    case R.id.list_item_timestamp:
                        Integer timestamp= (Integer) o;
                        outputTextView.setText(getResources().getString(R.string.timestamp_value_pattern,timestamp));
                        break;
                }
                return true;
            }
        });
        getListView().setAdapter(mAdapter);
        // Initiate wifi service manager
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled())
        {
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

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mModel.clear();
        mAdapter.notifyDataSetChanged();
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
                item.put("timestamp",result.timestamp);
                mModel.add(item);
            }
            mAdapter.notifyDataSetChanged();
            getListView().setAdapter(mAdapter);
        }
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getApplicationContext(), "Selected position: " + position, Toast.LENGTH_SHORT).show();
        final Intent saveIntent = new Intent(this, SaveData.class);
        startActivity(saveIntent);
    }
}