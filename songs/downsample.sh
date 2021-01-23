#!/bin/bash
#
# Creates a lower bitrate version of each MP3 in the ./original/ folder.
#
# Some originals are up to 320kbps which is a bit excessive considering the resulting filesize of
# the .apk for this game. They are downsampled to 96kbps.
#
# Once downsampled, they are copied to the android/assets/songs/mp3 directory.
#
# Note: It would be preferable to symlink that directory to one in this top level songs/ directory,
#   except that libgdx seems to by default do it the other way around... each non-android project
#   symlinks the android assets directory to their folders, rather than having one top level assets
#   dir. I just assume this is because the android build tools don't like symlinks without evidence).
#
# Note: To generate fun game levels from the songs, the `song-extract` gradle project operates on
#   the original MP3s, not the downsampled ones. Therefore the resulting data that the `song-extract`
#   gradle project dumps in android/assets/songs/data are of a better quality for more fun levels.
#

DIR=$(dirname "$0")
SRC_DIR="$DIR/original"
ASSET_DIR="$DIR/../android/assets/songs"
DOWNSAMPLED_DIR="$ASSET_DIR/mp3"

mkdir -p "$DOWNSAMPLED_DIR"

for MP3_PATH in $(ls "$SRC_DIR"/*.mp3)
do
    MP3_NAME=$(basename "$MP3_PATH")
    DEST_FILE="$DOWNSAMPLED_DIR/$MP3_NAME"

    if [[ ! -f "$DEST_FILE" || $* == *--force* ]]
    then
        lame --mp3input -b 96 "$MP3_PATH" "$DEST_FILE"
    else
        echo "Skipping $MP3_NAME, already exists (use --force to overwrite)"
    fi
done
