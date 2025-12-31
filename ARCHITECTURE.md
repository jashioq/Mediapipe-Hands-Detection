# Architecture Overview

## Application Flow

```
┌─────────────────────────────────────────────────────────────┐
│                        MainActivity                          │
│  - Initializes HandTrackingViewModel                        │
│  - Sets up Compose UI                                       │
│  - Hides system bars (immersive mode)                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    HandTrackingScreen                        │
│  - Manages camera permissions (Accompanist)                 │
│  - Displays camera preview (AndroidView + PreviewView)      │
│  - Shows UI controls (FPS, Settings, Camera Switch)         │
│  - Overlays hand landmarks (HandLandmarkOverlay)            │
│  - Settings bottom sheet                                    │
└────────┬──────────────────────────────────────┬─────────────┘
         │                                      │
         ▼                                      ▼
┌─────────────────────┐              ┌──────────────────────┐
│   CameraManager     │              │ HandLandmarkOverlay  │
│  - CameraX setup    │              │  - Canvas drawing    │
│  - Frame capture    │              │  - Draw landmarks    │
│  - Camera switch    │              │  - Draw connections  │
│  - 30 FPS capture   │              │  - Color by hand     │
└──────────┬──────────┘              └──────────────────────┘
           │
           │ Frame (ImageProxy)
           ▼
┌─────────────────────────────────────────────────────────────┐
│              HandTrackingViewModel                           │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │ processFrame(imageProxy)                           │    │
│  │  1. Convert ImageProxy → Bitmap                    │    │
│  │  2. Rotate bitmap based on orientation             │    │
│  │  3. Convert to MediaPipe Image                     │    │
│  │  4. Run hand detection                             │    │
│  │  5. Extract landmarks                              │    │
│  │  6. Update UI state                                │    │
│  │  7. Calculate FPS                                  │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  Uses: Dispatchers.Default (background thread)              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                 MediaPipe HandLandmarker                     │
│  - Pre-trained hand detection model                         │
│  - Detects up to 2 hands                                    │
│  - Returns 21 landmarks per hand                            │
│  - Provides handedness (left/right)                         │
│  - Configurable confidence thresholds                       │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

```
Camera Frame (ImageProxy)
    │
    ▼
[Background Thread: Dispatchers.Default]
    │
    ├─► Convert to Bitmap
    │
    ├─► Rotate based on device orientation
    │
    ├─► Convert to MediaPipe Image (BitmapImageBuilder)
    │
    ├─► MediaPipe HandLandmarker.detectForVideo()
    │
    ├─► Extract HandLandmarkerResult
    │       │
    │       ├─► List<NormalizedLandmark> (21 landmarks)
    │       └─► Handedness (Left/Right)
    │
    ├─► Create HandResult objects
    │
    ├─► Update StateFlow<HandTrackingUiState>
    │       │
    │       └─► detectedHands: List<HandResult>
    │
    └─► Calculate and update FPS
            │
            ▼
[Main Thread: Compose]
    │
    ├─► UI observes uiState via collectAsStateWithLifecycle()
    │
    ├─► HandLandmarkOverlay receives detectedHands
    │
    └─► Canvas draws landmarks and connections
```

## State Management (MVVM)

```
┌─────────────────────────────────────────────────────────────┐
│                  HandTrackingUiState                         │
│  ┌────────────────────────────────────────────────────┐    │
│  │ data class HandTrackingUiState(                    │    │
│  │     val detectedHands: List<HandResult>,           │    │
│  │     val fps: Int,                                  │    │
│  │     val cameraFacing: Int,                         │    │
│  │     val config: HandTrackingConfig,                │    │
│  │     val showSettingsSheet: Boolean                 │    │
│  │ )                                                  │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ StateFlow
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              HandTrackingViewModel                           │
│                                                              │
│  private val _uiState = MutableStateFlow(...)               │
│  val uiState: StateFlow<HandTrackingUiState> = ...          │
│                                                              │
│  Functions:                                                  │
│  - initializeHandLandmarker()                               │
│  - processFrame()                                           │
│  - toggleCamera()                                           │
│  - updateConfig()                                           │
│  - toggleSettingsSheet()                                    │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ collectAsStateWithLifecycle()
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 Compose UI (Screen)                          │
│                                                              │
│  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
│                                                              │
│  Renders based on state:                                    │
│  - Camera preview                                           │
│  - Hand landmarks (uiState.detectedHands)                   │
│  - FPS counter (uiState.fps)                                │
│  - Settings sheet (uiState.showSettingsSheet)               │
└─────────────────────────────────────────────────────────────┘
```

## Data Models

```
┌─────────────────────────────────────────────────────────────┐
│                      HandResult                              │
│  - landmarks: List<NormalizedLandmark>  (21 landmarks)      │
│  - handedness: HandType  (LEFT or RIGHT)                    │
│  - timestamp: Long                                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       HandType                               │
│  enum class HandType { LEFT, RIGHT }                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                  HandTrackingConfig                          │
│  - minHandDetectionConfidence: Float  (default: 0.5)        │
│  - minHandPresenceConfidence: Float   (default: 0.5)        │
│  - minTrackingConfidence: Float       (default: 0.5)        │
│  - maxNumHands: Int                   (default: 2)          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              HandLandmarkConnections                         │
│  - CONNECTIONS: List<Pair<Int, Int>>                        │
│    (defines which landmarks connect to form skeleton)       │
└─────────────────────────────────────────────────────────────┘
```

## Hand Landmark Structure

```
21 Landmarks per hand:

           8   12  16  20     Finger tips
           |   |   |   |
           7   11  15  19
           |   |   |   |
           6   10  14  18
           |   |   |   |
    4      5   9   13  17     Base of fingers
    |     / \ / \ / \ /
    3    /   X   X   X        Palm connections
    |   /   / \ / \ / \
    2  0───5───9───13──17     Wrist to pinky base
    | /
    1/
    |
    0  ← Wrist

Landmark IDs:
0:  Wrist
1-4:   Thumb (CMC, MCP, IP, Tip)
5-8:   Index (MCP, PIP, DIP, Tip)
9-12:  Middle (MCP, PIP, DIP, Tip)
13-16: Ring (MCP, PIP, DIP, Tip)
17-20: Pinky (MCP, PIP, DIP, Tip)
```

## Threading Model

```
┌─────────────────────────────────────────────────────────────┐
│                       Main Thread                            │
│  - Compose UI rendering                                     │
│  - User interactions                                        │
│  - Camera preview display                                   │
│  - Canvas drawing (HandLandmarkOverlay)                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ StateFlow updates
                     │
┌────────────────────┴────────────────────────────────────────┐
│                  CameraX Executor Thread                     │
│  - Camera frame capture                                     │
│  - Image analysis callback                                  │
│  - Passes ImageProxy to ViewModel                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ imageProxy
                     │
┌────────────────────┴────────────────────────────────────────┐
│            Background Thread (Dispatchers.Default)           │
│  - Bitmap conversion and rotation                           │
│  - MediaPipe hand detection                                 │
│  - Landmark extraction                                      │
│  - FPS calculation                                          │
│  - StateFlow updates                                        │
└─────────────────────────────────────────────────────────────┘
```

## Visualization Pipeline

```
HandResult (normalized coordinates 0.0-1.0)
    │
    ▼
HandLandmarkOverlay (Compose Canvas)
    │
    ├─► Convert normalized → screen coordinates
    │   landmark.x() * viewWidth  → screen X
    │   landmark.y() * viewHeight → screen Y
    │
    ├─► Draw connections (white lines, 2dp width)
    │   For each connection pair (start, end):
    │   drawLine(from: start, to: end)
    │
    └─► Draw landmarks (colored circles, 8dp radius)
        For each landmark:
        drawCircle(at: position, color: green/blue)

Color scheme:
- Left hand:  Green (0xFF00FF00)
- Right hand: Blue  (0xFF0080FF)
- Lines:      White (0xFFFFFFFF)
```

## Permission Flow

```
App Launch
    │
    ▼
Check Camera Permission
    │
    ├─► Granted ──────────────────────────┐
    │                                     │
    └─► Not Granted                       │
            │                             │
            ▼                             │
    Show Permission Dialog                │
            │                             │
            ├─► User Grants ──────────────┤
            │                             │
            └─► User Denies               │
                    │                     │
                    ▼                     │
            Show "Grant Permission"       │
            button and explanation        │
                    │                     │
                    └─► Retry ────────────┤
                                          │
                                          ▼
                                Initialize Camera
                                          │
                                          ▼
                                    Start Tracking
```

## Configuration Update Flow

```
User opens Settings Sheet
    │
    ▼
Adjust sliders
    │
    ▼
Tap "Apply Settings"
    │
    ├─► Create new HandTrackingConfig
    │
    ├─► Update ViewModel state
    │
    ├─► Close existing HandLandmarker
    │
    ├─► Reinitialize HandLandmarker with new config
    │
    └─► Resume hand tracking with new parameters
```

## Key Technologies

- **UI Framework**: Jetpack Compose (Material 3)
- **Camera**: CameraX (Camera2 API wrapper)
- **ML Framework**: MediaPipe Tasks Vision
- **Concurrency**: Kotlin Coroutines + Flow
- **Architecture**: MVVM (Model-View-ViewModel)
- **State**: StateFlow (reactive state management)
- **Permissions**: Accompanist Permissions
- **Drawing**: Compose Canvas API

## Performance Optimizations

1. **Background Processing**: Frame processing on Dispatchers.Default
2. **Frame Strategy**: KEEP_ONLY_LATEST (drop old frames if processing is slow)
3. **Efficient Format**: RGBA_8888 for direct MediaPipe compatibility
4. **Single Executor**: Dedicated thread for camera operations
5. **StateFlow**: Only UI updates when state actually changes
6. **Canvas Caching**: Compose automatically caches canvas operations
