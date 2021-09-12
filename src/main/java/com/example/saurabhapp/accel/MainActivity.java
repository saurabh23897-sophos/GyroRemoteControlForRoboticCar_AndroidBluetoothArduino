package com.example.saurabhapp.accel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    int x = 0, y = 0;
    String ss;
    Button bDisconnect, bConnect, start, stop;
    Boolean found = false, connected = false, run = false;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private BluetoothDevice device = null;
    private OutputStream outStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String address = "20:16:05:24:75:57";

    SensorManager sensorManager;
    Sensor sensor;
    TextView x_sensor, y_sensor, xy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        x_sensor = (TextView) findViewById(R.id.xaccel);
        y_sensor = (TextView) findViewById(R.id.yaccel);
        xy = (TextView) findViewById(R.id.accel);

        bConnect = (Button) findViewById(R.id.connect);
        bDisconnect = (Button) findViewById(R.id.disconnect);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        bConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });

        bDisconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    startSend();
                    run = true;
                }
            }
        });

        stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    run = false;
                }
            }
        });
    }

    public void startSend(){
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                x = (int) (event.values[0] * 1.5);
                y = (int) (event.values[1] * 2);
                if (x > 9) {
                    x = 9;
                }
                if (y > 9) {
                    y = 9;
                }
                if (x < -9) {
                    x = -9;
                }
                if (y < -9) {
                    y = -9;
                }
                if (run) {
                    ss = "";
                    if (x >= 0) {
                        ss += "+" + x;
                    } else {
                        ss += "" + x;
                    }
                    if (y >= 0) {
                        ss += "+" + y;
                    } else {
                        ss += "" + y;
                    }
                    ss += ".";
                    if (ss.length() == 5) {
                        send(ss);
                    }
                }
                x_sensor.setText(String.format(Locale.US, "x = %d", x));
                y_sensor.setText(String.format(Locale.US, "y = %d", y));
                xy.setText(ss);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void connect() {
        if (btAdapter == null) {
            Toast.makeText(MainActivity.this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (!(btAdapter.isEnabled())) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

            Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();

            if (bondedDevices.isEmpty()) {
                Toast.makeText(MainActivity.this, "No device paired", Toast.LENGTH_SHORT).show();
            } else {
                for (BluetoothDevice iterator : bondedDevices) {
                    if (iterator.getAddress().equals(address)) {
                        device = iterator;
                        found = true;
                        break;
                    }
                }
            }
        }

        if (found) {
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();
                connected = true;
                Toast.makeText(MainActivity.this, "Connected socket!!!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                try {
                    btSocket.close();
                    connected = false;
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Closed socket!!!", Toast.LENGTH_SHORT).show();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    Toast.makeText(MainActivity.this, e2.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (connected) {
                try {
                    outStream = btSocket.getOutputStream();
                } catch (IOException e3) {
                    Toast.makeText(MainActivity.this, e3.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MainActivity.this,"Device not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnect() {
        if (device != null) {
            try {
                btSocket.close();
                btSocket = null;
                device = null;
                connected = false;
                found = false;
                Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Closed Socket", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "First connect to the device", Toast.LENGTH_SHORT).show();
        }
    }

    public void send(String msg) {
        try {
            outStream.write(msg.getBytes());
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Cannot send data", Toast.LENGTH_SHORT).show();
        }
    }
}