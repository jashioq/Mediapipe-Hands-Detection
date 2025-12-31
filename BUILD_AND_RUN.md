# Build and Run Instructions

## ✅ Project Status: READY TO BUILD

All files are in place and the project is ready to build and run!

## Quick Build Check

Before building, verify:
- ✅ 9 Kotlin source files created
- ✅ 3 Gradle configuration files updated
- ✅ MediaPipe model downloaded (7.5 MB in assets)
- ✅ Camera permissions configured
- ✅ All dependencies specified

## Method 1: Android Studio (Recommended)

### Step 1: Open Project
```
File → Open → Select "MediaPipeHandsDetection" folder
```

### Step 2: Sync Gradle
```
File → Sync Project with Gradle Files
```
Wait for Gradle sync to complete (may take 2-5 minutes first time).

### Step 3: Build Project
```
Build → Make Project (Ctrl+F9 / Cmd+F9)
```

### Step 4: Run on Device
1. Connect Android device via USB OR start an emulator
2. Enable USB debugging on your device (if using physical device)
3. Click the green "Run" button or press `Shift + F10`
4. Select your target device
5. Wait for app to install and launch

### Step 5: Grant Permission
When app launches, tap "Grant Permission" to allow camera access.

## Method 2: Command Line (Gradle)

### Build APK
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Install APK
```bash
# Windows
gradlew.bat installDebug

# Linux/Mac
./gradlew installDebug
```

### Build + Install + Run
```bash
# Windows
gradlew.bat installDebug
adb shell am start -n com.jan.mediapipehandsdetection/.MainActivity

# Linux/Mac
./gradlew installDebug
adb shell am start -n com.jan.mediapipehandsdetection/.MainActivity
```

## Method 3: Direct APK Installation

### Build APK
```bash
./gradlew assembleDebug
```

### Install via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Launch App
```bash
adb shell am start -n com.jan.mediapipehandsdetection/.MainActivity
```

## Gradle Tasks Reference

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing)
./gradlew assembleRelease

# Install debug APK
./gradlew installDebug

# Uninstall app
./gradlew uninstallDebug

# Run tests
./gradlew test

# Generate APK and run tests
./gradlew build
```

## Troubleshooting Build Issues

### Issue: "SDK not found"
**Solution:**
```bash
# Create/update local.properties with your SDK path
echo "sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk" > local.properties
# (Adjust path for your system)
```

### Issue: "Gradle sync failed"
**Solution:**
1. File → Invalidate Caches / Restart
2. Delete `.gradle` and `.idea` folders
3. Reopen project in Android Studio
4. Let Gradle sync again

### Issue: "Execution failed for task ':app:mergeDebugResources'"
**Solution:**
```bash
./gradlew clean
./gradlew assembleDebug
```

### Issue: "Installed Build Tools revision X.X.X is corrupted"
**Solution:**
1. Open SDK Manager (Tools → SDK Manager)
2. Uninstall and reinstall Android SDK Build-Tools
3. Sync Gradle again

### Issue: "hand_landmarker.task not found"
**Solution:**
```bash
cd app/src/main/assets
curl -L -o hand_landmarker.task "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/latest/hand_landmarker.task"
```

### Issue: "Unresolved reference: HandTrackingViewModel"
**Solution:**
1. Build → Clean Project
2. Build → Rebuild Project
3. If still failing, check that all .kt files are in correct package

## Device Requirements

### Minimum Requirements
- Android 7.0 (API 24) or higher
- Camera support
- 200 MB free RAM
- ARM or x86 processor

### Recommended Specifications
- Android 12+ (API 31+)
- Snapdragon 8 series or equivalent
- 4 GB+ RAM
- Good lighting conditions for camera

## Testing on Emulator

### Create AVD (Android Virtual Device)
1. Tools → Device Manager
2. Create Device
3. Select device (e.g., Pixel 5)
4. Select system image (API 31+ recommended)
5. Configure camera:
   - Front camera: Webcam0
   - Back camera: Webcam0
6. Click Finish

### Launch Emulator
```bash
# List available emulators
emulator -list-avds

# Launch specific emulator
emulator -avd Pixel_5_API_31
```

### Performance on Emulator
- **Expected FPS**: 10-20 FPS (slower than physical device)
- **Camera**: Uses your computer's webcam
- **Recommendation**: Use physical device for best experience

## First Run Checklist

After successful build and installation:

- [ ] App launches without crashes
- [ ] Camera permission dialog appears
- [ ] Granting permission shows camera preview
- [ ] Camera preview fills screen
- [ ] FPS counter visible in top-right
- [ ] Settings button visible in top-left
- [ ] Camera switch button visible in top-left
- [ ] Hand detection works (show hand to camera)
- [ ] Landmarks appear as green/blue dots
- [ ] Lines connect landmarks
- [ ] Settings sheet opens when tapping settings icon
- [ ] Camera switches when tapping camera icon

## Performance Benchmarks

### Expected Build Times
- **First build**: 3-7 minutes (downloads dependencies)
- **Clean build**: 1-3 minutes
- **Incremental build**: 10-30 seconds

### Expected Runtime Performance
- **Startup time**: 1-2 seconds
- **Camera initialization**: <1 second
- **Hand detection latency**: <50ms
- **FPS**: 25-30 on modern devices

## Build Variants

### Debug Build (Development)
```bash
./gradlew assembleDebug
```
- Includes debugging symbols
- Not optimized
- Larger APK size (~30 MB)
- Connects to Android Studio debugger

### Release Build (Production)
```bash
./gradlew assembleRelease
```
- Optimized and minified
- Smaller APK size (~20 MB)
- Requires signing configuration
- Better performance

## Debugging

### View Logs
```bash
# View all logs
adb logcat

# Filter by app
adb logcat | grep "com.jan.mediapipehandsdetection"

# Clear logs and watch
adb logcat -c && adb logcat
```

### Common Log Messages
```
✅ "Camera initialized successfully"
✅ "HandLandmarker created"
✅ "Processing frame..."
❌ "Failed to initialize camera"
❌ "MediaPipe model not found"
```

### Android Studio Debugger
1. Set breakpoint in code (click left margin)
2. Run → Debug 'app' (Shift+F9)
3. App pauses at breakpoint
4. Inspect variables and step through code

## APK Size Breakdown

```
Total APK size: ~30 MB (debug) / ~20 MB (release)

Breakdown:
- MediaPipe model: 7.5 MB
- MediaPipe library: 12 MB
- CameraX libraries: 5 MB
- Compose libraries: 3 MB
- App code: 0.5 MB
- Resources: 2 MB
```

## Next Steps After Successful Build

1. ✅ Run app and verify all features work
2. ✅ Test camera switching
3. ✅ Test settings adjustments
4. ✅ Test with both hands
5. ✅ Check FPS performance
6. ✅ Test in different lighting
7. ✅ Test on multiple devices

## Additional Resources

- **Project Setup**: See `PROJECT_SETUP.md`
- **Quick Start**: See `QUICK_START.md`
- **Architecture**: See `ARCHITECTURE.md`
- **MediaPipe Docs**: https://developers.google.com/mediapipe
- **CameraX Docs**: https://developer.android.com/training/camerax
- **Compose Docs**: https://developer.android.com/jetpack/compose

---

**Ready to build? Run: `./gradlew assembleDebug`**
