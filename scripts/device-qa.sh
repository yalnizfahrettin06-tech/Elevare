#!/usr/bin/env bash
set +e
adb shell settings put system font_scale 1.0
bash gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2
result=$?
adb pull /sdcard/elevare-qa device-qa
if [ ! -s device-qa/06-home.png ]; then exit 1; fi
exit "$result"
