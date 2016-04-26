/**
 *  Copyright 2016 Huy Phan <huyphan@playxiangqi.com>
 * 
 *  This file is part of HOXChess.
 * 
 *  HOXChess is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  HOXChess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with HOXChess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.playxiangqi.hoxchess;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TablesActivity extends Activity
                implements PlayerManager.EventListener {

    private static final String TAG = "TablesActivity";

    private View inProgressView_;
    private ListView tablesListView_;

    private TablesAdapter adapter_;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);
        Log.d(TAG, "onCreate:");

        inProgressView_ = findViewById(R.id.inProgressLayout);
        tablesListView_ = (ListView)findViewById(R.id.list_tables);
        
        adapter_ = new TablesAdapter(this, R.layout.listview_item_table);
        tablesListView_.setAdapter(adapter_);
        
        tablesListView_.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TableInfo itemValue = (TableInfo) tablesListView_.getItemAtPosition(position);
                
                Log.d(TAG, "Position:" + position + " TableId: " + itemValue.tableId
                        + ", ListItem: " + itemValue);
                
                // Return the table-ID.
                Intent result = new Intent();
                result.putExtra("tid", itemValue.tableId);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        
    }

    @Override
    public void onPlayersLoaded() {
        Log.d(TAG, "onPlayersLoaded: Do nothing.");
    }

    @Override
    public void onTablesLoaded() {
        Log.d(TAG, "onTablesLoaded:");

        List<TableInfo> tables = PlayerManager.getInstance().getTables();
        Log.d(TAG, "onTablesLoaded: # of tables = " + tables.size());

        refreshListViewIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume:");
        if (!refreshListViewIfNeeded()) {
            PlayerManager.getInstance().addListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause:");
        PlayerManager.getInstance().removeListener(this);
    }

    private boolean refreshListViewIfNeeded() {
        if (!PlayerManager.getInstance().areTablesLoaded()) {
            Log.d(TAG, "refreshListViewIfNeeded: The table LIST is not yet loaded.");
            return false;
        }

        if (inProgressView_.getVisibility() != View.GONE) {
            inProgressView_.setVisibility(View.GONE);
            tablesListView_.setVisibility(View.VISIBLE);
        }
        adapter_.refreshTables();
        return true;
    }

    /**
     * The custom adapter for our list view.
     */
    private static class TablesAdapter extends BaseAdapter {
        private final Activity activity_;
        private final int resourceId_;
        private final HashMap<Integer, TableInfo> mIdMap_ = new HashMap<Integer, TableInfo>();

        public TablesAdapter(Activity context, int textViewResourceId) {
            activity_ = context;
            resourceId_ = textViewResourceId;
        }

        public void refreshTables() {
            mIdMap_.clear();
            final List<TableInfo> tables = PlayerManager.getInstance().getTables();
            for (int i = 0; i < tables.size(); ++i) {
                mIdMap_.put(Integer.valueOf(i), tables.get(i));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mIdMap_.size();
        }

        @Override
        public Object getItem(int position) {
            return mIdMap_.get(Integer.valueOf(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater layoutInflater = activity_.getLayoutInflater();
                convertView = layoutInflater.inflate(resourceId_, null);
                holder = new ViewHolder();
                holder.tableIdView = (TextView) convertView.findViewById(R.id.table_id);
                holder.playersInfoView = (TextView) convertView.findViewById(R.id.players_info);
                holder.gameInfoView = (TextView) convertView.findViewById(R.id.game_info);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final TableInfo tableInfo = (TableInfo) getItem(Integer.valueOf(position));
            holder.tableIdView.setText(tableInfo.tableId);
            holder.playersInfoView.setText(
                    String.format("%s vs. %s", tableInfo.getRedInfo(), tableInfo.getBlackInfo()));
            holder.gameInfoView.setText(tableInfo.itimes);

            return convertView;
        }

    }

    /**
     * The view holder for our custom adapter.
     */
    private static class ViewHolder {
        public TextView tableIdView;
        public TextView playersInfoView;
        public TextView gameInfoView;
    }
    
}
