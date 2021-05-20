#!/bin/bash
#
# Show all unique characters in each of the i18n files.
# Used to generate a backup font for use with languages that are not supported by the default bitmap font.
#

# https://stackoverflow.com/a/387704
cat android/assets/i18n/*.properties | sed -e "s/./\0\n/g" | sort -u | while read c; do echo -n "$c" ; done
echo ""

