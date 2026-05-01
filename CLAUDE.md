# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Locator Bar is a Minecraft mod that implements a player locator bar HUD. It supports both Fabric and NeoForge mod loaders for different Minecraft versions separate with branches on Git. The mod displays player heads, waypoints (via lodestone compasses), and world direction markers on a directional bar.

## Build Commands

### Building for Fabric
```bash
./gradlew :fabric:compileJava
```

### Building for NeoForge
```bash
./gradlew :neoforge:compileJava
```

### Building both loaders
```bash
./gradlew build
```

### Running the mod in development
```bash
# Fabric client
./gradlew :fabric:runClient

# NeoForge client
./gradlew :neoforge:runClient
```

## Architecture

### Multi-Loader Structure
The project maintains separate source trees for Fabric and NeoForge in `fabric/src/` and `neoforge/src/`. Code is duplicated between loaders rather than shared. When making changes, update both loader implementations unless the change is loader-specific.

### Loader-Specific Entry Points
- **Fabric**: `fabric/src/main/java/pl/fuzjajadrowa/locatorbar/fabric/LocatorBarFabric.java` - Implements `ModInitializer` and `ClientModInitializer`
- **NeoForge**: `neoforge/src/main/java/pl/fuzjajadrowa/locatorbar/neoforge/LocatorBarNeoForge.java` - Uses `@Mod` annotation and event bus

### Rendering Architecture
The mod uses a two-tier rendering system:
- `LocatorBarHudRenderer` - Main entry point that delegates based on configured style
- `ReworkedLocatorBarHudRenderer` - Custom top HUD style (configurable position, scale, view angle)
- `ClassicLocatorBarHudRenderer` - Vanilla-like style near XP bar (fixed position, 90° view angle)

Both renderers share similar logic for:
- Direction markers (N, S, E, W)
- Player head markers with distance-based alpha fading
- Waypoint markers from lodestone compasses in player inventory

### Mixin Differences
- **Fabric**: Uses `GuiMixin` to hook into GUI rendering via `render` method injection
- **NeoForge**: Uses event bus `RenderGuiEvent.Post` for rendering (no GUI mixin needed)
- Both loaders use `CompassItemMixin` for waypoint metadata and `OptionsScreenMixin` for config screen integration

### Configuration System
Configuration is stored in `config/locatorbar.json` and managed by `LocatorBarConfig.java`. The config uses Gson for JSON serialization with validation and clamping for numeric values. Key configuration options include:
- Style (OFF, REWORKED, CLASSIC)
- Scale, offset, and view angle (reworked style only)
- Coordinates display and format
- Day counter display
- World directions, player heads, and waypoints visibility
- Individual scale settings for each element type

### Waypoint System
Waypoints are created using lodestone compasses. When a player uses a lodestone compass on a lodestone, `CompassItemMixin` injects metadata via `WaypointData.java`:
- Owner UUID (player who created the waypoint)
- Waypoint ID (unique identifier)
- Index (for ordering)
- Custom color and symbol (optional)
- Hidden flag

Waypoint markers are rendered only for the owner and only in the same dimension.

### Resource Locations
All mod resources use the namespace `locatorbar`. Textures are located in `assets/locatorbar/textures/gui/`.

### Code Duplication
When implementing features, update both `fabric/src/` and `neoforge/src/` unless the feature is loader-specific. The shared packages (`client`, `config`, `waypoint`) contain identical code in both loader directories.

### Testing
The project does not currently have automated tests. Manual testing is done by running the mod in development clients.

### Release Process
Releases are triggered by Git tags via GitHub Actions (`tag-release.yml`). The workflow builds both loaders and creates releases with appropriate artifacts.