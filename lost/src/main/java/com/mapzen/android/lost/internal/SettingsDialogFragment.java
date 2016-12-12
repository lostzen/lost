package com.mapzen.android.lost.internal;

import com.mapzen.lost.R;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Handles displaying a dialog to prompt the user to resolve location settings
 */
public class SettingsDialogFragment extends DialogFragment implements
    DialogInterface.OnClickListener {

  private DialogInterface.OnClickListener externalListener;

  public void setOnClickListener(DialogInterface.OnClickListener listener) {
    externalListener = listener;
  }

  @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(this.getActivity())
        .setTitle(null)
        .setMessage(R.string.settings_alert_title)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.ok, this)
        .create();
  }

  @Override public void onClick(DialogInterface dialogInterface, int i) {
    if (externalListener != null) {
      externalListener.onClick(dialogInterface, i);
    }
  }

}
