package com.example.spotsaverscanner

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotsaverscanner.model.OverlayData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.aruco.Aruco
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.ArucoDetector
import org.opencv.objdetect.DetectorParameters

class CameraViewModel : ViewModel() {

    private val _scanState = MutableStateFlow<OverlayData?>(null)
    val scanState = _scanState.asStateFlow()

    private var lastMarkerId: Int? = null
    private var stableCount = 0
    private var detectionPaused = false

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun processFrame(bitmap: Bitmap, context: Context) {
        if (detectionPaused) return

        val markerId = detectMarker(bitmap)
        if (markerId != null) {
            if (markerId == lastMarkerId) {
                stableCount++
                if (stableCount >= 3) {
                    val decoded = decodeMarker(markerId)
                    _scanState.value = OverlayData(markerId, decoded)

                    // Vibrate on detection
                    val vibrator =
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

                    // Pause detection for 2 seconds after success
                    detectionPaused = true
                    viewModelScope.launch {
                        delay(2000)
                        detectionPaused = false
                        stableCount = 0
                        _scanState.value = null
                    }
                }
            } else {
                stableCount = 1
                lastMarkerId = markerId
            }
        } else {
            stableCount = 0
            lastMarkerId = null
        }
    }

    private fun detectMarker(bitmap: Bitmap): Int? {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB) // Required for ArUco

        val dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_4X4_50)
        val parameters = DetectorParameters()

        // Initialize the modern ArucoDetector
        val detector = ArucoDetector(dictionary, parameters)
        val corners = ArrayList<Mat>()
        val ids = Mat()

        detector.detectMarkers(mat, corners, ids)

        return if (ids.total() > 0) {
            ids.get(0, 0)[0].toInt()
        } else {
            null
        }
    }

    private fun decodeMarker(markerId: Int): String {
        val map = mapOf(
            1 to "Hello World",
            2 to "Welcome",
            3 to "Sample Message",
            4 to "Scan Successful",
            5 to "SpotSaver"
        )
        return map[markerId] ?: "Unknown Marker"
    }
}
