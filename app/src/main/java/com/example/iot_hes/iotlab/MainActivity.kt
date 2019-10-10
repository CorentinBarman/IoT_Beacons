package com.example.iot_hes.iotlab

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.estimote.coresdk.observation.region.Region
import com.estimote.coresdk.observation.region.beacon.BeaconRegion
import com.estimote.coresdk.recognition.packets.Beacon
import com.estimote.coresdk.service.BeaconManager
import java.util.*
import java.nio.file.Files.size



class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSION_REQUEST_COARSE_LOCATION = 2
    }

    lateinit var PositionText: TextView
    lateinit var Percentage: EditText
    lateinit var IncrButton: Button
    lateinit var DecrButton: Button
    lateinit var LightButton: Button
    lateinit var StoreButton: Button
    lateinit var RadiatorButton: Button

    lateinit var beaconManager: BeaconManager


    // In the "OnCreate" function below:
    // - TextView, EditText and Button elements are linked to their graphical parts (Done for you ;) )
    // - "OnClick" functions for Increment and Decrement Buttons are implemented (Done for you ;) )
    //
    // TODO List:
    // - Use the Estimote SDK to detect the closest Beacon and figure out the current Room
    //     --> See Estimote documentation:  https://github.com/Estimote/Android-SDK
    // - Set the PositionText with the Room name
    // - Implement the "OnClick" functions for LightButton, StoreButton and RadiatorButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PositionText = findViewById(R.id.PositionText)
        Percentage = findViewById(R.id.Percentage)
        IncrButton = findViewById(R.id.IncrButton)
        DecrButton = findViewById(R.id.DecrButton)
        LightButton = findViewById(R.id.LightButton)
        StoreButton = findViewById(R.id.StoreButton)
        RadiatorButton = findViewById(R.id.RadiatorButton)

        // Only accept input values between 0 and 100
        Percentage.filters = arrayOf<InputFilter>(InputFilterMinMax("0", "100"))

        IncrButton.setOnClickListener {
            var number = Integer.parseInt(Percentage.text.toString())
            if (number < 100) {
                number++
                Log.d("IoTLab-Inc", String.format("%d", number))
                Percentage.setText(String.format("%d", number))
            }
        }

        DecrButton.setOnClickListener {
            var number = Integer.parseInt(Percentage.text.toString())
            if (number > 0) {
                number--
                Log.d("IoTLab-Dec", String.format("%d", number))
                Percentage.setText(String.format("%d", number))
            }
        }

        val allRegions = BeaconRegion("Mint beacon",
                UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d".toUpperCase(Locale.ROOT)), 20397, null)

        beaconManager = BeaconManager(this)

        beaconManager.setMonitoringListener(object : BeaconManager.BeaconMonitoringListener{
            override fun onEnteredRegion(beaconRegion: BeaconRegion?, beacons: MutableList<Beacon>?) {
                if(beacons?.size != 0)
                {
                    for(i in 1..beacons!!.size) {
                        val beacon = beacons[i-1]
                        Log.d(TAG, "Beacon entered region, minor: " + beacon.minor + ", RSSI: " + beacon.rssi)
                    }
                }
            }

            override fun onExitedRegion(beaconRegion: BeaconRegion?) {
                Log.d(TAG, "Beacon exited region")
            }
        })

        beaconManager.setRangingListener(BeaconManager.BeaconRangingListener { beaconRegion, beacons ->
            var closestBeacon : Beacon? = null
            val n = 2
            var distance : Int = Int.MAX_VALUE
            for(beacon in beacons) {
                Log.d(TAG, "Beacon minor: " + beacon.minor + ", RSSI: " + beacon.rssi)

                val newDistance = (beacon.measuredPower - beacon.rssi)
                if(closestBeacon != null && distance > newDistance) {
                    closestBeacon = beacon
                    distance = newDistance
                }

                if(closestBeacon == null) {
                    closestBeacon = beacon
                }
            }

            PositionText.text = "Minor ID " + closestBeacon?.minor +  "\n(Major ID " + closestBeacon?.major + ")"
        })

        beaconManager.connect {
            Log.d(TAG, "Service Ready for Beacon (Ready to scan)")
            beaconManager.startMonitoring(allRegions)
            beaconManager.startRanging(allRegions)
        }


        LightButton.setOnClickListener {
            // TODO Send HTTP Request to command light
            Log.d("IoTLab", Percentage.text.toString())
        }



        StoreButton.setOnClickListener {
            // TODO Send HTTP Request to command store
            Log.d("IoTLab", Percentage.text.toString())
        }



        RadiatorButton.setOnClickListener {
            // TODO Send HTTP Request to command radiator
            Log.d("IoTLab", Percentage.text.toString())
        }


    }


    // You will be using "OnResume" and "OnPause" functions to resume and pause Beacons ranging (scanning)
    // See estimote documentation:  https://developer.estimote.com/android/tutorial/part-3-ranging-beacons/
    override fun onResume() {
        super.onResume()

        askRequiredBlePermissions()
    }


    override fun onPause() {
        super.onPause()

    }

    private fun askRequiredBlePermissions(): Boolean {
        var result = true

        // Location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.location_request_title))
                builder.setMessage(getString(R.string.location_access_content))
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener { requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_COARSE_LOCATION) }
                builder.show()
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION)
            }

            result = false
        }

        // Bluetooth activation
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            BluetoothAdapter.getDefaultAdapter().enable()
        }

        // Location activation
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
                AlertDialog.Builder(this)
                        .setMessage("Please activate location to be able to scan for bluetooth devices")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                        .show()
        }

        return result
    }

}


// This class is used to filter input, you won't be using it.

internal class InputFilterMinMax : InputFilter {
    private var min: Int = 0
    private var max: Int = 0

    constructor(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    constructor(min: String, max: String) {
        this.min = Integer.parseInt(min)
        this.max = Integer.parseInt(max)
    }

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = Integer.parseInt(dest.toString() + source.toString())
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }

        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c >= a && c <= b else c >= b && c <= a
    }
}