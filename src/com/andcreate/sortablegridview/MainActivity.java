/*
 * Copyright 2014 gari_jp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.andcreate.sortablegridview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    
    private static final int NUM_COLUMN = 5;
    private static final int NUM_ROW = 7;
    
    private SortableGridView mSortableGridView;
    private MyAdapter mAdapter = null;
    private List<Item> mItems = new ArrayList<MainActivity.Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        for (int i = 1; i <= NUM_COLUMN * NUM_ROW; i++) {
            mItems.add(new Item(R.drawable.ic_launcher, "item" + i));
        }
        
        mAdapter = new MyAdapter(this, mItems);
        
        mSortableGridView = (SortableGridView)findViewById(R.id.sortable_grid);
        mSortableGridView.setAdapter(mAdapter);
        mSortableGridView.setNumColumns(NUM_COLUMN);
        mSortableGridView.setOnDragAndDropListener(new DragAndDropListener() {
            
            @Override
            public void dropped(int from, int to) {
                Collections.swap(mItems, from, to);
                mSortableGridView.invalidateViews();
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_start:
                mSortableGridView.setSortMode(true);
                break;
            case R.id.action_sort_stop:
                mSortableGridView.setSortMode(false);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSortableGridView.getSortMode()) {
                mSortableGridView.setSortMode(false);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class Item {
        public int icon;
        public String name;
        
        Item(int icon, String name) {
            this.icon = icon;
            this.name = name;
        }
    }
    
    private class MyAdapter extends ArrayAdapter<Item> {
        private LayoutInflater mInflater;
        
        public MyAdapter(Context context, List<Item> items) {
            super(context, R.layout.item, items);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item, null);
            }
            
            Item item = getItem(position);
            
            TextView textView = (TextView)convertView;
            textView.setText(item.name);
            Drawable icon = getResources().getDrawable(item.icon);
            icon.setBounds(0, 0, 100, 100);
            textView.setCompoundDrawables(null, icon, null, null);
            textView.setHeight(parent.getHeight() / NUM_ROW);
            
            return textView;
        }
    }
}
