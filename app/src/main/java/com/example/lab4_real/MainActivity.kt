package com.example.lab4_real

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lab4_real.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        val manager = LinearLayoutManager(this)

        binding.recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, manager.orientation))
            layoutManager = manager
            adapter = Adapter(resources.openRawResource(R.raw.articles))
        }

        setContentView(R.layout.activity_main)
    }
}