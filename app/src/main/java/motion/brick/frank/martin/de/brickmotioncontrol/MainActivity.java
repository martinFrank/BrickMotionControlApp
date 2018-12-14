package motion.brick.frank.martin.de.brickmotioncontrol;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static motion.brick.frank.martin.de.brickmotioncontrol.MathUtil.limit;

public class MainActivity extends AppCompatActivity implements MotionInputListener {

    private Control control = Control.CATERPILLAR;
    private static final String STORE_NAME = "motion.brick.frank.martin.de.brickmotioncontrol.store";
    private static final String STORE_KEY_SERVER = "motion.brick.frank.martin.de.brickmotioncontrol.server";
    private static final String SERVER_URL_TEMPLATE = "http://%s:8080/motion?pwma=%d&pwmb=%d";
    private static final int MINIMAL_SEND_DEALY_IN_MILLIS = 50;
    private String serverName;
    private double pwma;
    private double pwmb;
    private double pwma_before;
    private double pwmb_before;
    private double lastTimeStamp;
    private RequestQueue queue;
    private static final DefaultRetryPolicy DEFAULT_RETRY_POLICY = new DefaultRetryPolicy(50,0,1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView console = findViewById(R.id.console);
        console.setMovementMethod(new ScrollingMovementMethod());
        setupProgressBar(R.id.seekBarCenter);
        setupProgressBar(R.id.seekBarLeft);
        setupProgressBar(R.id.seekBarRight);
        MotionView motionView = findViewById(R.id.motionView);
        motionView.setMotionInputListener(this);
        serverName = restoreServerNameFromSharedPreferences("192.168.0.6");
        EditText editText = findViewById(R.id.server);
        editText.setText(serverName);
        queue = Volley.newRequestQueue(this);
        setServerTextChangedListener(editText);
    }

    private void sendMotion() {
        if (hasMinimalDelayPassed() || isStopping()){
            if (hasMotionChanged()){
                int pa = (int) pwma;
                int pb = (int) pwmb;
                String url = String.format(SERVER_URL_TEMPLATE, serverName, pa, pb);
//                url = "https://dzone.com/articles/java-string-format-examples";
                sendMotionRequest(url);

                pwma_before = pwma;
                pwmb_before = pwmb;
            }
            lastTimeStamp = System.currentTimeMillis();
        }
    }
    private boolean hasMinimalDelayPassed() {
        return System.currentTimeMillis() - lastTimeStamp > MINIMAL_SEND_DEALY_IN_MILLIS;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        //stop things if they are still running
        pwma = 0;
        pwmb = 0;
        sendMotion();
        super.onPause();
    }

    private boolean hasMotionChanged() {
        return pwma != pwma_before || pwmb != pwmb_before;
    }

    private int c = 0;
    private void sendMotionRequest(final String url) {
        c = c + 1;
        Log.d("Send Motion Request", ""+c+" - "+url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        writeToConsole(String.format("%d success %n%s",c,response));
                        updateConnectionFlag(true);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                writeToConsole(String.format("%d failed %n%s",c,url));
                updateConnectionFlag(false);
            }
        });
        stringRequest.setRetryPolicy(DEFAULT_RETRY_POLICY);

        queue.add(stringRequest);
    }

    private void writeToConsole(String s) {
        TextView console = findViewById(R.id.console);
        console.setText(s);
    }

    private void updateConnectionFlag(boolean isConnected) {
        CheckBox checkBox = findViewById(R.id.checkConnection);
        checkBox.setChecked(isConnected);
    }

    @Override
    public void motion(float xpercent, float ypercent) {
        if (control == Control.CATERPILLAR) {
            setMotionForCaterpillar(xpercent, ypercent);
        }
        if (control == Control.WHEEL_STEERING) {
            setMotionFormWheelSteering(xpercent, ypercent);
        }
        sendMotion();
    }

    private void setMotionForCaterpillar(double xpercent, double ypercent) {
        double atan = Math.atan2(xpercent, ypercent);
        double length = Math.sqrt((xpercent * xpercent) + (ypercent * ypercent));
        double dy = (Math.sin(atan) * length); //power forward
        double dx = (Math.cos(atan) * length); //left/right
        double left = limit(dx + dy);
        double right = limit(dx - dy);
        setSeekBarLeft(left);
        setSeekBarRight(right);
        this.pwma = left;
        this.pwmb = right;
    }

    private void setMotionFormWheelSteering(double xpercent, double ypercent) {
        setSeekBarLeft(ypercent);
        setSeekBarRight(ypercent);
        setSeekBarCenter(xpercent);
        this.pwma = limit(xpercent);
        this.pwmb = limit(ypercent);
    }

    private void setSeekBarCenter(double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarCenter);
        seekBar.setProgress(100 + (int) percent);
    }

    private void setSeekBarRight(double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarRight);
        seekBar.setProgress(100 + (int) percent);
    }

    private void setSeekBarLeft (double percent) {
        SeekBar seekBar = findViewById(R.id.seekBarLeft);
        seekBar.setProgress(100 + (int) percent);
    }

    public void setupProgressBar(int id) {
        SeekBar progressRight = findViewById(id);
        progressRight.setMax(200);
        progressRight.setThumb(null);
    }

    public String restoreServerNameFromSharedPreferences(String defaultValue) {
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(STORE_KEY_SERVER, defaultValue);
    }

    public void storeServerNameIntoSharedPreferences(String newValue) {
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(STORE_KEY_SERVER, serverName);
        editor.apply();
    }

    public void setServerTextChangedListener(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                serverName = s.toString();
                storeServerNameIntoSharedPreferences(serverName);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    public boolean isStopping() {
        return pwma==0&&pwmb==0;
    }
}
