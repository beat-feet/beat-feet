# Beat Game

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80px">](https://f-droid.org/app/com.serwylo.beatgame)

[![Liberapay receiving](https://img.shields.io/liberapay/receives/BeatGame)](https://liberapay.com/BeatGame/donate)
[![F-Droid version](https://img.shields.io/f-droid/v/com.serwylo.beatgame)](https://f-droid.org/packages/com.serwylo.beatgame/)

<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/01_main_menu.png" alt="Main menu" width="400"> <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/02_in_game.png" alt="Jumping over obstacles" width="400"> <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/03_death.png" alt="Death scene" width="400">

**Jump your way through levels, each automatically generated from the beat of the music.**

Hit too many obstacles, and you will die. Successfully jump from obstacle to obstacle, and watch your score soar! Can you get to the end?

Play through dozens of levels, or use your own MP3 files to generate custom worlds.

Each level consists of obstacles that correspond to the music playing in the background. Every time you play the same level, the size of the obstacles will be the same, but the world will have a different style. No two sessions will look the same.

This is still in an early stage and experimental, so any feedback is very welcome at https://github.com/beat-game/beat-game/discussions.

## Contributing

### Donations

Beat Game is an open source, GPLv3 game. It will always be freely available via F-Droid, or for anyone to build, fork, or improve via the source code.

If you wish to support the development financially, donations are welcome via:

* [Liberapay](https://liberapay.com/BeatGame/donate)
* [GitHub sponsors](https://github.com/sponsors/pserwylo)

### Reporting Issues

Please report any issues or suggest features on the [issue tracker](https://github.com/beat-game/beat-game/issues).

### Submitting changes

Pull requests will be warmly received at [https://github.com/beat-game/beat-game](https://github.com/beat-game/beat-game).

## Compiling

This app uses a the libgdx library and Kotlin. It is recommended to read the [libgdx documentation to get a dev environment setup](https://libgdx.com/dev/setup/).

Alternatively, you can import the project into Android Studio and build from there.

There are some additional build processes involved in the game:
* `./gradlew :song-extract:processSongs` - Analyses the high bitrate MP3 files from `./songs/original`, extracts features to generate levels, and writes the data to `./android/assets/songs/data/`
* `./songs/downsample.sh` - Takes high bitrate MP3 files from `./songs/original` and reduce to 96Kbps, writing to `./android/assets/songs/mp3/`

Therefore, adding songs is a matter of putting a high bitrate version inside `./songs/original/SONG.mp3`, running `./gradlew :song-extract:processSongs` and then running `./songs/downsample.sh`.
This will make both a low bitrate version and a processed data file available to the game code.
