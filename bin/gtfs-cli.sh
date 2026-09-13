#!/bin/bash -e

cd scala
sbt "run gtfs $(printf "%q " "$@")"
