#!/bin/bash
#
# This script installs a GPX trace file onto an emulator or device. The specified trace file is
# copied to the app's external files directory (determined by the provided package name).
#
# Usage:
#   install-gpx-trace.sh <trace file> <package name>
#
# Example:
#   install-gpx-trace.sh lost.gpx com.example.myapp

if [[ $# -eq 0 ]]; then
    echo "Usage: ${0} <trace-file> <package-name>"
    exit 1
fi

TRACE_FILE=$1

if [[ -z "$2" ]]; then
    PACKAGE_NAME="com.example.lost"
else
    PACKAGE_NAME=$2
fi

adb push "$1" /sdcard/Android/data/"$PACKAGE_NAME"/files/
