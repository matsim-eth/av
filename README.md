# Autonomous vehicles for MATSim

[![Build Status](https://travis-ci.org/matsim-eth/av.svg?branch=master)](https://travis-ci.org/matsim-eth/av)

This project is an extension of the MATSim traffic simulation framework:
https://github.com/matsim-org/matsim

Releases are available on [bintray](https://bintray.com/matsim-eth/matsim/av)

Snapshots are available on [packagecloud](https://packagecloud.io/eth-ivt/matsim/packages/java/ch.ethz.matsim/av-0.1.2-SNAPSHOT.jar)

Maintenance: Sebastian Hörl

## Working with the repository

- The latest changes are in the `master` branch. The version there is always a SNAPSHOT version. If you want to use it, clone the git repository, there is no SNAPSHOT repository.
- Release versions are committed to the `releases` branch. Whenever something is pushed to this branch, a release is built and pushed to Bintray (given that the new version in pom.xml is valid)
