package com.indoorlocalizer.app.activity.offline.fragment;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.offline.utils.OfflineUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShowWifiFragment extends ListFragment {
    private static final String[] FROM = {DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES, DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY};

    private static final int[] TO = {R.id.ssid, R.id.bssid, R.id.capabilities,
            R.id.level,R.id.frequency};
    private List<HashMap<String,Object>> mModel = new ArrayList<HashMap<String,Object>>();
    private SimpleAdapter mAdapter;
    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;
    private List<ScanResult> wifiList;
    private ProgressBar progressBar;
    private TextView textView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        progressBar=(ProgressBar)getActivity().findViewById(R.id.wifi_search_progress);
        textView=(TextView)getActivity().findViewById(R.id.search_wifi_text_view);
        mAdapter = new SimpleAdapter(getActivity().getBaseContext(), mModel, R.layout.wifi_list_item_simple, FROM, TO);
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
        // Initiate wifi service manager
        mainWifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!mainWifi.isWifiEnabled()) {
            // If wifi disabled then enable it
            Toast.makeText(getActivity().getBaseContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }

        // wifi scanned value broadcast receiver
        receiverWifi = new WifiReceiver();

        // Register broadcast receiver
        // Broadcast receiver will automatically call when number of wifi connections changed
        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item

        setListAdapter(mAdapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    //rpID=-1 means that the RP isn't set.
    public void refresh(){
        mModel.clear();
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        mainWifi.startScan();
        mAdapter.notifyDataSetChanged();
    }

    public void onPause() {
        getActivity().unregisterReceiver(receiverWifi);
        super.onPause();
    }

    public void onResume() {
        getActivity().registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public void saveFingerprint(Context c,String mapName) {
        OfflineUtils.saveFingerprint(c,mapName,wifiList);
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            wifiList = mainWifi.getScanResults();
            for (ScanResult result:wifiList) {
                final HashMap<String, Object> item = new HashMap<String, Object>();
                item.put("ssid", result.SSID);
                item.put("bssid", result.BSSID);
                item.put("capabilities", result.capabilities);
                item.put("level", result.level);
                item.put("frequency", result.frequency);
                mModel.add(item);
            }
            mAdapter.notifyDataSetChanged();
            progressBar=(ProgressBar)getActivity().findViewById(R.id.wifi_search_progress);
            textView=(TextView)getActivity().findViewById(R.id.search_wifi_text_view);
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            getListView().setAdapter(mAdapter);
        }
    }
}
