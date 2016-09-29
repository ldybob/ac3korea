package com.ldybob.ac3korea;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class MyDialog extends DialogFragment {
    int msg;
    DialogInterface.OnClickListener listener;

    public MyDialog(int msgID, DialogInterface.OnClickListener clickListener) {
        msg = msgID;
        listener = clickListener;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg)
                .setPositiveButton(R.string.confirm, listener)
                .setNegativeButton(R.string.cancel, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
