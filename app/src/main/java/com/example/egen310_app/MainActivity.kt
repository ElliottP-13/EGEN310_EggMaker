package com.example.egen310_app

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    lateinit var bAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startButton : Button = findViewById(R.id.start)
        startButton.setOnClickListener { start_bttn() }

        bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter == null || !bAdapter.isEnabled){
            toast("TURN ON BLUETOOTH OR CONNECT TO EGG MAKER")
        }


    }

    private fun start_bttn(){
        toast("clicked button")
        val status : TextView = findViewById(R.id.status)
        val ran = (1..100).random()
        status.text = "Number: " + ran
        val paired : TextView = findViewById(R.id.paired_devices)
        paired.text = "Paired Devices"
        val devices = bAdapter.bondedDevices
        for (device in devices){
            val deviceName = device.name
            val deviceAddr = device
            paired.append("\nDevice: $deviceName, $deviceAddr")
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}