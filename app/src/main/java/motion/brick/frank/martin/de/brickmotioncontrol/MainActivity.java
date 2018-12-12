package motion.brick.frank.martin.de.brickmotioncontrol;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Timer;
import java.util.TimerTask;

import static motion.brick.frank.martin.de.brickmotioncontrol.MathUtil.limit;

public class MainActivity extends AppCompatActivity implements MotionInputListener {

    private Control control = Control.CATERPILLAR;
    private static final String STORE_NAME = "motion.brick.frank.martin.de.brickmotioncontrol.store";
    private static final String STORE_KEY_SERVER = "motion.brick.frank.martin.de.brickmotioncontrol.server";
    private double pwma;
    private double pwmb;
    private Timer timer = new Timer();
    private TimerTask timerTask = createUpdateTask();

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TextView textView = findViewById(R.id.console);
//        textView.setMovementMethod(new ScrollingMovementMethod());

        setupProgressBar(R.id.seekBarCenter);
        setupProgressBar(R.id.seekBarLeft);
        setupProgressBar(R.id.seekBarRight);

        MotionView motionView = findViewById(R.id.motionView);
        motionView.setMotionInputListener(this);
        timer.scheduleAtFixedRate(timerTask, 1000, 2000);

        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        String serverMame = sharedPref.getString(STORE_KEY_SERVER, "192.168.0.6");
        EditText editText = findViewById(R.id.server);
        editText.setText(serverMame);

        queue = Volley.newRequestQueue(this);

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                SharedPreferences sharedPref = getApplicationContext().
                        getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String serverString = s.toString();
                editor.putString(STORE_KEY_SERVER, serverString);
                editor.apply();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void sendCurrentMotion() {
//        Log.d("MotionControl", "sending pwm " + (int) pwma + " / " + (int) pwmb);

        EditText editText = findViewById(R.id.server);
        String server = editText.getText().toString();
        int pa = (int) pwma;
        int pb = (int) pwmb;
        String url = "http://"+server+":8080/motion?pwma="+pa+"&pwmb="+pb;
        sendRequest(url);


    }


    private void sendRequest(String url) {

        Log.d("Send Request", url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Volley", "Response is: " + response.substring(0, 500));
                        updateConnectionFlag(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "That didn't work!");
                updateConnectionFlag(false);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void updateConnectionFlag(boolean isConnected) {
        CheckBox checkBox = findViewById(R.id.checkConnection);
        checkBox.setChecked(isConnected);
    }

    @Override
    public void motion(float xpercent, float ypercent) {
        if (control == Control.CATERPILLAR) {
            controlCaterpillar(xpercent, ypercent);
        }
        if (control == Control.STEERING) {
            drawSteering(xpercent, ypercent);
        }
    }

    private void controlCaterpillar(double xpercent, double ypercent) {
        double atan = Math.atan2(xpercent, ypercent);
        double length = Math.sqrt((xpercent * xpercent) + (ypercent * ypercent));
        double dy = (Math.sin(atan) * length); //power forward
        double dx = (Math.cos(atan) * length); //left/rightmultiplier
        double left = limit(dx + dy);
        double right = limit(dx - dy);
        left(left);
        right(right);
        setPwm(left, right);
    }

    private void setPwm(double pwma, double pwmb) {
        this.pwma = pwma;
        this.pwmb = pwmb;
    }

    private void drawSteering(double xpercent, double ypercent) {
        left(ypercent);
        right(ypercent);
        center(xpercent);
        setPwm(xpercent, ypercent);
    }

    private void center(double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarCenter);
        seekBar.setProgress(100 + (int) percent);
    }

    private void right(double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarRight);
        seekBar.setProgress(100 + (int) percent);
    }

    private void left(double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarLeft);
        seekBar.setProgress(100 + (int) percent);
    }

    public void setupProgressBar(int id) {
        SeekBar progressRight = findViewById(id);
        progressRight.setMax(200);
        progressRight.setThumb(null);
    }

    private TimerTask createUpdateTask() {
        return new TimerTask() {
            @Override
            public void run() {
                sendCurrentMotion();
            }
        };
    }
}
