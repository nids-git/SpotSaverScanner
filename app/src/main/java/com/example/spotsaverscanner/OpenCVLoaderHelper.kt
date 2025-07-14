package com.example.spotsaverscanner

import android.content.Context
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat

object OpenCVLoaderHelper {
    fun init(context: Context): Boolean {
        val success = OpenCVLoader.initLocal()
        if (success) {
            Log.d("OpenCV", "OpenCV loaded successfully")


            // Test OpenCV functionality
            val mat = Mat.eye(3, 3, CvType.CV_8UC1)
            Log.d("OpenCV", "Created matrix: \n$mat")
            val matDump = Mat.eye(3, 3, CvType.CV_8UC1).dump()
            Log.d("OpenCV", "Matrix data:\n$matDump")
        } else {
            Log.e("OpenCV", "OpenCV failed to load")
        }
        return success
    }
}