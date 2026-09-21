#!/usr/bin/env bash
# Run on a CI emulator; this script is not an invitation to start local Android builds.
set -uo pipefail
original_font="$(adb shell settings get system font_scale | tr -d '\r')"
restore_device() {
  if [ "$original_font" = "null" ]; then
    adb shell settings delete system font_scale >/dev/null
  else
    adb shell settings put system font_scale "$original_font" >/dev/null
  fi
}
trap restore_device EXIT
adb shell settings put system font_scale 1.0
bash gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2
result=$?
mkdir -p device-qa
adb pull /sdcard/elevare-qa/. device-qa/
for evidence in 07-home.png 11-player.png 15-restored-paused.png 17-font-200.png 18-routines.png 19-routine-detail.png 20-lifestyle-home.png 21-pro-confirm.png 22-pro-applied.png running-normal.mp4 running-slow.mp4 yoga-entry-hold-exit.mp4; do
  if [ ! -s "device-qa/$evidence" ]; then
    echo "Missing device QA evidence: $evidence" >&2
    result=1
  fi
done
exit "$result"
