# Gemini Marker Assistant Proposal

## Feature Idea

Add a Gemini-powered marker assistant that helps users turn plain latitude, longitude, and marker titles into useful map notes.

The first version can stay small:

- User enters latitude, longitude, and a short marker title.
- User taps a "Generate note with Gemini" action.
- Gemini returns a concise marker note with suggested context, a visit reminder, and nearby-place questions the user may want to answer manually.
- The generated note is shown for review before it is saved with the marker.

This fits the current app because the core workflow already lets users add and persist custom markers.

## Why This Is Useful

The current marker form stores only a title and coordinates. Gemini can make the marker more useful without changing the map flow by helping users create richer notes such as:

- "Good meetup point near the main gate. Confirm parking before leaving."
- "Potential food stop. Check opening hours and crowd level."
- "Saved travel location. Ask Gemini for a short itinerary around this area."

## Suggested Implementation Plan

1. Add a marker note field to `MarkerData`.
2. Add an optional note text area to the add-marker card.
3. Add a "Generate note with Gemini" button near the title field.
4. Use Firebase AI Logic with the Gemini Developer API for the model call.
5. Keep the prompt narrow and privacy-aware:

```text
Create a short, practical map marker note.
Title: {markerTitle}
Latitude: {latitude}
Longitude: {longitude}

Return:
- one short note under 30 words
- one useful reminder
- one question the user should verify manually
Do not claim live place facts unless provided by the user.
```

6. Show the generated content in the note field before saving so the user remains in control.

## Follow-Up Enhancements

- Generate marker title suggestions from user-entered notes.
- Summarize all saved markers into a one-day route plan.
- Let users ask Gemini to group saved markers by purpose, such as food, work, travel, or errands.
- Add a "Copy Gemini prompt" fallback for builds where Firebase/Gemini credentials are not configured yet.

## Safety And Privacy Notes

- Do not send precise location data to Gemini without a visible user action.
- Keep the generated note editable before saving.
- Do not present Gemini output as live business, traffic, or safety data.
- Avoid storing API keys in source control; use local configuration or Firebase setup instructions.

## Current Android Gemini Direction

Android's Gemini Developer API documentation currently recommends using Firebase AI Logic for accessing Gemini Pro and Flash models from Android apps. A production implementation should follow that setup instead of committing API keys or secrets to this repository.
