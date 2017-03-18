package com.cnh.android.pf.mediamonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    protected final static String READ_TIMES = "read_times";

    protected SharedPreferences prefs;

    protected int readTimes = 1;
    protected String mountedPath = "";
    protected String intentReceived = "None";

    private static String getExtendedMemoryPath(Context context) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = this.getPreferences(Context.MODE_PRIVATE);
        int times = prefs.getInt(READ_TIMES, 1);
        EditText et = (EditText)findViewById(R.id.edit_read);
        et.setText(Integer.toString(times));

        TextView tvSDCard = (TextView)findViewById(R.id.text_sdcard);
        tvSDCard.setText("getExternalStorageDirectory(): " + Environment.getExternalStorageDirectory().getPath());

        TextView tvUSB = (TextView)findViewById(R.id.text_usb);
        tvUSB.setText("getExternalUSBDirectory(): " + getExtendedMemoryPath(this));

        TextView tvFiles = (TextView)findViewById(R.id.text_files);
        tvFiles.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void onPause() {
        super.onPause();

        EditText et = (EditText)findViewById(R.id.edit_read);
        if (et.getText() == null || TextUtils.isEmpty(et.getText())) {
            readTimes = 1;
        } else {
            readTimes = Integer.parseInt(et.getText().toString());
        }
        prefs.edit().putInt("read_times", readTimes);
        prefs.edit().apply();
    }

    @Override
    public void onResume() {
        super.onResume();

        mountedPath = "";
        intentReceived = "";
        if (getIntent() != null) {
            mountedPath = getIntent().getStringExtra(MountStatusReceiver.MOUNTED_PATH);
            intentReceived = getIntent().getStringExtra(MountStatusReceiver.INTENT_ACTION);
        }

        TextView tvPath = (TextView)findViewById(R.id.text_path);
        if (mountedPath == null || mountedPath.isEmpty()) {
            tvPath.setVisibility(View.GONE);
        } else {
            tvPath.setText("Mounted Storage: " + mountedPath);
            //tvPath.setVisibility(View.VISIBLE); // Finally I decided dont show this on UI
        }

        TextView tvStatus = (TextView)findViewById(R.id.text_intent_received);
        if (intentReceived == null || intentReceived.isEmpty()) {
            tvStatus.setText("None");
        } else {
            tvStatus.setText(intentReceived);
        }

        EditText et = (EditText)findViewById(R.id.edit_read);
        if (et.getText() == null || TextUtils.isEmpty(et.getText())) {
            et.setText(Integer.toString(readTimes));
        }

        Button button = (Button)findViewById(R.id.button_test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText)findViewById(R.id.edit_read);
                if (et.getText() == null || TextUtils.isEmpty(et.getText())) {
                    readTimes = 1;
                    et.setText(Integer.toString(readTimes));
                } else {
                    readTimes = Integer.parseInt(et.getText().toString());
                }
                readExternalStorage(readTimes, mountedPath);
            }
        });

        if (mountedPath != null && !mountedPath.isEmpty()) {
            readExternalStorage(readTimes, mountedPath);
        }
    }

    private void readExternalStorage(int times, String path) {
        if (path == null || path.isEmpty()) {
            //Toast.makeText(this, "External USB storage not valid.", Toast.LENGTH_SHORT).show();
            //return;
            path = getExtendedMemoryPath(this);
        }

        Toast.makeText(this, "Start reading storage " + path, Toast.LENGTH_SHORT).show();
        File root = new File(path);
        if (root == null) {
            Toast.makeText(this, "Read storage " + path + " failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView tv = (TextView)findViewById(R.id.text_files);
        for (int i=1; i<=times; i++) {
            tv.append("-------------READ STORAGE-------------\r\n");
            readWholeFold(root);
        }
        Toast.makeText(this, "Read storage " + times + " times finished.", Toast.LENGTH_SHORT).show();
    }

    private void readWholeFold(File path) {
        File[] files = path.listFiles();
        if (files == null) {
            Toast.makeText(this, "Read storage " + path + " failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView tv = (TextView)findViewById(R.id.text_files);
        for (File file : files) {
            if (file.isDirectory()) {
                if (tv != null) {
                    tv.append(file.getAbsolutePath() + "\r\n");
                }
                readWholeFold(file);
            } else {
                if (tv != null) {
                    tv.append(file.getAbsolutePath() + "/" + file.getName() + "\r\n");
                }
            }
        }
    }
}
