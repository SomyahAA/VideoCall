package com.example.tryvideocall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tryvideocall.agora.media.RtcTokenBuilder
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas


private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

class MainActivity : AppCompatActivity() {

    private val APP_ID = "abcbccc386c6473c9455f897b2bd2555"

    private val CHANNEL = "a"

    private var mRtcEngine: RtcEngine? = null

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
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
            && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
        ) {
            initializeAndJoinChannel()
        }
    }

    private fun startVideoCall(): String {

        val appCertificate = "b6ae56a99aa440f3a71291f046254aca"
        val uid = 546755
        val expirationTimeInSeconds = 3600

        val token = RtcTokenBuilder()
        val timestamp: Int = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()

        val result: String = token.buildTokenWithUid(
            APP_ID, appCertificate,
            CHANNEL, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp
        )
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

        mRtcEngine!!.joinChannel(
            startVideoCall(),
            CHANNEL,
            "",
            546755 /*this is the uid it must match the id used to generate the token */
        )
    }

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