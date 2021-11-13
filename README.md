
# Strickr application - A Stickers application for whatsapp

When updating a new package go to this parts:

    - Utilities.java    ->      descriptionsMap
    - Utilities.java    ->      PACKS_NAMES
    - Utilities.java    ->      PACKS_LIKES -> add a false
    - Utilities.java    ->      translateString(int str)
    - strings.xml       ->      <!--  Packages Name  --> fields
    - contents.json     ->      Add a new sticker area like the others but with new one credentials

Also go to Firebase FireStore:

    - Kirill's Stickers project -> FireStore -> and update this collections:
        - Stickers Packs    ->      add the new pack statistics
            - Important, make sure that the Firebase isn't adding a white space at the start
        - Start States      ->      add the default start stated you gave the pack
        - Featured Pack     ->      update the name if the new pack needs to be the featured

Before publishing a new version update the following:

    - Utilities.java    ->      public static final String CURRENT_VERSION = "6";   // This is last update version
    - build.gradle      ->      Module: app     ->      versionCode + versionName

### currently only for android
