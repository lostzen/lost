package com.example.lost;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/**
 * Base class for displaying a list of {@link Sample}s
 */
public abstract class SampleListActivity extends AppCompatActivity {

  abstract int numOfSamples();
  abstract Sample[] getSamples();

  View.OnClickListener onClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      ViewHolder viewHolder = (ViewHolder) v.getTag();
      startActivity(new Intent(SampleListActivity.this, viewHolder.activityClass));
    }
  };

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list_view);
    setupListView();
  }

  private void setupListView() {
    ListView listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(new BaseAdapter() {
      @Override public int getCount() {
        return numOfSamples();
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
          view = LayoutInflater.from(SampleListActivity.this).inflate(R.layout.sample_item, parent,
              false);
          holder = new ViewHolder(view);
          view.setTag(holder);
        } else {
          view = convertView;
          holder = (ViewHolder) view.getTag();
        }
        Sample sample = getSamples()[position];
        holder.title.setText(sample.getTitleId());
        holder.description.setText(sample.getDetailId());
        holder.activityClass = sample.getActivityClass();
        view.setOnClickListener(onClickListener);
        return view;
      }
    });
  }
}
