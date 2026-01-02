# Real-time offline hand tracking with MediaPipe app

A real-time hand tracking Android app using MediaPipe. Detects up to 2 hands simultaneously and visualizes 21 landmarks per hand overlaid on the camera preview. Uses MVVM architecture.

https://github.com/user-attachments/assets/55e474bf-5ce1-41ee-8090-fe54366d1a23

## Configuration options

Adjustable via settings bottom sheet:

```kotlin
data class HandTrackingConfig(
    val minHandDetectionConfidence: Float = 0.5f,
    val minHandPresenceConfidence: Float = 0.5f,
    val minTrackingConfidence: Float = 0.5f,
    val maxNumHands: Int = 2
)
```

**Parameters explained:**
- **Detection confidence**: Initial hand detection threshold (higher = fewer false positives)
- **Presence confidence**: Confidence threshold for hand still being present in frame
- **Tracking confidence**: Landmark tracking threshold (higher = more stable, but may drop hands)
- **Max hands**: Number of hands to detect (1 or 2)

---

## How it works

This app transforms camera frames into hand landmark coordinates through a 4 step pipeline:

---

### **Step 1: Camera frame capture**

Camera captures frames and delivers them for processing:

```kotlin
private fun createImageAnalyzer() = ImageAnalysis.Builder()
    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
    .build()
    .also { it.setAnalyzer(cameraExecutor, onFrameAnalyzed) }
```

**Key configuration:**
- **STRATEGY_KEEP_ONLY_LATEST**: Drops old frames if processing is slow
- **RGBA_8888**: Format compatible with MediaPipe's image builder

---

### **Step 2: Frame preprocessing**

Camera frames are converted to a format expected by MediaPipe:

```kotlin
// Convert ImageProxy to Bitmap
val bitmap = ImageProcessingUtils.imageProxyToBitmap(imageProxy)

// Rotate based on device orientation
val rotatedBitmap = ImageProcessingUtils.rotateBitmap(
    bitmap,
    imageProxy.imageInfo.rotationDegrees.toFloat()
)

// Convert to MediaPipe Image
val mpImage = BitmapImageBuilder(rotatedBitmap).build()
```

**Why rotation matters:**
- Device orientation affects camera output (portrait vs landscape)
- MediaPipe expects images in a standard orientation
- Rotation ensures landmarks map correctly to screen coordinates

---

### **Step 3: Hand detection with MediaPipe**

The preprocessed image is fed through MediaPipe's hand landmarker:

```kotlin
val options = HandLandmarker.HandLandmarkerOptions.builder()
    .setRunningMode(RunningMode.VIDEO)
    .setNumHands(config.maxNumHands)
    .setMinHandDetectionConfidence(config.minHandDetectionConfidence)
    .setMinHandPresenceConfidence(config.minHandPresenceConfidence)
    .setMinTrackingConfidence(config.minTrackingConfidence)
    .build()

val result = landmarker.detectForVideo(mpImage, timestamp)
```

**What happens inside:**
- **Palm detection**: Locates hands in the frame using a lightweight CNN
- **Landmark regression**: Predicts 21 landmarks per hand
- **Hand classification**: Determines left vs right hand
- **Tracking**: Uses previous frame data for consistency

The model returns normalized coordinates for each landmark.

---

### **Step 4: Visualization on a canvas**

Landmarks are drawn as colored circles with connecting lines:

```kotlin
// Transform normalized coordinates to screen pixels
val x = CoordinateTransformationUtils.transformLandmarkCoordinate(
    normalizedValue = landmark.x(),
    previewSize = bounds.contentWidth,
    offset = bounds.offsetX,
    isFrontCamera = isFrontCamera,
    isXAxis = true
)

// Draw connections (white lines)
drawLine(
    color = Color.White,
    start = Offset(startX, startY),
    end = Offset(endX, endY),
    strokeWidth = 2.dp
)

// Draw landmarks (colored circles)
drawCircle(
    color = handColor,
    radius = 4.dp,
    center = Offset(x, y)
)
```

**Coordinate transformation pipeline:**
1. Normalized to screen pixels (multiply by view dimensions)
2. Account for letterbox offsets (black bars on sides or top and bottom)
3. Mirror X-axis for front camera (to match preview)
4. Apply offsets to center content in view
