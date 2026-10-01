# Gen III Save Inspector (Android)

An Android app based on the previous `gen3_iv_ev_reader.py`.

## Features
- Open `.sav` / `.srm` with Android's system file picker.
- Parse the newest valid Gen III save block.
- Auto-detect R/S/E vs FireRed/LeafGreen party layout.
- Read party and all 14 PC boxes.
- Show species, gender, level, Nature (Chinese/Japanese/English), IVs, EVs,
  calculated stats, PID and checksum status.
- Standard 128 KiB saves and saves with trailing emulator RTC metadata are supported.
- Long-press a Pokémon or Egg to select one or more entries for deletion.
- Before a deletion is written back, the app creates and verifies a full backup
  with `_backup` inserted before the original file extension.

## Build
Open this folder in Android Studio and build `app`, or push to GitHub and run the included **Build APK** workflow.
