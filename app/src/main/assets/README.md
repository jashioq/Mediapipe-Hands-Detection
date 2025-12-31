# MediaPipe Hand Landmarker Model

This directory should contain the MediaPipe Hand Landmarker model file.

## Download Instructions

1. Download the `hand_landmarker.task` model from:
   https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task

2. Place the downloaded `hand_landmarker.task` file in this directory:
   `app/src/main/assets/hand_landmarker.task`

## Alternative Download Methods

### Using wget (Linux/Mac):
```bash
cd app/src/main/assets
wget https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
```

### Using curl:
```bash
cd app/src/main/assets
curl -O https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task
```

### Using PowerShell (Windows):
```powershell
cd app\src\main\assets
Invoke-WebRequest -Uri "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task" -OutFile "hand_landmarker.task"
```

The model file is approximately 26 MB in size.
