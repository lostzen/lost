package com.example.lost;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Base class for displaying a list of items with a title and description.
 */
public abstract class ListActivity extends AppCompatActivity {

  ListView listView;

  abstract int numOfItems();
  abstract List<Item> getItems();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_view);
    setupListView();
  }

  private void setupListView() {
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(new BaseAdapter() {
      @Override public int getCount() {
        return numOfItems();
      }

      @Override public Object getItem(int position) {
        return null;
      }

      @Override public long getItemId(int position) {
        return 0;
      }

      @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
          view = LayoutInflater.from(ListActivity.this).inflate(R.layout.sample_item, parent,
              false);
          holder = new ViewHolder(view);
          view.setTag(holder);
        } else {
          view = convertView;
          holder = (ViewHolder) view.getTag();
        }
        Item item = getItems().get(position);
        holder.title.setText(item.getTitle());
        holder.description.setText(item.getDetail());
        return view;
      }
    });
  }
}
