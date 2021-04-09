package com.example.egen310_app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.*
import java.util.zip.CheckedOutputStream
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var bAdapter: BluetoothAdapter
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream
    lateinit var handler: Handler
    private var cooking_bool = false

    lateinit var mNotificationManager: NotificationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startButton : ImageButton = findViewById(R.id.start)
        startButton.setOnClickListener { start_bttn() }
        val cooking : ImageButton = findViewById(R.id.cooking)
        cooking.setOnClickListener { cook_bttn() }

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var notificationChannel = NotificationChannel("egg_ready", "Egg Completion", NotificationManager.IMPORTANCE_HIGH)

        notificationChannel.description = "Notifications when the eggs are ready"
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
//        notificationChannel.vibrationPattern = longArrayOf(2000, 1000)
        notificationChannel.enableVibration(true)

        mNotificationManager.createNotificationChannel(notificationChannel)


        bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter == null || !bAdapter.isEnabled){
            toast("TURN ON BLUETOOTH OR CONNECT TO EGG MAKER")
        }
        else{
            val devices = bAdapter.bondedDevices
            for (device in devices){
                val deviceName = device.name
                val deviceAddr = device
                if (deviceName == "HC-05") { // Connect to egg maker
                    Log.i("BLUETOOTH", "Init")
                    var uuid = device.uuids[0].uuid
                    Log.i("BLUETOOTH", "UUID OF DEVICE: ${uuid.toString()}" )
                    var socket : BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

                    // if we aren't connected, then connect to socket
                    Log.i("BLUETOOTH", "Starting Connection")
                    socket.connect()
                    outputStream = socket.outputStream
                    inputStream = socket.inputStream

                    handler = object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            handleMsg(msg)
                        }
                    }


                    thread { // thread to constantly check for new messages
                        val bufferedReader: BufferedReader =
                            BufferedReader(InputStreamReader(inputStream))
                        while (true) {
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

//        showNotification()


    }

    /**
     * Handles the messages from the bluetooth device.
     */
    private fun handleMsg(msg: Message){
        val content = msg.obj as String


        if (content.contains("STATUS:")){
            val update = content.split(':')[1].toFloat()
            var progressBar = findViewById<CircleProgressBar>(R.id.custom_progressBar)
            Log.i("STATUS_UPDATE", update.toString())
            progressBar.setProgress(update * 100)
        }
        else if (content.contains("DONE")){
            cooking_bool = false

            val status : TextView = findViewById(R.id.status)
            status.text = getString(R.string.ready)

            showNotification()
        }
    }

    /**
     * What happens when start button (egg) is clicked
     */
    private fun start_bttn(){
        toast("clicked button")
        val startButton : ImageButton = findViewById(R.id.start)
        startButton.isEnabled = false
        startButton.visibility = View.INVISIBLE
        val cooking : ImageButton = findViewById(R.id.cooking)
        cooking.isEnabled = true
        cooking.visibility = View.VISIBLE
        val status : TextView = findViewById(R.id.status)
        val devices = bAdapter.bondedDevices

        for (device in devices){
            val deviceName = device.name
            val deviceAddr = device
            if (deviceName == "HC-05") {
                Log.i("BLUETOOTH", "Attempting to send data")
                write("1")
                cooking_bool = true
                status.text = getString(R.string.cooking)
            }
        }
    }

    /**
     * what happens when cook button (frying pan) is clicked
     */
    private fun cook_bttn(){
        if (cooking_bool){
            toast("Egg is cooking! Please wait")
        }
        else {
            val startButton: ImageButton = findViewById(R.id.start)
            startButton.visibility = View.VISIBLE
            startButton.isEnabled = true
            val cooking: ImageButton = findViewById(R.id.cooking)
            cooking.visibility = View.INVISIBLE
            cooking.isEnabled = false
            val progressBar: CircleProgressBar = findViewById(R.id.custom_progressBar)
            progressBar.setProgress(0.toFloat())

            val status : TextView = findViewById(R.id.status)
            status.text = getString(R.string.status)

        }
    }

    /**
     * Shows a notification to the user when eggs are done!
     */
    fun showNotification() {
        val mBuilder = NotificationCompat.Builder(applicationContext, "egg_ready")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Eggs are ready") // title for notification
                .setContentText("Your eggs are done cooking! Go pick them up and enjoy!")// message for notification
                .setAutoCancel(true) // clear notification after click
//        val intent = Intent(applicationContext, MainActivity::class.java)
//        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_NO_CREATE)
//        mBuilder.setContentIntent(pi)
        mNotificationManager.notify(0, mBuilder.build())
    }

    /**
     * Makes a toast message (word bubble on screen)
     */
    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Writes to bluetooth output
     */
    private fun write(msg: String){
        val x : ByteArray = msg.toByteArray()
        outputStream.write(x);
    }
}