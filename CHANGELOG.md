#Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

## 1.1.1 (TBD)

* Javadoc project name and external API links
* Project Feature DAO bounding box when not in the same projection
* Feature Overlay Query message builder, check if features are indexed first
* geopackage-core version updated to 1.1.1 - [Issue #13](https://github.com/ngageoint/geopackage-android/issues/13)
* min and max column query methods - [Issue #15](https://github.com/ngageoint/geopackage-android/issues/15)
* TileDao methods, query for tile grid or bounding box at zoom level - [Issue #14](https://github.com/ngageoint/geopackage-android/issues/14)
* Database header and integrity validation options and methods. Validate imported GeoPackage headers by default. - [Issue #16](https://github.com/ngageoint/geopackage-android/issues/16)
* Manager methods - delete all externally linked databases with missing files, retrieve database name by externally linked path
* TBD

## [1.1.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.1.0) (10-08-2015)

* NGA Table Index Extension implementation - http://ngageoint.github.io/GeoPackage/docs/extensions/geometry-index.html
* Feature Index Manager to combine existing metadata indexing with the NGA Table Index Extension
* Feature Tile improvements, including max features per tile settings and custom max feature tile drawing
* Feature and Tile DAO get bounding box method
* Feature Overlay Query for querying the features behind the drawn feature tiles

## [1.0.1](https://github.com/ngageoint/geopackage-android/releases/tag/1.0.1) (09-23-2015)

* Upgrading geopackage-core version to 1.0.1 to get added GeoPackageCache functionality

## [1.0.0](https://github.com/ngageoint/geopackage-android/releases/tag/1.0.0) (09-15-2015)

* Initial Release