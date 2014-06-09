package com.indoorlocalizer.app.activity.offline;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.ListAps;
import com.indoorlocalizer.app.activity.common.model.OptionElement;
import com.indoorlocalizer.app.activity.common.xml.XmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/*
 * Main activity for the offline options.
 */
public class OfflineOptionsListMainMenu extends ListActivity {

    private static final String[] FROM = {"option_name","option_description"};
    private static final int[] TO = {R.id.option_name, R.id.option_description};
    private List<Map<String, Object>> mModel = new LinkedList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_scanner);
        createOptionListXML();
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

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_stored_fingerprint_option:
                showFingerPrints();
                return true;
            case R.id.create_rp_option:
                createRP();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                showWifi();
                break;
            case 1:
                showFingerPrints();
                break;
            case 2:
                createRP();
                break;
        }
    }

    private void createOptionList() {

        Map<String, Object> item = new HashMap<String, Object>();
        Map<String, Object> item2 = new HashMap<String, Object>();
        Map<String, Object> item3 = new HashMap<String, Object>();
        //TODO: XML integration for option menu persistence
        item.put("option_name","Show WiFi");
        item.put("option_description","Shows WiFi Access Point on your location");
        item2.put("option_name","Get stored fingerprints");
        item2.put("option_description","Retrieve fingerprints that are previously stored into the Database");
        item3.put("option_name","Create reference point");
        item3.put("option_description","Use your current position to retrieve network status and store a new fingerprint");
        mModel.add(item);
        mModel.add(item2);
        mModel.add(item3);
    }

    private void createOptionListXML(){
        List results=new ArrayList<OptionElement>();
        try {
            XmlParser parser=new XmlParser();
            InputStream in_s = getApplicationContext().getAssets().open("optionsMenu.xml");
            results = parser.parse(in_s);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showWifi(){
        Intent showWifi=new Intent(this.getApplicationContext(),ShowWifi.class);
        startActivity(showWifi);
    }
    private void showFingerPrints(){
        Intent showAPs=new Intent(this,ListAps.class);
        startActivity(showAPs);
    }

    private void createRP(){
        Intent createRP=new Intent(this,DataRetriever.class);
        startActivity(createRP);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.wifi_scanner,menu);
        return true;
    }

}