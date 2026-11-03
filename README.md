# CLR Music 🎵

[![Build CLR Music APK](https://github.com/Prashantv223/CLR-music-/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Prashantv223/CLR-music-/actions/workflows/build-apk.yml)

**CLR Music** is an AI-powered Digital Audio Workstation (DAW), Beat Maker, and Suno AI-style song creator for Android.

---

## 🚀 Features

- **Suno AI-Style Song Creation**: Generate full songs with multi-track stems (Vocals, 808 Bass, Drums, Chords) directly from text lyrics, mood, and genre.
- **Synchronized Karaoke & Lyrics Editor**: Real-time line-by-line scrolling lyrics with instant lyrics editing and synchronization.
- **Multitrack Studio Timeline**: 5-track DAW arrangement with volume faders (0–150%), stereo pan (L–R), solo/mute, and canvas waveform visualization.
- **16-Step Drum Sequencer & Beat Pads**: 4 drum voice lanes with presets (Trap Drill, Lo-Fi Chill, Default 4/4) + 12 touch-responsive MPC beat pads.
- **Virtual MIDI Keyboard**: 2-octave piano keyboard with octave switching and pitch playback.
- **Vocal Booth & Mic Recording**: Real-time RMS amplitude visualizer, automated AI background noise reduction gate, and voice-to-text transcription.
- **Studio VST Effects Rack**: 5-Band Graphic Equalizer (Flat, Bass Boost, Club 808, Vocal Boost, Rock, Pop, Hip Hop), Space Reverb, Tape Delay, and Distortion.
- **Hi-Fi Music Player**: Animated vinyl artwork disc, waveform seekbar, karaoke view, and social media sharing.
- **Export & Collab**: Export High-Fidelity WAV (44.1kHz 16-bit), individual stems pack, and live collaborative session rooms.

---

## 🛠️ GitHub Actions Automated APK Build Workflow

This repository (`Prashantv223/CLR-music-`) is configured with automated GitHub Actions CI/CD to build the Android APK on every push, pull request, or tag:

- **Workflow File**: `.github/workflows/build-apk.yml`
- **Triggers**:
  - Pushes to `main` or `master` branches
  - Pull requests targeting `main` or `master`
  - Version tags (e.g. `v1.0.0`) to automatically generate a GitHub Release with the attached APK
  - Manual triggers via the **Actions** tab in GitHub (**Run workflow** button)

### How to Download the Built APK from GitHub Actions:
1. Navigate to the **Actions** tab in the repository: `https://github.com/Prashantv223/CLR-music-/actions`
2. Click on the latest workflow run: **Build CLR Music APK**.
3. Under the **Artifacts** section at the bottom, click **CLR-Music-app-debug** to download the ZIP containing the APK.

---

## 💻 Local Build Instructions

### Prerequisites
- JDK 17 or 21
- Android SDK (API Level 36 / 35 / 34)

### Building the APK
```bash
# Clone the repository
git clone https://github.com/Prashantv223/CLR-music-.git
cd CLR-music-

# Prepare environment file
cp .env.example .env

# Restore debug keystore (if not already present)
base64 -d debug.keystore.base64 > debug.keystore

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
