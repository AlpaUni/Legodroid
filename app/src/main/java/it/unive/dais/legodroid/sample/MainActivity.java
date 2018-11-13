package it.unive.dais.legodroid.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.Plug;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Prelude.ReTAG("MainActivity");

    private TextView textView;
    private final Map<String, Object> statusMap = new HashMap<>();
    @Nullable
    private EV3 ev3;
    @Nullable
    private TachoMotor motor;

    private void updateStatus(Plug p, String key, Object value) {
        Log.d(TAG, String.format("%s: %s: %s", p, key, value));
        statusMap.put(key, value);
        runOnUiThread(() -> textView.setText(statusMap.toString()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);

        try {
            ev3 = new EV3(new BluetoothConnection("EV3").connect());

            Button stopButton = findViewById(R.id.stopButton);
            stopButton.setOnClickListener(v -> ev3.cancel());

            Button startButton = findViewById(R.id.startButton);
            startButton.setOnClickListener(v -> {
                try {
                    ev3.run(this::legomain);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });

            EditText e1 = findViewById(R.id.powerEdit);
            e1.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int speed = 0;
                    try {
                        speed = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (motor != null) {
                        updateStatus(motor, "power", speed);
                        try {
                            motor.setPower(speed);
                            motor.start();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });

            EditText e2 = findViewById(R.id.speedEdit);
            e2.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int speed = 0;
                    try {
                        speed = Integer.parseInt(s.toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (motor != null) {
                        updateStatus(motor, "speed", speed);
                        try {
                            motor.setSpeed(speed);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "fatal error: cannot connect to EV3");
            e.printStackTrace();
        }

    }

    // main program executed by EV3

    private void legomain(EV3.Api api) {
        LightSensor lightSensor = api.getLightSensor(EV3.InputPort._3);
        TouchSensor touchSensor = api.getTouchSensor(EV3.InputPort._1);
        GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._4);
        motor = api.getTachoMotor(EV3.OutputPort.A);
        try {
            motor.start();

            while (!api.ev3.isCancelled()) {
                try {
                    Future<Float> pos1 = motor.getPosition();
                    updateStatus(motor, "position", pos1.get());

                    Future<Float> gyro = gyroSensor.getAngle();
                    updateStatus(gyroSensor, "angle", gyro.get());

                    Future<Short> ambient = lightSensor.getAmbient();
                    updateStatus(lightSensor, "ambient", ambient.get());

                    Future<Short> reflected = lightSensor.getReflected();
                    updateStatus(lightSensor, "reflected", reflected.get());

                    Future<LightSensor.Color> colf = lightSensor.getColor();
                    LightSensor.Color col = colf.get();
                    updateStatus(lightSensor, "color", col);
                    runOnUiThread(() -> findViewById(R.id.colorView).setBackgroundColor(col.toARGB32()));

//                    Future<LightSensor.Rgb> rgb = lightSensor.getRgb();
//                    LightSensor.Rgb rgbv = rgb.get();
//                    updateStatus(lightSensor, "rgb", String.format("0x%06x", rgbv.toRGB24()));

                    Future<Boolean> touched = touchSensor.getPressed();
                    updateStatus(touchSensor, "touch", touched.get() ? 1 : 0);

                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                motor.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}


