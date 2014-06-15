package com.indoorlocalizer.app.activity.offline;

import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.utils.MultiItemRowListAdapter;

import java.sql.SQLException;

public class ShowSavedMaps extends ListActivity {
    private  Cursor mCursor;
    private static final String[] FROM = {  DatabaseHelper.KEY_MAP_NAME, DatabaseHelper.KEY_ID,
                                            DatabaseHelper.KEY_NUMBER_OF_RP,
                                            DatabaseHelper.KEY_MAP_IMAGE_PATH};

    private static final int[] TO = {R.id.map_image_button,R.id.map_title};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_maps);
        final TextView emptyListMsg=(TextView)findViewById(R.id.empty_list_message);
        DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            // dbManager.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        if(mCursor.getCount()>0) {
            int spacing = (int)getResources().getDimension(R.dimen.spacing);
            int itemsPerRow = getResources().getInteger(R.integer.items_per_row);
            SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.map_selector_item, mCursor, FROM, TO, 0);
            MultiItemRowListAdapter wrapperAdapter = new MultiItemRowListAdapter(this.getApplicationContext(), mAdapter, itemsPerRow, spacing);
            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

                @Override
                public boolean setViewValue(View view, Cursor cursor, int i) {
                   //TODO: empty message in layout
                   //emptyListMsg.setVisibility(View.INVISIBLE);
                    /* VERSION 1.0 */
                    switch (view.getId()) {
                        case R.id.map_image_button:
                            final ImageButton outputImageButton = (ImageButton) view;
                            //TODO: right implementation image isn't a Drawable file yet!.
                            String map_icon_id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_IMAGE_PATH));
                            int image_id=getResources().getIdentifier(map_icon_id,"drawable",getPackageName());
                            Drawable image= getResources().getDrawable(image_id) ;
                            outputImageButton.setImageDrawable(image);
                            break;
                        case R.id.map_title:
                            final TextView outputTextView = (TextView) view;
                            String mapName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME));
                            outputTextView.setText(getResources().getString(R.string.map_value_pattern, mapName));
                            break;
                    }
                    return true;
                }
            });
            setListAdapter(wrapperAdapter);
        } else {
            emptyListMsg.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_saved_maps, menu);
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
