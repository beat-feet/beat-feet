# Files stored by BeatFeet

BeatFeet stores many different files. For my own sanity, I needed to document them here to better understand.

| File                                | GDX file type ([docs](https://libgdx.com/wiki/file-handling#file-storage-types)) | Path                                   |
|-------------------------------------|----------------------------------------------------------------------------------|----------------------------------------|
| Built in level - Data               | Internal                                                                         | `songs/data/NAME.json`                 |
| Built in level - MP3                | Internal                                                                         | `songs/mp3/NAME.mp3`                   |
| Metadata about users custom world   | Local                                                                            | `custom-world/world.json`              |
| Custom level - Data                 | Local                                                                            | `custom-world/MP3_NAME.json`           |
| Custom level - MP3                  | Local                                                                            | `custom-world/MP3_NAME.mp3`            |
| Metadata about all available worlds | Local                                                                            | `.cache/worlds/worlds.json`            |
| Metadata about individual worlds    | Local                                                                            | `.cache/worlds/WORLD_ID/world.json`    |
| Downloaded level - Data             | Local                                                                            | `.cache/worlds/WORLD_ID/LEVEL_ID.mp3`  |
| Downloaded level - MP3              | Local                                                                            | `.cache/worlds/WORLD_ID/LEVEL_ID.json` |
