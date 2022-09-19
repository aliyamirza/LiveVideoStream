package com.aliya.livevideostream

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.aliya.livevideostream.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),View.OnClickListener {

    var callEnd = false
    var callMute = false
    var cameraFlip = false
    var videoFlip = false
    var localView:SurfaceView?=null


    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        binding.btnAudio.setOnClickListener(this)
        binding.btnFlip.setOnClickListener(this)
        binding.btnEndCall.setOnClickListener(this)
        binding.btnVideo.setOnClickListener(this)

        initCameraPermission()

    }

    fun hasCameraPermissionGrant() = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun initCameraPermission() {
        val permissionList = mutableListOf<String>()
        if (!hasCameraPermissionGrant()) permissionList.add(android.Manifest.permission.CAMERA)

        if (permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this,permissionList.toTypedArray(),0)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){

            binding.btnAudio.id ->{
                when(callMute){
                    false -> {
                        binding.btnAudio.setImageResource(R.drawable.ic_baseline_mic_24)
                        callMute = true
                    }
                    true ->{
                        binding.btnAudio.setImageResource(R.drawable.ic_baseline_mic_off_24)
                        callMute = false
                    }
                }
            }
            binding.btnEndCall.id ->{
                when(callEnd){
                    false -> {
                        binding.btnEndCall.setImageResource(R.drawable.ic_baseline_call_24)
                        callEnd = true
                    }
                    true ->{
                        binding.btnEndCall.setImageResource(R.drawable.ic_baseline_call_end_24)
                        callEnd = false
                    }
                }
            }
            binding.btnFlip.id ->{
                when(cameraFlip){
                    false -> {
                        binding.btnFlip.setImageResource(R.drawable.ic_baseline_flip_camera_android_24)
                        cameraFlip = true
                    }
                    true ->{
                        binding.btnFlip.setImageResource(R.drawable.ic_baseline_flip_camera_android_24)
                        cameraFlip = false
                    }
                }
            }
            binding.btnVideo.id ->{
                when(videoFlip){
                    false -> {
                        binding.btnVideo.setImageResource(R.drawable.ic_baseline_videocam_24)
                        videoFlip = true
                    }
                    true ->{
                        binding.btnVideo.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                        videoFlip = false
                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==0 && permissions.isNotEmpty()){
            for (i in permissions.indices){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    initFrontCamera()
                }
            }
        }
    }

    private fun initFrontCamera() {

    }
}