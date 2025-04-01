package com.example.platform_channel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel

class MainActivity : FlutterActivity(), SensorEventListener {
    // Flutter와 동일하게 채널 이름 설정
    private val BATTERY_CHANNEL = "samples.flutter.dev/battery"
    private val ACCEL_CHANNEL = "samples.flutter.dev/accelerometer"

    private var sensorManager: SensorManager? = null
    private var accelEventSink: EventChannel.EventSink? = null

    /// configureFlutterEngine안에 MethodChannel & EventChannel 생성
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        /// MethodChannel: 배터리 처리
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, BATTERY_CHANNEL)
            .setMethodCallHandler { call, result ->
                if (call.method == "getBatteryLevel") {
                    val level = getBatteryLevel()
                    if (level != -1) {
                        result.success(level)
                    } else {
                        result.error("UNAVAILABLE", "Battery level not available", null)
                    }
                } else {
                    result.notImplemented()
                }
            }

        /// EventChannel: 가속도 센서 처리
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, ACCEL_CHANNEL)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                    accelEventSink = events
                    val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    sensorManager?.registerListener(
                        this@MainActivity, sensor, SensorManager.SENSOR_DELAY_NORMAL
                    )
                }

                override fun onCancel(arguments: Any?) {
                    sensorManager?.unregisterListener(this@MainActivity)
                    accelEventSink = null
                }
            })
    }

    /// 센서 값 수신 → Dart로 전달
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            accelEventSink?.success("X: ${x.toString().take(3)}, Y: ${y.toString().take(3)}, Z: ${z.toString().take(3)}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    /// 배터리 가져오는 함수
    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
