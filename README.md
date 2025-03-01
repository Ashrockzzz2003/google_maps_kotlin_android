## Google Maps SDK for Android

> [!Important]
> The project is currently at its very basic stage, and there are many more ideas and features that have yet to be explored. It’s possible that we could integrate `Gemini` in some way to enhance a feature that could potentially become a feature in Google Maps.

This repo is to explore the Google Maps SDK for Android by trying it out in an Android App powered by `Kotlin`.

## Demo

https://github.com/user-attachments/assets/fcbb94d2-04a1-4acc-97eb-a0485ce6f1fa

## Features Implemented

> [!Note]
> For the app to build successfully, replace the `<YOUR_MAPS_API_KEY>` placeholder in [`AndroidManifest.xml`](https://github.com/Ashrockzzz2003/google_maps_kotlin_android/blob/main/app/src/main/AndroidManifest.xml) with your Google Maps API Key.

- [x] Load Google Maps.
- [x] Add markers based on `latitude` and `longitude` in map via a form.
- [x] Remove added markers after a confirmation on clicking a marker. 
- [x] Get current live location(GPS) and show in map.
- [x] Request permission for current location(GPS).
- [x] 3 different map views available. (Normal, Terrain, Satellite).
