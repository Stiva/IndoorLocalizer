package com.indoorlocalizer.app.activity.offline.utils;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;

import java.sql.SQLException;

public class ListAps extends ListActivity{
    private static final String[] FROM = {  DatabaseHelper.KEY_MAP_NAME,DatabaseHelper.KEY_REFERENCE_POINT_ID,
                                            DatabaseHelper.KEY_ID,DatabaseHelper.KEY_SSID,
                                            DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES, DatabaseHelper.KEY_LEVEL,
                                            DatabaseHelper.KEY_FREQUENCY,DatabaseHelper.KEY_HITS};

    private static final int[] TO = {R.id.map,R.id.reference_point,R.id.ssid, R.id.bssid, R.id.capabilities,R.id.level,R.id.frequency,R.id.hits};
    private Cursor mCursor,rpCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_aps);
        Intent intent=getIntent();
        String mapName=intent.getExtras().getString("mapName");
        final TextView emptyListMsg=(TextView)findViewById(R.id.empty_list_message);
        final DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getAccessPointByMap(mapName);
           // dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        if(mCursor.getCount()>0) {
            SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, R.layout.wifi_list_item, mCursor, FROM, TO, 0);
            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

                @Override
                public boolean setViewValue(View view, Cursor cursor, int i) {
                    final TextView outputTextView = (TextView) view;
                    emptyListMsg.setVisibility(View.INVISIBLE);
                    /* VERSION 1.0 */
                    switch (view.getId()) {
                        case R.id.map:
                            String map = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME));
                            outputTextView.setText(getResources().getString(R.string.map_value_pattern, map));
                            break;
                        case R.id.reference_point:
                            Integer rp = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_REFERENCE_POINT_ID));
                            String rpName = "";
                            try{
                                dbManager.open();
                                rpName = dbManager.getRpName(rp);
                                // dbManager.close();
                            } catch (SQLException e){
                                e.printStackTrace();
                            }
                            outputTextView.setText(getResources().getString(R.string.rp_value_pattern, rp,rpName));
                            break;
                        case R.id.ssid:
                            String ssid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SSID));
                            outputTextView.setText(getResources().getString(R.string.ssid_value_pattern, ssid));
                            break;
                        case R.id.bssid:
                            String bssid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BSSID));
                            outputTextView.setText(getResources().getString(R.string.bssid_value_pattern, bssid));
                            break;
                        case R.id.capabilities:
                            String capabilities = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CAPABILITIES));
                            outputTextView.setText(getResources().getString(R.string.capabilities_value_pattern, capabilities));
                            break;
                        case R.id.level:
                            Integer level = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LEVEL));
                            outputTextView.setText(getResources().getString(R.string.level_value_pattern, level));
                            break;
                        case R.id.frequency:
                            Integer frequency = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FREQUENCY));
                            outputTextView.setText(getResources().getString(R.string.frequency_value_pattern, frequency));
                            break;
                        case R.id.hits:
                            Integer hits = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_HITS));
                            outputTextView.setText(getResources().getString(R.string.hits_value_pattern, hits));
                            break;
                    }
                    return true;
                }
            });
            setListAdapter(mAdapter);
        } else {
            emptyListMsg.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_aps, menu);
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
}
