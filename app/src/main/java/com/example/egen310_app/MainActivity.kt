package com.example.egen310_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*
import java.util.zip.CheckedOutputStream
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var bAdapter: BluetoothAdapter
    lateinit var outputStream: OutputStream
    lateinit var inputStream: InputStream
    var count = 0
    val list: ArrayList<String> = ArrayList()
    lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startButton : ImageButton = findViewById(R.id.start)
        startButton.setOnClickListener { start_bttn() }
        val cooking : ImageButton = findViewById(R.id.cooking)
        cooking.setOnClickListener { cook_bttn() }



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

                    handler = object: Handler(Looper.getMainLooper()){
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            handleMsg(msg)
                        }
                    }


                    thread {
                        val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(inputStream))
                        while(true){
                            var data = bufferedReader.readLine()
                            Log.i("READ", data)
                            var msg: Message = Message()
                            msg.what = 10
                            msg.obj = data
                            handler.sendMessage(msg)
                        }
                    }

                }
            }
        }


    }


    lateinit var countDownTimer: CountDownTimer
    private fun handleMsg(msg: Message){
        val content = msg.obj as String


        if (content.contains("STATUS:")){
            val update = content.split(':')[1].toFloat()
            var progressBar = findViewById<CircleProgressBar>(R.id.custom_progressBar)
            Log.i("STATUS_UPDATE", update.toString())
            progressBar.setProgress(update * 100)
        }

    }

    private fun start_bttn(){
        toast("clicked button")
        val startButton : ImageButton = findViewById(R.id.start)
//        startButton.isEnabled = false
        startButton.visibility = View.GONE
        val cooking : ImageButton = findViewById(R.id.cooking)
//        cooking.isEnabled = true
        cooking.visibility = View.VISIBLE
        val status : TextView = findViewById(R.id.status)
        val paired : TextView = findViewById(R.id.paired_devices)
        paired.text = "Paired Devices"
        val devices = bAdapter.bondedDevices

        for (device in devices){
            val deviceName = device.name
            val deviceAddr = device
            if (deviceName == "HC-05") {
                paired.append("\nDevice: $deviceName, $deviceAddr")
                Log.i("BLUETOOTH", "Attempting to send data")

                write("1")
            }
        }
    }

    private fun cook_bttn(){
        val startButton : ImageButton = findViewById(R.id.start)
        startButton.visibility = View.VISIBLE
        val cooking : ImageButton = findViewById(R.id.cooking)
        cooking.visibility = View.GONE
        toast("Egg is cooking!")
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun write(msg: String){
        val x : ByteArray = msg.toByteArray()
        outputStream.write(x);
    }
}