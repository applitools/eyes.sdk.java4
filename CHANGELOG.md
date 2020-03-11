
### Fixed
- Correct element location and size when using layout(), ignore(), strict() and content(). [Trello 1601](https://trello.com/c/mN1R9zVZ)

## [4.5.3] - 2020-02-28
### Fixed 
- Removed deprecated Sonatype parent POM. [Trello 1556](https://trello.com/c/EGpks4Pv)

## [4.5.0] - 2020-02-01
### Added 
- Long-running request implementation. [Trello 1516](https://trello.com/c/GThsXbIL)
- Upload images directly to cloud storage. [Trello 1461](https://trello.com/c/1V5X9O37)

## [4.4.0] - 2020-01-23
### Fixed
- ClassCastException while getting status bar height. [Trello 1478](https://trello.com/c/RuPL3v4v)
- Setting the batch sequence name on session start. [Trello 1484](https://trello.com/c/eJPrunMV)
- checkRegion() with selector for iOS and Android [Trello 1419](https://trello.com/c/IIIKosP8)
### Added
- Print visual locator screenshot URL to the logs [Trello 1439](https://trello.com/c/7JAqWkaz)

## [4.3.0] - 2019-12-23
### Fixed
- Fullpage screenshot with RecyclerView on Android. [Trello 1416](https://trello.com/c/cpHFs1Zr), [Trello 953](https://trello.com/c/3UOEHgCl)
### Added
- Debug screenshots for visual locators. [Trello 1429](https://trello.com/c/CUebKSdY)
- Additional logs into BaseVisualLocatorProvider. [Trello 1401](https://trello.com/c/6MyKH9iR)
- Locator IDs into debug screenshot file name. [Trello 1438](https://trello.com/c/9lxK5XZv)

## [4.2.1] - 2019-11-1
### Fixed
- Send viewport screenshot on each locators call. [Trello 1208](https://trello.com/c/tDVd9f7K/1208-visual-locators-returns-empty-locator)
- Send device name when session is created. [Trello 125](https://trello.com/c/ekZqajRU/1184-device-name-not-set-or-set-to-desktop-when-running-an-appium-test)
- Catch NullPointerException if there no visual locators for provided name. [Trello 1222](https://trello.com/c/jhDnsp9q/1222-java-appium-4-visual-locators-nullpointerexception)

## [4.2.0] - 2019-10-04
### Added 
- Added 'Visal Locators' API.

## [4.1.1] - 2019-10-05
### Fixed
- Renamed `EyesAppiumTarget` to `Target`, imported from `com.applitools.eyes.appium`

## [4.1.0] - 2019-10-04
### Added 
- This CHANGELOG file.
- `cut()` API to Target.
