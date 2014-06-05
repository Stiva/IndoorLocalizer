package com.indoorlocalizer.app.activity.common;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.db.DbManager;
import com.indoorlocalizer.app.activity.db.model.LocalDataCursor;

import java.sql.SQLException;

public class ListAps extends ListActivity{
    private static final String TAG="[List AP]";
    private static final String[] FROM = {DatabaseHelper.KEY_ID,DatabaseHelper.KEY_SSID,DatabaseHelper.KEY_BSSID,DatabaseHelper.KEY_CAPABILITIES, DatabaseHelper.KEY_LEVEL,DatabaseHelper.KEY_FREQUENCY};

    private static final int[] TO = {R.id.ssid, R.id.bssid, R.id.capabilities,R.id.level,R.id.frequency};
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_aps);
        DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getAllAccessPoints();
           // dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        mAdapter = new SimpleCursorAdapter(getApplicationContext(),R.layout.custom_list_item,mCursor,FROM,TO, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){

            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                final TextView outputTextView = (TextView) view;
                /* VERSION 1.0 */
                switch (view.getId()) {
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
                }
                return true;


                /* VERSION 2.0
                final LocalDataCursor localDataCursor=(LocalDataCursor) cursor;

                // We have to detect which is the item and show it
                switch (view.getId()) {
                    case R.id.ssid:
                        String ssid = localDataCursor.getSsid();
                        outputTextView.setText(getResources().getString(R.string.ssid_value_pattern, ssid));
                        break;
                    case R.id.bssid:
                        String bssid = localDataCursor.getBssid();
                        outputTextView.setText(getResources().getString(R.string.bssid_value_pattern, bssid));
                        break;
                    case R.id.capabilities:
                        String capabilities = localDataCursor.getCapabilities();
                        outputTextView.setText(getResources().getString(R.string.capabilities_value_pattern, capabilities));
                        break;
                    case R.id.level:
                        Integer level = localDataCursor.getLevel();
                        outputTextView.setText(getResources().getString(R.string.level_value_pattern, level));
                        break;
                    case R.id.frequency:
                        Integer frequency = localDataCursor.getFrequency();
                        outputTextView.setText(getResources().getString(R.string.frequency_value_pattern, frequency));
                        break;
                }
                return true;
                */
            }
        });
        setListAdapter(mAdapter);
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
