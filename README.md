# Beat Game

<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/01_main_menu.png" alt="Main menu" width="400"> <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/02_in_game.png" alt="Jumping over obstacles" width="400"> <img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/03_death.png" alt="Death scene" width="400">

## Contributing

### Donations

Beat Game is an open source, GPLv3 game. It will always be freely available via F-Droid, or for anyone to build, fork, or improve via the source code.

If you wish to support the development financially, you can do so via [GitHub sponsors](https://github.com/sponsors/pserwylo).

### Reporting Issues

Please report any issues or suggest features on the [issue tracker](https://github.com/pserwylo/beat-game/issues).

### Submitting changes

Pull requests will be warmly received at [https://github.com/pserwylo/beat-game](https://github.com/pserwylo/beat-game).

## Compiling

This app uses a the libgdx library and Kotlin. It is recommended to read the [libgdx documentation to get a dev environment setup](https://libgdx.com/dev/setup/).

Alternatively, you can import the project into Android Studio and build from there.

There are some additional build processes involved in the game:
* `./gradlew :song-extract:processSongs` - Analyses the high bitrate MP3 files from `./songs/original`, extracts features to generate levels, and writes the data to `./android/assets/songs/data/`
* `./songs/downsample.sh` - Takes high bitrate MP3 files from `./songs/original` and reduce to 96Kbps, writing to `./android/assets/songs/mp3/`

Therefore, adding songs is a matter of putting a high bitrate version inside `./songs/original/SONG.mp3`, running `./gradlew :song-extract:processSongs` and then running `./songs/downsample.sh`.
This will make both a low bitrate version and a processed data file available to the game code.
