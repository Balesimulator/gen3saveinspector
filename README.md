# Gen III Save Inspector (Android)

A tiny read-only Android app based on the previous `gen3_iv_ev_reader.py`.

## Features
- Open `.sav` / `.srm` with Android's system file picker.
- Parse the newest valid Gen III save block.
- Auto-detect R/S/E vs FireRed/LeafGreen party layout.
- Read party and all 14 PC boxes.
- Show species, Nature (Chinese/Japanese/English), IVs, EVs, PID and checksum status.
- Standard 128 KiB saves and saves with trailing emulator RTC metadata are supported.
- Read-only: the app never writes back to the save.

## Build
Open this folder in Android Studio and build `app`, or push to GitHub and run the included **Build APK** workflow.
