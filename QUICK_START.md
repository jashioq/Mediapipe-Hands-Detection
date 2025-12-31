# Quick Start Guide

## Ready to Run!

Your MediaPipe Hands Detection app is fully configured and ready to use.

## What's Included

✅ All source code files created
✅ Gradle dependencies configured
✅ Camera permissions set up
✅ MediaPipe model downloaded (7.5 MB)
✅ Complete MVVM architecture
✅ Real-time visualization

## Files Created

### Core Application Files
1. `MainActivity.kt` - App entry point
2. `HandTrackingViewModel.kt` - MediaPipe integration & state management
3. `HandTrackingScreen.kt` - Compose UI with camera preview
4. `HandLandmarkOverlay.kt` - Canvas drawing for landmarks
5. `HandResult.kt` - Data models
6. `CameraManager.kt` - CameraX setup

### Configuration Files
- `app/build.gradle.kts` - Updated with all dependencies
- `AndroidManifest.xml` - Camera permissions added
- `app/src/main/assets/hand_landmarker.task` - MediaPipe model (7.5 MB)

## Run the App

### Option 1: Android Studio
1. Open project in Android Studio
2. Click "Sync Project with Gradle Files" (if needed)
3. Connect your Android device via USB or start an emulator
4. Click the green "Run" button (Shift + F10)
5. Grant camera permission when prompted
6. Point camera at your hand!

### Option 2: Command Line
```bash
# Build and install
./gradlew installDebug

# Or build + install + run
./gradlew installDebug && adb shell am start -n com.jan.mediapipehandsdetection/.MainActivity
```

## First Use

1. **Grant Permission**: When you first launch, tap "Grant Permission" to allow camera access
2. **Position Your Hand**: Hold your hand in front of the camera
3. **See Landmarks**: 21 green/blue dots will appear on your hand
4. **Try Both Hands**: The app can track up to 2 hands simultaneously
5. **Adjust Settings**: Tap the settings icon (⚙️) to adjust detection parameters
6. **Switch Camera**: Tap the camera switch icon to toggle front/back camera

## UI Controls

- **Top-Right**: FPS counter shows performance
- **Top-Left**: Settings button (⚙️) - opens configuration sheet
- **Top-Left**: Camera switch button - toggle front/back camera

## Settings You Can Adjust

In the settings bottom sheet:
- **Min Detection Confidence** (0.0 - 1.0)
- **Min Presence Confidence** (0.0 - 1.0)
- **Min Tracking Confidence** (0.0 - 1.0)
- **Max Number of Hands** (1 or 2)

## Visual Indicators

- **Green dots/lines**: Left hand
- **Blue dots/lines**: Right hand
- **White lines**: Connections between landmarks
- **Circle size**: 8dp radius
- **Line width**: 2dp

## Expected Performance

- **Physical Device**: 25-30 FPS
- **Emulator**: 10-20 FPS (slower, use physical device for best results)
- **Processing Time**: <50ms per frame

## Tips for Best Results

✅ **Good Lighting**: Ensure your hand is well-lit
✅ **Clear Background**: Plain backgrounds work better
✅ **Full Hand Visible**: Show your entire palm and fingers
✅ **Distance**: Hold hand 1-3 feet from camera
✅ **Physical Device**: Use real device for better performance

## Troubleshooting

### "No hands detected"
- Check lighting conditions
- Move hand closer to camera
- Lower detection confidence in settings
- Ensure palm is facing camera

### "Low FPS"
- Use physical device instead of emulator
- Reduce max hands to 1
- Close other apps

### "Camera won't start"
- Grant camera permission
- Try switching cameras
- Restart the app

## Testing Emulator Setup

To use webcam on emulator:
1. Open AVD Manager
2. Edit your virtual device
3. Under "Camera", set:
   - Front camera: Webcam0
   - Back camera: Webcam0
4. Start emulator
5. Your computer's webcam will be used

## Next Steps

- Experiment with different hand poses
- Try tracking two hands simultaneously
- Adjust confidence thresholds for your use case
- Test in different lighting conditions
- Try gesture recognition by monitoring landmark positions

## Project Documentation

For detailed information, see:
- `PROJECT_SETUP.md` - Complete technical documentation
- `app/src/main/assets/README.md` - MediaPipe model info

## Support

If you encounter issues:
1. Clean and rebuild: Build → Clean Project, then Build → Rebuild Project
2. Sync Gradle: File → Sync Project with Gradle Files
3. Invalidate caches: File → Invalidate Caches / Restart
4. Check that hand_landmarker.task exists in assets folder

---

**Enjoy building with MediaPipe Hands! 👋**
