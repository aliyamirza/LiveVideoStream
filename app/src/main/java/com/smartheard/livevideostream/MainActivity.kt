package com.smartheard.livevideostream

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.smartheard.livevideostream.databinding.ActivityMainBinding
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration


class MainActivity : AppCompatActivity(),View.OnClickListener {
    lateinit var binding: ActivityMainBinding

    private var mEndCall = false
    private var mMuted = false
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null
    private var rtcEngine: RtcEngine? = null

        var cameraEnd = false
    var cameraFlip = false
    var videoFlip = false
    var voiceMute = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

         requestForPermission()
        initRtcEngine()

                binding.btnAudio.setOnClickListener(this)
        binding.btnEndCall.setOnClickListener(this)
        binding.btnFlip.setOnClickListener(this)
        binding.btnVideo.setOnClickListener(this)

//        binding.joinRoom.setOnClickListener {
//            startActivity(Intent(this, VideoCallScreen::class.java))
//        }

//        binding.buttonCall.setOnClickListener {
//            if (mEndCall) {
//                startCall()
//                mEndCall = false
//                binding.buttonCall.setImageResource(R.drawable.ic_end_call)
//                binding.buttonMute.visibility = VISIBLE
//                binding.buttonSwitchCamera.visibility = VISIBLE
//
//            } else {
//                endCall()
//                mEndCall = true
//                binding.buttonCall.setImageResource(R.drawable.ic_baseline_local_phone_24)
//                binding.buttonMute.visibility = INVISIBLE
//                binding.buttonSwitchCamera.visibility = INVISIBLE
//            }
//        }
//
//        binding.buttonSwitchCamera.setOnClickListener {
//            rtcEngine?.switchCamera()
//        }
//
//        binding.buttonMute.setOnClickListener {
//            mMuted = !mMuted
//            rtcEngine?.muteLocalAudioStream(mMuted)
//            val res: Int = if (mMuted) {
//                R.drawable.ic_mute
//            } else {
//                R.drawable.ic_unmute
//            }
//
//            binding.buttonMute.setImageResource(res)
//        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onConnectionLost() {
            super.onConnectionLost()
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Joined Channel Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /*
         * Listen for the onFirstRemoteVideoDecoded callback.
         * This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
         * You can call the setupRemoteVideoView method in this callback to set up the remote video view.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread {
                setupRemoteVideoView(uid)
            }
        }

        /*
        * Listen for the onUserOffline callback.
        * This callback occurs when the remote user leaves the channel or drops offline.
        */
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                onRemoteUserLeft()
            }
        }
    }

    private fun setupSession() {
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine?.enableVideo()
        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun initAndJoinChannel() {

        // This is our usual steps for joining
        // a channel and starting a call.
        initRtcEngine()
        setupVideoConfig()
        setupLocalVideoView()
        joinChannel()
        // setupSession()
    }

    // Initialize the RtcEngine object.
    private fun initRtcEngine() {
        try {

            rtcEngine = RtcEngine.create(baseContext, getString(R.string.app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.d("InitRtcEngine", "initRtcEngine: $e")
        }
    }

    private fun setupLocalVideoView() {


        localView = RtcEngine.CreateRendererView(baseContext)
        localView!!.setZOrderMediaOverlay(true)
        binding.localVideo.addView(localView)

        // Set the local video view.
        rtcEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun setupRemoteVideoView(uid: Int) {


        if (binding.remoteVideoContainer.childCount > 1) {
            return
        }
        remoteView = RtcEngine.CreateRendererView(applicationContext)
        remoteView?.setZOrderMediaOverlay(true)
        binding.remoteVideoContainer.addView(remoteView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun setupVideoConfig() {


        rtcEngine?.enableVideo()

        rtcEngine?.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun joinChannel() {
        // Join a channel with a token.
        rtcEngine?.joinChannel(null, "AliyaChannel", "Extra Optional Data", 0)
    }

    private fun startCall() {
        setupLocalVideoView()
        joinChannel()
        initAndJoinChannel()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        leaveChannel()
    }

    private fun removeLocalVideo() {
        if (localView != null) {
            binding.localVideo.removeView(localView)
        }
        localView = null
    }

    private fun removeRemoteVideo() {
         binding.remoteVideoContainer.removeView(remoteView)
        if (remoteView != null) {
            binding.remoteVideoContainer.removeView(remoteView)
        }
        remoteView = null
    }

    private fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        removeRemoteVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mEndCall) {
            leaveChannel()
        }
        RtcEngine.destroy()
    }


    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasGallaryPermission() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasChangeAudioSettings() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ) == PackageManager.PERMISSION_GRANTED


    private fun hasRecordAudio() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasBluetooth() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.BLUETOOTH
    ) == PackageManager.PERMISSION_GRANTED

    private fun hasNetworkState() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_NETWORK_STATE
    ) == PackageManager.PERMISSION_GRANTED


    private fun requestForPermission() {

        val permissionList = mutableListOf<String>()

        if (!hasCameraPermission()) {
            permissionList.add(Manifest.permission.CAMERA)
        }

        if (!hasChangeAudioSettings()) {
            permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
        }

        if (!hasGallaryPermission()) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!hasNetworkState()) {
            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE)
        }
        if (!hasBluetooth()) {
            permissionList.add(Manifest.permission.BLUETOOTH)
        }
        if (!hasRecordAudio()) {
            permissionList.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionList.isNotEmpty()) {

            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                     Here we continue only if all permissions are granted.
                    // The permissions can also be granted in the system settings manually.
                    initAndJoinChannel()

                } else {
                    Toast.makeText(this, "$i Permission Not Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding!!.btnAudio.id -> {
                when (voiceMute) {
                    false -> {
                        binding.btnAudio.setImageResource(R.drawable.ic_baseline_mic_24)
                        voiceMute = true
                        rtcEngine?.enableLocalAudio(true)
                    }
                    true -> {
                        binding.btnAudio.setImageResource(R.drawable.ic_baseline_mic_off_24)
                        voiceMute = false
                        rtcEngine?.enableLocalAudio(false)
                    }
                }
            }
            binding!!.btnEndCall.id -> {
                when (cameraEnd) {
                    false -> {
                        binding.btnEndCall.setImageResource(R.drawable.ic_baseline_call_24)
                        cameraEnd = true
                        startCall()
                    }
                    true -> {
                        binding!!.btnEndCall.setImageResource(R.drawable.ic_baseline_call_end_24)
                        cameraEnd = false
                        endCall()
                    }
                }
            }
            binding!!.btnVideo.id -> {
                when (videoFlip) {
                    false -> {
                        binding.btnVideo.setImageResource(R.drawable.ic_baseline_videocam_24)
                        videoFlip = true
                        rtcEngine?.enableLocalVideo(true)
                    }
                    true -> {
                        binding.btnVideo.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                        videoFlip = false
                        rtcEngine?.enableLocalVideo(false)
                    }

                }
            }
            binding!!.btnFlip.id -> {
                when (cameraFlip) {
                    false -> {
                        binding.btnFlip.setImageResource(R.drawable.ic_baseline_flip_camera_android_24)
                        cameraFlip = true
                        rtcEngine?.switchCamera()
                    }
                    true -> {
                        binding.btnFlip.setImageResource(R.drawable.ic_baseline_flip_camera_android_24)
                        cameraFlip = false
                        rtcEngine?.switchCamera()
                    }

                }
            }
        }

    }

}

//class MainActivity : AppCompatActivity(), View.OnClickListener {
//
//    lateinit var binding: ActivityMainBinding
//    var cameraEnd = false
//    var cameraFlip = false
//    var videoFlip = false
//    var voiceMute = false
//    var localVideo: SurfaceView? = null
//    var remoteVideo: SurfaceView? = null
//    var rtcEngine: RtcEngine? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        binding.btnAudio.setOnClickListener(this)
//        binding.btnEndCall.setOnClickListener(this)
//        binding.btnFlip.setOnClickListener(this)
//        binding.btnVideo.setOnClickListener(this)
//
//        requestForPermission()
//        initAgoraAndJoinChannel()
//
//    }
//
//    private fun initAgoraAndJoinChannel() {
//        initRtcEngine()
//        setUpVideoConfig()
//        joinChannel()
//        setUpSessions()
//    }
//
//    private fun setUpSessions() {
//        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
//        rtcEngine?.enableVideo()
//        rtcEngine?.setVideoEncoderConfiguration(
//            VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x480,
//                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
//                VideoEncoderConfiguration.STANDARD_BITRATE,
//                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT)
//        )
//    }
//
//    private fun setUpVideoConfig() {
//        rtcEngine?.enableVideo()
//        rtcEngine?.setVideoEncoderConfiguration(VideoEncoderConfiguration(
//            VideoEncoderConfiguration.VD_640x360,
//            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_1,
//            VideoEncoderConfiguration.STANDARD_BITRATE,
//            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
//        ))
//    }
//
//    val iRtcEventHandler = object : IRtcEngineEventHandler() {
//        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
//            super.onJoinChannelSuccess(channel, uid, elapsed)
//        }
//
//        override fun onUserJoined(uid: Int, elapsed: Int) {
//            super.onUserJoined(uid, elapsed)
//            setupRemoteVideo(uid)
//        }
//    }
//
//    private fun setupRemoteVideo(uid: Int) {
//        remoteVideo = RtcEngine.CreateRendererView(this)
//        remoteVideo?.setZOrderMediaOverlay(true)
//        binding.remoteVideoContainer.addView(remoteVideo)
//        rtcEngine?.setupRemoteVideo(VideoCanvas(remoteVideo, VideoCanvas.RENDER_MODE_FIT, uid))
//    }
//
//    private fun initRtcEngine() {
//        try {
//            rtcEngine = RtcEngine.create(baseContext, getString(R.string.app_id), iRtcEventHandler)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun joinChannel() {
//        rtcEngine?.joinChannel(null, "aliya_mirza", "", 0)
//    }
//

//
//    fun hasCameraPermissionGrant() = ActivityCompat.checkSelfPermission(this,
//        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.CAMERA
//    ) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasGallaryPermission() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    ) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasChangeAudioSettings() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.MODIFY_AUDIO_SETTINGS
//    ) == PackageManager.PERMISSION_GRANTED
//
//
//    private fun hasRecordAudio() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.RECORD_AUDIO
//    ) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasBluetooth() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.BLUETOOTH
//    ) == PackageManager.PERMISSION_GRANTED
//
//    private fun hasNetworkState() = ActivityCompat.checkSelfPermission(
//        this,
//        Manifest.permission.ACCESS_NETWORK_STATE
//    ) == PackageManager.PERMISSION_GRANTED
//
//    private fun requestForPermission() {
//
//        val permissionList = mutableListOf<String>()
//
//        if (!hasCameraPermission()) {
//            permissionList.add(Manifest.permission.CAMERA)
//        }
//
//        if (!hasChangeAudioSettings()) {
//            permissionList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
//        }
//
//        if (!hasGallaryPermission()) {
//            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
//        }
//
//        if (!hasNetworkState()) {
//            permissionList.add(Manifest.permission.ACCESS_NETWORK_STATE)
//        }
//        if (!hasBluetooth()) {
//            permissionList.add(Manifest.permission.BLUETOOTH)
//        }
//        if (!hasRecordAudio()) {
//            permissionList.add(Manifest.permission.RECORD_AUDIO)
//        }
//
//        if (permissionList.isNotEmpty()) {
//
//            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
//        }
//
//    }
//
//
//
////    private fun initCameraPermission() {
////        val permissionList = mutableListOf<String>()
////        if (!hasCameraPermissionGrant()) permissionList.add(android.Manifest.permission.CAMERA)
////
////        if (permissionList.isNotEmpty()) {
////            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), 0)
////        }else{
////            initAgoraAndJoinChannel()
////        }
////    }
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 0 && permissions.isNotEmpty()) {
//            for (i in permissions.indices) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    initFrontCamera()
//                    initAgoraAndJoinChannel()
//                }
//            }
//        }
//    }
//
//    fun leaveChannel(){
//        rtcEngine?.leaveChannel()
//        rtcEngine =null
//    }
//
//    fun removeLocalVideo(){
//        if (localVideo !=null){
//            binding.localVideo.removeView(localVideo)
//        }
//        localVideo =null
//    }
//
//    fun removeRemoteVideo(){
//        if (remoteVideo !=null){
//            binding.remoteVideoContainer.removeView(remoteVideo)
//        }
//        remoteVideo =null
//    }
//
//    fun endCall(){
//        removeLocalVideo()
//        removeRemoteVideo()
//        leaveChannel()
//    }
//
//
//    private fun initFrontCamera() {
//        try {
//            localVideo = RtcEngine.CreateRendererView(this)
//            localVideo!!.setZOrderMediaOverlay(true)
//            binding.localVideo.addView(localVideo)
//            rtcEngine?.setupLocalVideo(VideoCanvas(localVideo, VideoCanvas.RENDER_MODE_FIT, 0))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//}