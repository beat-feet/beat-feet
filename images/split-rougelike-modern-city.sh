#!/bin/bash

# Based on http://samclane.github.io/cutting-a-kenney-sprite-sheet-with-imagemagick/

set -x

# roof_brown:     x 0-7   y 0-3
# roof_dark_grey: x 8-15  y 0-3
# roof_grey:      x 16-23 y 0-3
# roof_sand:      x 24-31 y 0-4

mkdir -p rougelike-modern-city/Tiles
# Seems to remove the alpha channel, so instead manually created a version with a 1px padding via GIMP.
# convert -verbose rougelike-modern-city/Spritesheet/roguelikeCity_magenta.png -gravity center -extent 630x477 rougelike-modern-city/Spritesheet/rougelikeCity_magenta_cropped.png
convert rougelike-modern-city/Spritesheet/rougelikeCity_cropped.png -crop 37x28-1-1@ +repage +adjoin rougelike-modern-city/Tiles/tile_%d.png


