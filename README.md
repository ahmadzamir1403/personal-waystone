# Personal Waystone

A portable waystone for Minecraft **26.3**, Fabric Loader **0.19.5 or newer**,
Fabric API, and Java **25 or newer**.

Download the mod JAR from [Releases](https://github.com/ahmadzamir1403/personal-waystone/releases)
and place it in your instance's `mods` directory. Replace older Personal Waystone
JARs so only one version is installed. Install Fabric API separately.

## Controls

- **Shift + right-click a block:** bind the destination above that block.
- **Shift + right-click air:** clear the destination.
- **Right-click:** teleport to the bound destination, subject to configured cooldown
  and cross-dimension rules.

## Version 1.0.1

Fixes Shift-clicking a block accidentally clearing the destination. Block binding
consumes the client interaction, and anchor changes and other use effects run on
the server only. Existing anchors remain compatible.

## Build

With Java 25 installed, run `./gradlew build` (Windows: `./gradlew.bat build`).
The installable JAR is `build/libs/personalwaystone-1.0.1.jar`;
the `-sources.jar` is for development.

Run `python tests/check_interaction_guards.py build/libs/personalwaystone-1.0.1.jar`
to verify the compiled client/server interaction guards (requires Python 3 and
the JDK's `javap`). These checks do not replace an in-game smoke test.

## License

CC0-1.0; see [LICENSE](LICENSE).
