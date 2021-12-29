package com.example.tryvideocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import com.example.tryvideocall.agora.media.RtcTokenBuilder
import io.agora.agorauikit.manager.AgoraRTC


private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

class MainActivity : AppCompatActivity() {

    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "abcbccc386c6473c9455f897b2bd2555"
    // Fill the channel name.
    private val CHANNEL = "a"
    // Fill the temp token generated on Agora Console.



    //private val TOKEN = "006abcbccc386c6473c9455f897b2bd2555IAB2hHnD0+QHANO7ARbdRFcmcs4ghbI06a7YtsgfvIlQvkO+t+gAAAAAEACu7KwLRYfNYQEAAQBFh81h"
    private val TOKEN = "006abcbccc386c6473c9455f897b2bd2555IABnnH3ooSuEzOhCaIgBkREhD6R9t533K+Xs+D8qG2uPJEO+t+hPAoleIgB3oBqax5DNYQQAAQBXTcxhAgBXTcxhAwBXTcxhBABXTcxh"
    private var mRtcEngine: RtcEngine ?= null

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
            && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initializeAndJoinChannel()
        }
    }


    private fun startVideoCall( ): String {



        val   appCertificate = "b6ae56a99aa440f3a71291f046254aca";


        val  uid = 2082341273;
        val  expirationTimeInSeconds = 3600;

        val token : RtcTokenBuilder =  RtcTokenBuilder()
        val  timestamp :Int = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()


        val result: String = token.buildTokenWithUid(APP_ID, appCertificate,
            CHANNEL, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);

        Log.d("TAG", "startVideoCall999: $result")

        return result
    }


    private fun initializeAndJoinChannel() {
        try {


            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)

           

            Log.d("TAG", "initializeAndJoinChannel: ")
        } catch (e: Exception) {
            Log.e("TAG", "initializeAndJoinChannel: ", e)
        }
        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine!!.enableVideo()

        val localContainer = findViewById<FrameLayout>(R.id.local_video_view_container)
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val localFrame = RtcEngine.CreateRendererView(baseContext)
        localContainer.addView(localFrame)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))
        startVideoCall()
        // Join the channel with a token.
        //mRtcEngine.
        mRtcEngine!!.joinChannel(startVideoCall().replace('/','+'), CHANNEL, "", 0)
    }

    // Kotlin

    private fun setupRemoteVideo(uid: Int) {
        val remoteContainer = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }

}