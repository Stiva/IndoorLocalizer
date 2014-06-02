package com.indoorlocalizer.app.activity.common;

import android.app.ListActivity;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.db.DbManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListAps extends ListActivity {
    private static final String[] FROM = {"ssid", "bssid", "capabilities", "level", "frequency"};

    private static final int[] TO = {R.id.list_item_ssid, R.id.list_item_bssid, R.id.list_item_capabilities,
            R.id.list_item_level, R.id.list_item_timestamp};
    private SimpleCursorAdapter mAdapter;
    private Cursor mCursor;
    private List<Map<String, Object>> mModel = new LinkedList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getAllAccessPoints();
           // dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_list_aps);
        mAdapter = new SimpleCursorAdapter(getApplicationContext(),R.layout.activity_list_aps,mCursor,FROM,TO, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
