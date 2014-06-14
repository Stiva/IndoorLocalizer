package com.indoorlocalizer.app.activity.offline;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.AccessPoint;
import com.indoorlocalizer.app.activity.common.model.InfrastructureMap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ShowWifi extends ListActivity implements InsertMapNameDialog.InsertMapNameDialogListener{

    private static final String[] FROM = {DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES, DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY};

    private static final int[] TO = {R.id.ssid, R.id.bssid, R.id.capabilities,
            R.id.level,R.id.frequency};

    private WifiManager mainWifi;
    private WifiReceiver receiverWifi;
    private List<ScanResult> wifiList;
    private String imageFilePath="ic_launcher";
    private SimpleAdapter mAdapter;
    private List<Map<String, Object>> mModel = new LinkedList<Map<String, Object>>();
    private static final int PICK_IMAGE = 1;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_wifi);
        mAdapter = new SimpleAdapter(this, mModel, R.layout.wifi_list_item_simple, FROM, TO);
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
    //rpID=-1 means that the RP isn't set.
    public void saveFingerprint(String mapName) {
        DbManager dbManager=new DbManager(getApplicationContext());
        try {
            dbManager.open();
            for(ScanResult res:wifiList){
                dbManager.addWifi(new AccessPoint(mapName,1,res.SSID,res.BSSID,res.capabilities,res.level,res.frequency));
            }
            dbManager.addMap(new InfrastructureMap(mapName,1,imageFilePath));
            dbManager.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_wifi, menu);
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
                InsertMapNameDialog dialog=new InsertMapNameDialog();
                dialog.show(getFragmentManager(), "Insert map name");
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText edit=(EditText)dialog.getDialog().findViewById(R.id.map_name_editText);
        String mapName=edit.getText().toString();

        saveFingerprint(mapName);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
    @Override
    public void onButtonClick(DialogFragment dialog) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            Uri _uri = data.getData();

            //User had pick an image.
            Cursor cursor = getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();

            //Link to the image
            imageFilePath = cursor.getString(0);
            cursor.close();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
