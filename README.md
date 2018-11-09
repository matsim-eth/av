# Autonomous vehicles for MATSim

[![Build Status](https://travis-ci.org/matsim-eth/av.svg?branch=master)](https://travis-ci.org/matsim-eth/av)

This project is an extension of the MATSim traffic simulation framework:
https://github.com/matsim-org/matsim

Maintenance: Sebastian Hörl

## Usage

The package can be added to the maven dependencies by adding the following repository:

```xml
<repository>
  <id>matsim</id>
  <url>http://dl.bintray.com/matsim-eth/matsim</url>
</repository>
```

And using the following dependency:

```xml
<dependency>
  <groupId>ch.ethz.matsim</groupId>
  <artifactId>av</artifactId>
  <version>0.3.6</version>
</dependency>
```
The versions of the AV extension correspond to specific versions of MATSim:

|AV Extension|Compatible MATSim|
|------------|-----------------|
| 0.3.x      | 0.11.x          |
| 0.2.x      | 0.10.1          |
| 0.1.x      | 0.10.0          |

In the respository there is a dedicated branch for 0.3.x, 0.2.x etc. Note that the latest branch makes the extension compatible with the current SNAPSHOT version of MATSim. However, it depends on a specific weekly release of MATSim (e.g. `0.11.0-2018w44`. As long as this version (as specified in the `pom.xml` here is used, things should work well). In many cases it may be possible to override the version used here with a newer weekly version or even the `-SNAPSHOT` version. If for instance changes have been made in the `DVRP` contribution or other parts, incompatibilities can arise which should be updated here.
