package com.example.egen310_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*
import java.util.zip.CheckedOutputStream

class MainActivity : AppCompatActivity() {

    lateinit var bAdapter: BluetoothAdapter
    lateinit var outputStream: OutputStream
    lateinit var inputStream: InputStream
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startButton : Button = findViewById(R.id.start)
        startButton.setOnClickListener { start_bttn() }

        bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter == null || !bAdapter.isEnabled){
            toast("TURN ON BLUETOOTH OR CONNECT TO EGG MAKER")
        }
        else{
            val devices = bAdapter.bondedDevices
            for (device in devices){
                val deviceName = device.name
                val deviceAddr = device
                if (deviceName == "HC-05") {
                    Log.i("BLUETOOTH", "Init")
                    var uuid = device.uuids[0].uuid
                    Log.i("BLUETOOTH", "UUID OF DEVICE: ${uuid.toString()}" )
                    var socket : BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    socket.connect()
                    outputStream = socket.outputStream
                    inputStream = socket.inputStream

                }
            }
        }


    }


    private fun start_bttn(){
        toast("clicked button")
        val status : TextView = findViewById(R.id.status)
        count += 1
        status.text = "Count: " + count
        val paired : TextView = findViewById(R.id.paired_devices)
        paired.text = "Paired Devices"
        val devices = bAdapter.bondedDevices

        for (device in devices){
            val deviceName = device.name
            val deviceAddr = device
            if (deviceName == "HC-05") {
                paired.append("\nDevice: $deviceName, $deviceAddr")
                Log.i("BLUETOOTH", "Attempting to send data")

                if(count % 2 == 0){
                    write("1")
                } else{
                    write("0")
                }

                var bufferedReader: BufferedReader = BufferedReader(InputStreamReader(inputStream))
                Log.i("READ", bufferedReader.readLine())

            }
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun write(msg: String){
        val x : ByteArray = msg.toByteArray()
        outputStream.write(x);
    }
}