# Automated vehicles for MATSim

This project is an extension of the [MATSim](https://github.com/matsim-org/matsim) traffic simulation framework that adds shared automated vehicles to the simulation.

## Available versions

The AV extension is currently kept compatible with the following MATSim versions:

|MATSim              |AV version      |               |
|--------------------|-----------------|---------------|
| Weekly SNAPSHOT `12.0-2019w37`            | `1.0.1`           | [![Build Status](https://travis-ci.org/matsim-eth/av.svg?branch=master)](https://travis-ci.org/matsim-eth/av) |
| Release `11.0`       | `1.0.1-matsim11`  | [![Build Status](https://travis-ci.org/matsim-eth/av.svg?branch=master-11)](https://travis-ci.org/matsim-eth/av) |
| Release `0.10.1`     | `1.0.1-matsim10`  | [![Build Status](https://travis-ci.org/matsim-eth/av.svg?branch=master-10)](https://travis-ci.org/matsim-eth/av) |

Since we have to react to changes in the `master` branch of the [MATSim main repository](https://github.com/matsim-org/matsim) "on demand", compatibility may be "out-of-synch" for a short time until we updated to the next weekly SNAPSHOT. We recommend using the AV extension with a stable version of MATSim.

To use the AV extension you first need to add the ETH MATSim Bintray repository to your `pom.xml`:

```xml
<repository>
    <id>matsim-eth</id>
    <url>https://dl.bintray.com/matsim-eth/matsim</url>
</repository>
```

Add the following to your `pom.xml` dependencies to use the extension with version `1.0.1` and MATSim 11, for instance:

```xml
<dependency>
    <groupId>ch.ethz.matsim</groupId>
    <artifactId>av</artifactId>
    <version>1.0.1-matsim11</version>
</dependency>
```

## Repository sturcture

This repository makes use of the [GitFlow](https://nvie.com/posts/a-successful-git-branching-model/) repository model. This means that development is taking place in the `develop` branch, while the current production version can be found in the `master` branch. Note that, contrary to the basic model, we use multiple `master` branches to maintain versions of the code that are compatible with different releases of MATSim. For instance, `master-11` is compatible with MATSim 11. The `master` branch is kept compatible with the `master` branch of the [MATSim main repository](https://github.com/matsim-org/matsim). Backports are always derived from the `master` branch into the specific backport branches.

For creating the backports, the recommended workflow is as follows: Branch `backport-X` from master, add changes for compatibility, merge back `backport-X` into `master-X`. 

