#!/bin/bash

# Based on http://samclane.github.io/cutting-a-kenney-sprite-sheet-with-imagemagick/

set -x

mkdir -p rougelike-modern-city/Tiles
convert rougelike-modern-city/Spritesheet/roguelikeCity_magenta.png -gravity center -extent 630x477 rougelike-modern-city/Spritesheet/rougelikeCity_magenta_cropped.png
convert rougelike-modern-city/Spritesheet/rougelikeCity_magenta_cropped.png -crop 37x28-1-1@ +repage +adjoin rougelike-modern-city/Tiles/tile_%d.png


