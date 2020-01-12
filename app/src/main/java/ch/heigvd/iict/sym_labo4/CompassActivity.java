package ch.heigvd.iict.sym_labo4;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.hardware.SensorManager;

import ch.heigvd.iict.sym_labo4.gl.OpenGLRenderer;

public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;

    private Sensor mMagnetic;


    //opengl
    private OpenGLRenderer opglr = null;
    private GLSurfaceView m3DView = null;



    float[] gravity = new float[3];//gravity
    float[] geomagnetic  = new float[3];//geomagnetic
    float[] matrixR = new float[16];//R

    boolean sensorReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we need fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // we initiate the view
        setContentView(R.layout.activity_compass);

        //we create the renderer
        this.opglr = new OpenGLRenderer(getApplicationContext());

        // link to GUI
        this.m3DView = findViewById(R.id.compass_opengl);

        //init opengl surface view
        this.m3DView.setRenderer(this.opglr);


        this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        this.mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


    }

    /* TODO
        your activity need to register to accelerometer and magnetometer sensors' updates
        then you may want to call
        this.opglr.swapRotMatrix()
        with the 4x4 rotation matrix, everytime a new matrix is computed
        more information on rotation matrix can be found on-line:
        https://developer.android.com/reference/android/hardware/SensorManager.html#getRotationMatrix(float[],%20float[],%20float[],%20float[])
    */

    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;


        switch (mySensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                sensorReady = true;
                break;
            default:
                break;
        }

        if (geomagnetic != null && gravity != null && sensorReady) {
            sensorReady = false;

            SensorManager.getRotationMatrix(matrixR, null, gravity, geomagnetic);

            matrixR = this.opglr.swapRotMatrix(matrixR);
        }
        
    }


}
