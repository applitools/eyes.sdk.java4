
### Added 
- Ignore displacements and enable patterns. [Trello 1628](https://trello.com/c/ejcVTEJV)
### Updated
- Use the scrollable view's X position for avoiding "clickable elements" instead of fixed location. [Trello 1871](https://trello.com/c/Dt6yaCpw)

## [4.12.0]
### Updated
- Accessibility guidelines version support [Trello 1767](https://trello.com/c/gq69woeK)

## [4.11.1] - 2020-05-4
### Fixed
- Upload viewport image to Azure cloud for visual locators. [Trello 1672](https://trello.com/c/9r6TeoVo)
### Added
- Disable SSL verification. Accept all certificates. [Trello 1771](https://trello.com/c/WPWFJFcD)

## [4.11.0] - 2020-04-26
### Updated
- Send DOM directly to Azure Storage server. [Trello 1743](https://trello.com/c/PVyhBHCh)
- Migrate post visual locators to long-running request. [Trello 1672](https://trello.com/c/9r6TeoVo)
### Added
- Eyes-Date header for all requests in the long-running task process. [Trello 1756](https://trello.com/c/zqcGscs3)
### Fixed
- Taking IOS fullpage screenshot with elements below scrollable view. [Trello 1419](https://trello.com/c/IIIKosP8)


## [4.10.0] - 2020-04-16
### Updated
-  StartSession isNew handling. [Trello 1715](https://trello.com/c/DcVzWbeR/)
### Fixed
- Changed X position for Android fullpage to avoid touch action on the elements located centered. [Trello 1645](https://trello.com/c/nSGlV1C1)

## [4.9.1] - 2020-04-10
### Fixed
- Version 4.9.0 skipped because of Maven deploy issues.
### Updated
- `com.applitools.eyes.selenium.Eyes` will use standard screenshot if viewportScreenshot fails for mobile. [Trello 1663](https://trello.com/c/HnJKeNKO)
- StartSession request now considers "isNew" flag. [Trello 1715](https://trello.com/c/DcVzWbeR)


## [4.8.0] - 2020-04-08
### Fixed 
- Reset contentSize if no active scrollable element. [Trello 1371](https://trello.com/c/M90Imf7t) 
- Getting device pixel ratio for `com.applitools.eyes.selenium.Eyes`.
### Updated
- WebView full page screenshot with `com.applitools.eyes.selenium.Eyes`. [Trello 1663](https://trello.com/c/HnJKeNKO)

## [4.7.0] - 2020-04-03
### Fixed
- Update 'getElementRegion' for iOS. [Trello 1702](https://trello.com/c/fz5vDBEx)
### Added
- Support scrollable views from AndroidX libraries with EyesAppiumHelper library. [Trello 1701](https://trello.com/c/BIZSYiDN)
- Basic tests for iOS table and scroll views.

## [4.6.1] - 2020-03-29
### Fixed
- Check for XCUIElementTypeTable child count to avoid IndexOfBoundException. [Trello 1670](https://trello.com/c/If5sMKeW)

## [4.6.0] - 2020-03-24
### Fixed
- Correct element location and size when using layout(), ignore(), strict(), content(), floating() and accessibility(). [Trello 1601](https://trello.com/c/mN1R9zVZ)
- Check for XCUIElementTypeScrollView child count to avoid IndexOfBoundException.
### Updated
- Additional logs (agentId, session details, scrollable element type). [Trello 1663](https://trello.com/c/HnJKeNKO)

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
