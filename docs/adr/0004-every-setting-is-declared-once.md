# ADR 0004: Every setting is declared once

## Context

The PRD asks for every server option to be set from `/nightborn` (CFG-1), saved per world as JSON (CFG-2), shown on a Mod Menu screen that shares the commands' validation (CFG-6), and synced to clients. Written by hand, each option would appear in at least five places: the record, the JSON reader and writer, the network codec, the command tree and the screen. Nightborn needs dozens of options, and every later feature adds more (CFG-5), so those copies would drift.

## Decision

Each option is one `IntSetting` or `BoolSetting` field beside the section record that holds it, with its section, key, default and bounds. `NightbornConfig.SETTINGS` lists them all in order.

- The JSON file, the network codec, the `/nightborn config` tree, `/nightborn status` and the settings screen all walk that list. None of them names an option.
- Bounds live on the setting, so the command argument, the screen slider and the clamp on load use the same numbers.
- Section records are still immutable records, and clamp in their constructors, so a value built any other way is clamped too.
- Settings load when the server starts, from the world save. The standards allow loading them earlier, from the level storage session, for options needed before the server is built. No Nightborn option is needed that early yet, so the later, simpler hook is used until one is.
- The settings screen sends the settings it opened with and the settings it ends with. The server applies only the ones the operator changed, so a change made by command while the screen was open is kept, and it logs who changed what.
- Settings are whole numbers or toggles. Percentages and seconds are whole numbers, which keeps the codec and the commands simple.
- An option that only changes what one player sees (CFG-7), such as gore, is not a setting. It lives in a separate client store, `ClientState`, saved in the game's config folder as `nightborn/client-state.json` and never sent to the server. It shares only the file handling, `ConfigFile`, with the server settings, and the settings screen shows it in its own section that every player can change, in a world or not.

## Consequences

- Adding an option is one field, one entry in its section's list, and its two lang keys. `LangAssetsTest` fails if the lang keys are missing.
- The network codec sends the values in list order with a count first, so a client and server with different option lists refuse each other's packet instead of misreading it.
- A setting that needs a type other than a whole number or a toggle needs a new `Setting` kind, handled in each place that switches over the sealed interface. The compiler lists those places.
