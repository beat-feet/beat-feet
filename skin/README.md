# libgdx Scene2D Skin Details

## Fonts

There are two sets of fonts:
* Default "Kenney" fonts (see `/images/kenney-fonts`)
* Fallback "Noto" fonts (A TTF available on most linux distros and Windows)

The fallback is used for languages which require glyphs not spuported by the Kenney fonts. The list
of such fonts is available from `Assets.notoLocales()`. If such a locale is in use, then the
`Assets.Styles.Labels` and `Assets.Styles.TextButtons` will change their styles to those with a
which have a `-noto` suffix (e.g. `huge` vs `huge-noto`). The skin contains button and label styles
for these noto varieties which extend their default counterparts, and the only difference is the font.

The skin also contains the noto fonts.

Generating all glyphs from the Noto TTF is a bit excessive (there are almost 900 glyphs). Therefore,
the `texture-packer` project is responsible for looping over all `*.properties` files and finding
every character in use. This is what is then used to generate a font, put it in the skin directory,
and then we need to open the skin in SkinComposer and re-export it again to ensure that the Noto
bitmap fonts are included in the skins texture atlas.

If a new language is added, or translations are modified, the above process should be repeated in
case there are any new characters that require glyphs.

Some considerations when adding languages and regenerating fonts:
* The font data and .png files are first created by running `./gradlew :texture-packer:pack-textures`
* This writes files into the `./skin/` directory.
* Using the libgdx SkinComposer tool, open the `.scmp` file in `./skin/`.
* Warning: If the texture packer needed extra `.png` files now that new characters have been added,
  you will want to manually edit the `.scmp` file and make sure that the corresponding `noto_mono_*.png`
  files are accounted for. Do a git diff to see what files have been modified and check all are in
  the `.scmp` file.
* From SkinComposer, export the skin to the `./android/assets` directory.