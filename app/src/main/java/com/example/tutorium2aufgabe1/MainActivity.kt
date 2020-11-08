package com.example.tutorium2aufgabe1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_LIGHT
import android.hardware.Sensor.TYPE_LINEAR_ACCELERATION
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlin.math.sqrt

class MainActivity() : AppCompatActivity(),SensorEventListener{
    private var tvAccelerometer:ArrayList<TextView> = ArrayList()
    private var tvLight:ArrayList<TextView> = ArrayList()
    private var tvLocation:ArrayList<TextView> = ArrayList()

    private var idAccelerometer:ArrayList<Int> = arrayListOf(R.id.tvAcc1,R.id.tvAcc2,R.id.tvAcc3,R.id.tvMag)
    private var idLight:ArrayList<Int> = arrayListOf(R.id.tvL)
    private var idLocation:ArrayList<Int> = arrayListOf(R.id.tvLat,R.id.tvLong,R.id.tvAlt,R.id.tvAcc)

    private lateinit var sensorManager : SensorManager
    private lateinit var sensorAccelerometer : Sensor
    private lateinit var sensorLight : Sensor

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener :LocationListener
    private var hasNetwork = false
    private var hasGPS = false

    private var accelerometerData : SensorData? = null
    private var lightData : SensorData2? = null

    private lateinit var button1: Button
    private lateinit var button2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initSensors()
        getLocationData()
    }
    private fun initViews(){
        for(i in idAccelerometer){
            tvAccelerometer.add(findViewById(i))
        }
        for(i in idLight){
            tvLight.add(findViewById(i))
        }
        for(i in idLocation){
            tvLocation.add(findViewById(i))
        }
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)

        var clickedOnce = true
        button1.setOnClickListener {
            if(clickedOnce) {
                registerListener()
                clickedOnce = false
            }
            else{
                unregisterListener()
                clickedOnce = true
            }
        }
        button2.setOnClickListener {
            if(clickedOnce){
                registerListener2()
                clickedOnce = false
            }
            else {
                unregisterListener()
                clickedOnce = true
            }
        }
    }
    private fun initSensors(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION)!=null) {
            sensorAccelerometer = sensorManager.getDefaultSensor(TYPE_LINEAR_ACCELERATION)
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null){
           sensorLight = sensorManager.getDefaultSensor(TYPE_LIGHT)
        }
    }

    private fun registerListener(){
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
            sensorManager.registerListener(this,sensorAccelerometer,SensorManager.SENSOR_DELAY_FASTEST)
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null) {
            sensorManager.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }
    private fun registerListener2(){
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
            sensorManager.registerListener(this,sensorAccelerometer,1000000)
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null) {
            sensorManager.registerListener(this, sensorLight, 1000000)
        }
    }

    private fun unregisterListener(){
        sensorManager.unregisterListener(this,sensorAccelerometer)
        sensorManager.unregisterListener(this,sensorLight)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event!!.sensor.type == TYPE_LINEAR_ACCELERATION){
            getAccelerometerData(event)
        }
        else if(event.sensor.type == Sensor.TYPE_LIGHT) {
            getLightData(event)
        }
    }

    private fun getAccelerometerData(e:SensorEvent?){
        if(accelerometerData == null) {
            accelerometerData = SensorData(e!!.values[0], e.values[1], e.values[2], e.timestamp)
        }
        else{
            accelerometerData!!.x1 = e!!.values[0]
            accelerometerData!!.x2 = e.values[1]
            accelerometerData!!.x3 = e.values[2]
        }
        tvAccelerometer[0].text = "X: ${"%.2f".format(accelerometerData!!.x1)}"
        tvAccelerometer[1].text = "Y: ${"%.2f".format(accelerometerData!!.x2)}"
        tvAccelerometer[2].text = "Z: ${"%.2f".format(accelerometerData!!.x3)}"
        val magnitude : Float = sqrt(accelerometerData!!.x1*accelerometerData!!.x1+accelerometerData!!.x2*accelerometerData!!.x2+accelerometerData!!.x3*accelerometerData!!.x3)
        tvAccelerometer[3].text = "Magnitude: ${"%.2f".format(magnitude)}"
    }
    private fun getLightData(e:SensorEvent?){
        if(lightData == null) {
            lightData = SensorData2(e!!.values[0], e.timestamp)
        }
        else{
            lightData!!.x1 = e!!.values[0]
        }
        tvLight[0].text = "Licht: ${"%.2f".format(lightData!!.x1)}"
    }

    private fun getLocationData(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object:LocationListener{
            override fun onLocationChanged(location: Location) {
                tvLocation[0].text = "${location?.latitude}"
                tvLocation[1].text = "${location?.longitude}"
                tvLocation[2].text = "${location?.altitude}"
                tvLocation[3].text = "${location?.accuracy}"
            }
        }
        hasNetwork = locationManager.isProviderEnabled(NETWORK_PROVIDER)
        hasGPS = locationManager.isProviderEnabled(GPS_PROVIDER)
        if(hasNetwork){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0L,0f,locationListener)
        }
        else if(hasGPS){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0f,locationListener)
        }
    }
}