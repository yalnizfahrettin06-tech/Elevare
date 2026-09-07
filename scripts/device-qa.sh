#!/usr/bin/env bash
set +e
adb shell settings put system font_scale 1.0
bash gradlew :app:connectedDebugAndroidTest --no-daemon --max-workers=2
result=$?
adb pull /sdcard/Android/data/com.elevare.active/files/qa device-qa
exit "$result"
