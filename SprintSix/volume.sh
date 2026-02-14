#!/bin/bash

# ===============================
# Local Volume Mount/Unmount Script
# ===============================

DEVICE="/dev/xvdf"        # Change if needed
MOUNT_POINT="/mnt/ebs"    # Change if needed

ACTION=$1

if [ -z "$ACTION" ]; then
  echo "Usage: ./volume.sh mount|unmount"
  exit 1
fi

mount_volume() {

  echo "Checking if device exists..."

  if [ ! -b "$DEVICE" ]; then
    echo "Device $DEVICE not found."
    echo "Make sure you attached the volume from AWS console."
    exit 1
  fi

  echo "Creating mount directory..."
  sudo mkdir -p $MOUNT_POINT

  echo "Mounting volume..."
  sudo mount $DEVICE $MOUNT_POINT

  echo "Mounted successfully at $MOUNT_POINT"
  lsblk
}

unmount_volume() {

  echo "Unmounting volume..."
  sudo umount $MOUNT_POINT

  if [ $? -eq 0 ]; then
    echo "Unmount successful."
  else
    echo "Unmount failed. Check if volume is in use."
    exit 1
  fi
}

if [ "$ACTION" == "mount" ]; then
  mount_volume
elif [ "$ACTION" == "unmount" ]; then
  unmount_volume
else
  echo "Invalid option. Use mount or unmount."
fi
