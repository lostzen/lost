package com.example.lost;

import android.view.View;
import android.widget.TextView;

public class ViewHolder {
  TextView title;
  TextView description;
  Class activityClass;

  ViewHolder(View view) {
    title = (TextView) view.findViewById(R.id.title);
    description = (TextView) view.findViewById(R.id.description);
  }
}
