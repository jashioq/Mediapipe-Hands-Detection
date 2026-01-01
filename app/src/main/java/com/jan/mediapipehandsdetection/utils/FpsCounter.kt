package com.jan.mediapipehandsdetection.utils

class FpsCounter {
    private var frameCount = 0
    private var fpsUpdateTime = System.currentTimeMillis()
    private var currentFps = 0

    /**
     * Records a new frame and updates FPS if one second has elapsed
     */
    fun recordFrame() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - fpsUpdateTime

        if (elapsed >= 1000) {
            currentFps = (frameCount * 1000 / elapsed).toInt()
            frameCount = 0
            fpsUpdateTime = currentTime
        }
    }

    fun getCurrentFps(): Int = currentFps
}
