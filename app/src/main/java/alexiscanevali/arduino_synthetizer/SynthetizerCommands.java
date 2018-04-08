package alexiscanevali.arduino_synthetizer;

/* MÉMO DE CE QU'IL RESTE À FAIRE - FONCTIONNALITÉS/INTERFACE GRAPHIQUE DE L'APP
* PERSONAL REMAINING STUFF TO DO - ONLY FOR ALEXIS
*
* Implémenter les capteurs de lumière, proximité
* Implémenter la détection de la force sur le paneau bas droit
* Mettre à jour l'interface graphique avec la valeur des capteurs
*/

/* MÉMO DE CE QU'IL RESTE À FAIRE - GÉNÉRAL / REMAINING STUFF TO DO - GENERAL
* Connexion bluetooth avec l'arduino / Connection with arduino
* Passage d'informations avec l'arduino / Exchanging data between app and arduino
* Effectuer les actions envoyées par le mobile / Executing actions sent by mobile app
* */
import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SynthetizerCommands extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

        /*
    * CUSTOM CLASS VARIABLE BELOW
    * */

    private int osc1_x_coordinate = 25;
    private int osc1_y_coordinate = 25;
    private int osc2_x_coordinate = 25;
    private int osc2_y_coordinate = 50;
    private int xTouchCoordinate = 0;
    private int yTouchCoordinate = 0;
    private int xCurrentCoordinate=0;
    private int yCurrentCoordinate=0;
    private double rotationOsc1 = 0;
    private double rotationOsc2 = 0;
    private int deviceSensorsToDisplay=-1;

    /*
    * END OF CUSTOM CLASS VARIABLE
    * */

    /*
    * VARIABLE TO SEND TO ARDUINO
    * */
    private int pctOsc1 = 0;
    private int pctOsc2 = 0;
    private int pctFilter = 50;
    private int pctLFO = 50;
    private int pctVolume = 50;
    private int pctLightSensor = 0;
    private int pctProximitySensor = 0;
    private int pctForcePressure = 0;

    /*
    * VARIABLES RECEIVED FROM ARDUINO
    * */
    //ADD FIRST VARIABLE HERE
    /*
    * ... TO COMPLETE...
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_synthetizer_commands);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_top_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        /*
        *ON CREATE CUSTOM CODE HERE:
        * */

        ImageView oscilator1 = (ImageView) findViewById(R.id.osc1);
        oscilator1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        xTouchCoordinate = (int)motionEvent.getX();
                        yTouchCoordinate = (int)motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xCurrentCoordinate = (int)motionEvent.getX();
                        yCurrentCoordinate = (int)motionEvent.getY();
                        updateOsc1(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        xCurrentCoordinate = (int)motionEvent.getX();
                        yCurrentCoordinate = (int)motionEvent.getY();
                        updateOsc1(true);
                        break;
                }
                return true;
            }
        });

        ImageView oscilator2 = (ImageView) findViewById(R.id.osc2);
        oscilator2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        xTouchCoordinate = (int)motionEvent.getX();
                        yTouchCoordinate = (int)motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xCurrentCoordinate = (int)motionEvent.getX();
                        yCurrentCoordinate = (int)motionEvent.getY();
                        updateOsc2(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        xCurrentCoordinate = (int)motionEvent.getX();
                        yCurrentCoordinate = (int)motionEvent.getY();
                        updateOsc2(true);
                        break;
                }
                return true;
            }
        });

        SeekBar sb_filter = (SeekBar) findViewById(R.id.sbar_filter);
        sb_filter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pctFilter = i;
                updateSeekBarUI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBarUI();
            }
        });

        SeekBar sb_lfo = (SeekBar) findViewById(R.id.sbar_lfo);
        sb_lfo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pctLFO = i;
                updateSeekBarUI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBarUI();
            }
        });

        SeekBar sb_volume = (SeekBar) findViewById(R.id.sbar_volume);
        sb_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pctVolume = i;
                updateSeekBarUI();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBarUI();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /*
    * CUSTOM METHODS BELOW
    * */

    public void onPianoKeyClick(View view){
        int id = view.getId();


        /*C key triggered*/
        if((id==R.id.c_low_key)||(id==R.id.c_low_right)){
            Toast.makeText(this, "C Low", Toast.LENGTH_SHORT).show();
        }
        /*C# key triggered*/
        else if((id==R.id.c_sharp_left)||(id==R.id.c_sharp_right)){
            Toast.makeText(this, "C# Low", Toast.LENGTH_SHORT).show();
        }
        /*D key triggered*/
        else if((id==R.id.d_key_left)||(id==R.id.d_key)||(id==R.id.d_key_right)){
            Toast.makeText(this, "D", Toast.LENGTH_SHORT).show();
        }
        /*D# key triggered*/
        else if((id==R.id.d_sharp_left)||(id==R.id.d_sharp_right)){
            Toast.makeText(this, "D#", Toast.LENGTH_SHORT).show();
        }
        /*E key triggered*/
        else if((id==R.id.e_key_left)||(id==R.id.e_key)){
            Toast.makeText(this, "E", Toast.LENGTH_SHORT).show();
        }
        /*F key triggered*/
        else if((id==R.id.f_key_right)||(id==R.id.f_key)){
            Toast.makeText(this, "F", Toast.LENGTH_SHORT).show();
        }
        /*F key triggered*/
        else if((id==R.id.f_sharp_left)||(id==R.id.f_sharp_right)){
            Toast.makeText(this, "F#", Toast.LENGTH_SHORT).show();
        }
        /*G key triggered*/
        else if((id==R.id.g_key_left)||(id==R.id.g_key)||(id==R.id.g_key_right)){
            Toast.makeText(this, "G", Toast.LENGTH_SHORT).show();
        }
        /*G# key triggered*/
        else if((id==R.id.g_sharp_left)||(id==R.id.g_sharp_right)){
            Toast.makeText(this, "G#", Toast.LENGTH_SHORT).show();
        }
        /*A key triggered*/
        else if((id==R.id.a_key_left)||(id==R.id.a_key)||(id==R.id.a_key_right)){
            Toast.makeText(this, "A", Toast.LENGTH_SHORT).show();
        }
        /*A# key triggered*/
        else if((id==R.id.a_sharp_left)||(id==R.id.a_sharp_right)){
            Toast.makeText(this, "A#", Toast.LENGTH_SHORT).show();
        }
        /*B key triggered*/
        else if((id==R.id.b_key_left)||(id==R.id.b_key)){
            Toast.makeText(this, "B", Toast.LENGTH_SHORT).show();
        }
        /*High C key triggered*/
        else if((id==R.id.c_high_key)||(id==R.id.c_high_key_right)){
            Toast.makeText(this, "C High", Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.c_high_sharp){
            Toast.makeText(this, "C# High", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unknown key", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateOsc1(boolean isReleased){
        TextView txt_pct_osc1 = (TextView) findViewById(R.id.txt_pct_osc1);
        /*TextView txt_y = (TextView) findViewById(R.id.txt_y_coordinate);
        TextView txt_move_x = (TextView) findViewById(R.id.txt_x_move_coordinate);
        TextView txt_move_y = (TextView) findViewById(R.id.txt_y_move_coordinate);
        TextView txt_angle = (TextView) findViewById(R.id.txt_angle);*/
        ImageView buton_osc1 = (ImageView) findViewById(R.id.osc1);



        int xAB = (xTouchCoordinate-osc1_x_coordinate);
        int xAC = (xCurrentCoordinate-osc1_x_coordinate);
        int yAB = (yTouchCoordinate-osc1_y_coordinate);
        int yAC = (yCurrentCoordinate-osc1_y_coordinate);
        int abac = (xAB*xAC)+(yAB*yAC);
        double scalarAB = Math.sqrt(((Math.pow(xAB,2.0))+(Math.pow(yAB,2.0))));
        double scalarAC = Math.sqrt(((Math.pow(xAC,2.0))+(Math.pow(yAC,2.0))));
        double angle = Math.toDegrees(Math.acos(abac/(scalarAB*scalarAC)));



        /*txt_x.setText(Integer.toString(xTouchCoordinate)+" "+Integer.toString(xAB)+" "+Double.toString(scalarAB));
        txt_y.setText(Integer.toString(yTouchCoordinate)+" "+Integer.toString(yAB)+" "+Double.toString(scalarAC));
        txt_move_x.setText(Integer.toString(xCurrentCoordinate)+" "+Integer.toString(xAC));
        txt_move_y.setText(Integer.toString(yCurrentCoordinate)+" "+Integer.toString(yAC));
        txt_angle.setText(Double.toString(angle)+"°"+Double.toString(rotationOsc1));*/

        Matrix matrix = new Matrix();
        buton_osc1.setScaleType(ImageView.ScaleType.MATRIX);   //required
        matrix.postRotate((float) 0.0, (buton_osc1.getWidth()/2), (buton_osc1.getHeight()/2));
        matrix.postRotate((float) (angle+rotationOsc1), (buton_osc1.getWidth()/2), (buton_osc1.getHeight()/2));
        buton_osc1.setImageMatrix(matrix);

        pctOsc1 = (int) Math.round((((angle+rotationOsc1)%360)*100)/359.99);

        txt_pct_osc1.setText(Integer.toString(pctOsc1)+" %");

        if(isReleased){
            rotationOsc1 = ((rotationOsc1+angle)%360);
        }
    }

    public void updateOsc2(boolean isReleased){
        TextView txt_pct_osc2 = (TextView) findViewById(R.id.txt_pct_osc2);
        ImageView buton_osc2 = (ImageView) findViewById(R.id.osc2);

        int xAB = (xTouchCoordinate-osc2_x_coordinate);
        int xAC = (xCurrentCoordinate-osc2_x_coordinate);
        int yAB = (yTouchCoordinate-osc2_y_coordinate);
        int yAC = (yCurrentCoordinate-osc2_y_coordinate);
        int abac = (xAB*xAC)+(yAB*yAC);
        double scalarAB = Math.sqrt(((Math.pow(xAB,2.0))+(Math.pow(yAB,2.0))));
        double scalarAC = Math.sqrt(((Math.pow(xAC,2.0))+(Math.pow(yAC,2.0))));
        double angle = Math.toDegrees(Math.acos(abac/(scalarAB*scalarAC)));

        Matrix matrix = new Matrix();
        buton_osc2.setScaleType(ImageView.ScaleType.MATRIX);   //required
        matrix.postRotate((float) 0.0, (buton_osc2.getWidth()/2), (buton_osc2.getHeight()/2));
        matrix.postRotate((float) (angle+rotationOsc2), (buton_osc2.getWidth()/2), (buton_osc2.getHeight()/2));
        buton_osc2.setImageMatrix(matrix);

        pctOsc2 = (int) Math.round((((angle+rotationOsc2)%360)*100)/359.99);

        txt_pct_osc2.setText(Integer.toString(pctOsc2)+" %");

        if(isReleased){
            rotationOsc2 = ((rotationOsc2+angle)%360);
        }
    }

    public void updateSeekBarUI(){
        TextView txt_sb_filter = (TextView) findViewById(R.id.txt_filter);
        TextView txt_sb_lfo = (TextView) findViewById(R.id.txt_lfo);
        TextView txt_sb_volume = (TextView) findViewById(R.id.txt_volume);

        txt_sb_filter.setText("Filter: "+Integer.toString(pctFilter)+"%");
        txt_sb_lfo.setText("LFO: "+Integer.toString(pctLFO)+"%");
        txt_sb_volume.setText("Volume: "+Integer.toString(pctVolume)+"%");
    }

    public void updateSensorDisplayUI(int mobileOrArduino){
        TextView txt_sensor_1 = (TextView) findViewById(R.id.txt_sensor_display_1);
        TextView txt_sensor_2 = (TextView) findViewById(R.id.txt_sensor_display_2);
        TextView txt_sensor_3 = (TextView) findViewById(R.id.txt_sensor_display_3);
        TextView txt_sensor_4 = (TextView) findViewById(R.id.txt_sensor_display_4);
        TextView txt_sensor_5 = (TextView) findViewById(R.id.txt_sensor_display_5);

        switch (mobileOrArduino){
            case 0://Mobile
                txt_sensor_1.setText("Mobile sensors");
                txt_sensor_2.setText("Light sensor: "+Integer.toString(pctLightSensor)+"%");
                txt_sensor_3.setText("Proximity sensor: "+Integer.toString(pctProximitySensor)+"%");
                txt_sensor_4.setText("Force pressure: "+Integer.toString(pctForcePressure)+"%");
                txt_sensor_5.setText("");
                break;
            case 1://Arduino
                txt_sensor_1.setText("Arduino sensors");
                txt_sensor_2.setText("");
                txt_sensor_3.setText("");
                txt_sensor_4.setText("");
                txt_sensor_5.setText("");
                break;
            default:
                txt_sensor_1.setText("Select device sensors");
                txt_sensor_2.setText("");
                txt_sensor_3.setText("");
                txt_sensor_4.setText("");
                txt_sensor_5.setText("");
                break;
        }
    }

    public void switchSensorDisplayMode(View view){
        int id = view.getId();

        if(id==R.id.btn_mobile_values){
            deviceSensorsToDisplay = 0;
        } else if (id==R.id.btn_arduino_values){
            deviceSensorsToDisplay = 1;
        } else{
            deviceSensorsToDisplay = -1;
        }
        updateSensorDisplayUI(deviceSensorsToDisplay);
    }

    /*
    * END OF CUSTOM METHODS
    * */
}
