package com.example.screenmedia

import android.content.res.Configuration
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.dreams.DreamService
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.VideoView
import kotlinx.coroutines.*
import kotlin.math.abs
import kotlin.random.Random


class DreamSaver : DreamService() {
    val LogTag = "ScreenSaver"
    val ANGLE_LIMIT = 7.5
    val SCREEN_WIDTH:Int by lazy {
        windowManager.currentWindowMetrics.bounds.right
    }
    val SCREEN_HIGH:Int by lazy {
        windowManager.currentWindowMetrics.bounds.bottom
    }

    val videoView: VideoView by lazy {
        val it = findViewById<VideoView>(R.id.videoView)
        it.setOnCompletionListener {
            it.start()
        }
        it
    }

   private lateinit var textField: TextView

    val o: TextView by lazy { findViewById(R.id.o) }

    private val scope: CoroutineScope by lazy { CoroutineScope(Job()) }

    val scoreView: TextView by lazy {
        findViewById(R.id.score)
    }
    var score = 0.0f

    override fun onCreate() {
        super.onCreate()
        Log.e(LogTag, "onCreate")

    }

    override fun onAttachedToWindow() {
        Log.e(LogTag, "onAttachedToWindow")
        setContentView(R.layout.my_dream_service_layout)

        textField = findViewById(R.id.tec)
        textField.y = SCREEN_HIGH*.7f
        textField.x = (SCREEN_WIDTH-textField.width).toFloat()

        val sensorManager = (getSystemService(SENSOR_SERVICE) as SensorManager)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            textField.visibility = VISIBLE
            o.visibility = VISIBLE
        } else {
            textField.visibility = INVISIBLE
            o.visibility = INVISIBLE
        }

        sensorManager.registerListener(
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                if (event != null &&
                event.sensor.type == TYPE_ACCELEROMETER &&
                abs(event.values[0]) <= ANGLE_LIMIT)
                {
                    textField.x = (
                            (1 -(event.values[0] + ANGLE_LIMIT) / (2 * ANGLE_LIMIT))
                            * (SCREEN_WIDTH - textField.width)
                            ).toFloat()

                }
            }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    Log.d("Sensor", "onAccur    1q21acyChanged")
                }

            },
            sensorManager.getDefaultSensor(TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        isFullscreen = true
        isInteractive = false
        videoView.setVideoPath("android.resource://" + packageName + "/" + R.raw.window)

        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            scope.launch {
            val REST = 40
            val differential = Point(30,40)
                var mult = 1f

            while (true) {
                mult+=0.005f
                if (!isActive) return@launch
                if (o.x < REST || o.x > SCREEN_WIDTH-2*REST){
                    differential.x = -differential.x
                }
                if(o.y < 8*REST)differential.y = -differential.y

                if(o.y > textField.y+REST) {
                    o.x = (Random.nextInt(3) + 5) * 120f
                    o.y = (Random.nextInt(3) + 5) * 120f
                    mult = 1f
                    score = 0f
                }
                if(o.y > textField.y-4*REST && o.x > textField.x &&
                    o.x < textField.x+textField.width) {
                    differential.y = -differential.y

                }
                o.x += differential.x*mult
                o.y += differential.y*mult
                delay(100)
                score+=0.1f
                scoreView.text = "${score.toInt()}"
            }
        }

        super.onAttachedToWindow()
    }


    override fun onDreamingStarted() {
        Log.e(LogTag, "onDreamingStarted")
        super.onDreamingStarted()
        videoView.start()
    }
    override fun onDreamingStopped() {
        Log.e(LogTag, "onDreamingStopped")
        super.onDreamingStopped()
        videoView.stopPlayback()
    }

    override fun onDetachedFromWindow() {
        scope.cancel()
        super.onDetachedFromWindow()
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
            wakeUp()
    }

    override fun onDestroy() {
        Log.e(LogTag, "onDestroy")
        super.onDestroy()
    }
}