package com.example.serverserviceapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.serverserviceapp.Constants.GET_RANDOM_NUMBER
import com.example.serverserviceapp.ui.theme.ServerServiceAppTheme

class MainActivity : ComponentActivity() {
    private var mService: Messenger? = null
    private var mClientService: Messenger? = null
    private var bound by mutableStateOf(false)
    private var randomNumber by mutableStateOf(0)

    inner class ClientServiceHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_RANDOM_NUMBER -> {
                    randomNumber = msg.arg1
                    Log.i("MainActivity", randomNumber.toString())
                }

                else -> super.handleMessage(msg)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServerServiceAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (randomNumber != 0) "Random Number: ${randomNumber}" else "service not Bound",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            enabled = bound == true,
                            onClick = {
                                val msg = Message.obtain(null, GET_RANDOM_NUMBER)
                                msg.replyTo = mClientService
                                try {
                                    mService?.send(msg)
                                } catch (e: RemoteException) {
                                    e.printStackTrace()
                                }
                            }) {
                            Text("Get Number")
                        }
                        Button(
                            enabled = bound == false,
                            onClick = {
                                boundService()
                            }) {
                            Text(text = "Bound Service")
                        }
                        Button(
                            enabled = bound == true,
                            onClick = {
                                unBoundService()
                            }) {
                            Text(text = "UnBound Service")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        boundService()
    }

    private fun boundService() {
        Intent(this, MessengerService::class.java).also {
            bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unBoundService()
    }

    private fun unBoundService() {
        if (bound) {
            unbindService(mConnection)
            bound = false
            randomNumber = 0
        }
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            mService = Messenger(binder)
            mClientService = Messenger(ClientServiceHandler())
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            mService = null
            mClientService = null
            bound = false
        }

    }
}

