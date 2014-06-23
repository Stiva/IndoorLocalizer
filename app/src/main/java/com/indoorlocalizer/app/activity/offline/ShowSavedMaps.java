package com.indoorlocalizer.app.activity.offline;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.indoorlocalizer.app.R;
import com.indoorlocalizer.app.activity.common.db.DatabaseHelper;
import com.indoorlocalizer.app.activity.common.db.DbManager;
import com.indoorlocalizer.app.activity.common.model.InfrastructureMap;
import com.indoorlocalizer.app.activity.common.utils.MultiItemRowListAdapter;
import com.indoorlocalizer.app.activity.offline.utils.ListAps;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShowSavedMaps extends ListActivity {
    private  Cursor mCursor;
    private CopyOnWriteArrayList<InfrastructureMap> savedMaps;
    private static final String[] FROM = {  DatabaseHelper.KEY_MAP_NAME, DatabaseHelper.KEY_ID,
                                            DatabaseHelper.KEY_NUMBER_OF_RP,
                                            DatabaseHelper.KEY_MAP_IMAGE_PATH};

    private static final int[] TO = {R.id.map_image_button,R.id.map_title};

    private SimpleCursorAdapter mAdapter;
    private MultiItemRowListAdapter wrapperAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_saved_maps);
        final TextView emptyListMsg=(TextView)findViewById(R.id.empty_map_list_message);
        DbManager dbManager=new DbManager(getApplicationContext());
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        try{
            dbManager.open();
            mCursor = dbManager.getMapNameList();
            if(mCursor.getCount()>0) {
                createMapFromCursor(mCursor);
                int spacing = (int)getResources().getDimension(R.dimen.spacing);
                int itemsPerRow = getResources().getInteger(R.integer.items_per_row);
                mAdapter= new SimpleCursorAdapter(this, R.layout.map_selector_item, mCursor, FROM, TO, 0);
                wrapperAdapter = new MultiItemRowListAdapter(this, mAdapter, itemsPerRow, spacing);
                mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int i) {
                        emptyListMsg.setVisibility(View.INVISIBLE);
                        switch (view.getId()) {
                            case R.id.map_image_button:
                                final ImageView outputImageView=(ImageView) view;
                                Bitmap resizedImage;
                                final String map_icon_id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_IMAGE_PATH));
                                Bitmap image=BitmapFactory.decodeFile(map_icon_id);
                                if(image!=null) {
                                    resizedImage = Bitmap.createScaledBitmap(image, 340, 340, true);
                                    outputImageView.setImageBitmap(resizedImage);
                                } else {
                                    try {
                                        Drawable dr=Drawable.createFromStream(getAssets().open("map_default_icon.png"),null);
                                        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
                                        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 340, 340, true));
                                        outputImageView.setImageDrawable(d);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                outputImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String mapName = selectMapName(map_icon_id);
                                        Intent intent = new Intent(getBaseContext(), ListAps.class);
                                        intent.putExtra("mapName", mapName);
                                        startActivity(intent);
                                    }
                                });
                                outputImageView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ShowSavedMaps.this);
                                        builder.setTitle(R.string.option_map_dialog)
                                                .setItems(R.array.string_option_name, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        switch (which) {
                                                            case 0:
                                                                String mapName = selectMapName(map_icon_id);
                                                                Intent intent = new Intent(ShowSavedMaps.this, ListAps.class);
                                                                intent.putExtra("mapName", mapName);
                                                                startActivity(intent);
                                                                break;
                                                            case 1:
                                                                deleteDialog(map_icon_id);
                                                                break;
                                                        }
                                                    }
                                                });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                        return true;
                                    }
                                });
                                break;
                            case R.id.map_title:
                                final TextView outputTextView = (TextView) view;
                                final String mapName=cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_MAP_NAME));
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
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void createMapFromCursor(Cursor mCursor) {
        savedMaps=new CopyOnWriteArrayList<InfrastructureMap>();
        while (mCursor.moveToNext()) {
            savedMaps.add(new InfrastructureMap(mCursor.getString(1),mCursor.getInt(2),mCursor.getString(3)));
        }
        mCursor.moveToFirst();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_saved_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private String selectMapName(String map_path){
        for(InfrastructureMap map:savedMaps){
            if(map.getMapImagePath().equals(map_path))
                return map.getMapName();
        }
        return null;
    }

    private void deleteDialog(final String map_icon_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowSavedMaps.this);
        builder.setMessage(R.string.delete_map_message)
                .setTitle(R.string.delete_map_title);
        builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String mapname=selectMapName(map_icon_id);
                cancelMap(mapname);
            }
        });
        builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void cancelMap(String mapName){
        //Remove the map from the dynamic ArrayList of savedMaps
        for(InfrastructureMap map:savedMaps)
            if(map.getMapName().equals(mapName))
                savedMaps.remove(map);
        //Remove the map from DB
        DbManager dbManager=new DbManager(getApplicationContext());
        try{
            dbManager.open();
            dbManager.deleteMapByName(mapName);
            dbManager.deleteApByMapName(mapName);
            dbManager.deleteRpByMapName(mapName);
            mCursor= dbManager.getMapNameList();
            mAdapter.swapCursor(mCursor);
            mAdapter.notifyDataSetChanged();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
