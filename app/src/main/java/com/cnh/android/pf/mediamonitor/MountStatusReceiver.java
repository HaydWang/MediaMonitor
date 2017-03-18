package com.cnh.android.pf.mediamonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by f10210c on 3/17/2017.
 */
public class MountStatusReceiver extends BroadcastReceiver {
    public final static String MOUNTED_PATH = "mounted_path";
    public final static String INTENT_ACTION = "intent_action";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d("f10210c", "Storage status received: " + intent.getAction());
            Toast.makeText(context, "Storage status received: " + intent.getAction(),
                    Toast.LENGTH_LONG).show();

            String path = "";
            if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
                path = intent.getDataString().replace("file://", "");
            }
            Log.d("f10210c", "Storage path received: " + path);

            // Start Test Activity
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MOUNTED_PATH, path);
            i.putExtra(INTENT_ACTION, intent.getAction());
            context.startActivity(i);
        }
    }
}
