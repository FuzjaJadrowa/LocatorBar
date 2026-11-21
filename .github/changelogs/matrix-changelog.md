# 1.3.0
## Added
- Added vertical pitch direction arrows to player markers on the locator bar.
- Added waypoint support for compasses stored inside other inventory containers (such as shulker boxes and modded containers/backpacks).
- Added tooltip to server-controlled options in the client config screen.
- [1.21.11, 26.1.2, 26.3] Added support for vanilla waypoints on the locator bar.
## Changed
- Changed classic locator bar view angle from 90° to 120° to match vanilla behavior.
- Changed project license from GPL-3.0 to PIF-1.0.
## Fixed
- Fixed HUD rendering order so the locator bar renders behind the player tab list overlay.
- Fixed waypoint unlinking synchronization between client and server, including compasses stored inside containers.
- Fixed team black color visibility on player markers.
- Fixed partial server configuration files resetting unspecified values instead of preserving default.
- Fixed other various issues.