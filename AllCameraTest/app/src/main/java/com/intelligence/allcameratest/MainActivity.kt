package com.intelligence.allcameratest

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.dfqin.grantor.PermissionListener
import com.github.dfqin.grantor.PermissionsUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        capture.setOnClickListener { startActivity(Intent(this,CaptureActivity::class.java)) }
        camera.setOnClickListener { startActivity(Intent(this,Camera2Activity::class.java)) }
        custom.setOnClickListener { startActivity(Intent(this,CustomViewActivity::class.java)) }
        download.setOnClickListener { startActivity(Intent(this,CommonActivity::class.java)) }
        data_binding.setOnClickListener { startActivity(Intent(this,CountryPickerActivity::class.java)) }
        requestPermission()
    }

    fun requestPermission() {
        val listener = object : PermissionListener {
            override fun permissionDenied(permission: Array<out String>) {
            }

            override fun permissionGranted(permission: Array<out String>) {
            }
        }
        if (!PermissionsUtil.hasPermission(this, Manifest.permission.CAMERA)) {
            PermissionsUtil.requestPermission(this, listener, Manifest.permission.CAMERA)
        }
        if (!PermissionsUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionsUtil.requestPermission(
                this,
                listener,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

}
