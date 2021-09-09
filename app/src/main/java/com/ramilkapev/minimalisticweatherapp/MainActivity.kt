package com.ramilkapev.minimalisticweatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    lateinit var mToolbar: MaterialToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, MainFragment()).commit()
        }
        mToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(mToolbar)
    }
}