#!/usr/bin/env bash
java -cp target/commons-cli-1.3.1.jar:target/alluxio-core-client-1.4.0-RC1.jar:target/shellex-1.0-SNAPSHOT.jar net.mbl.cli.ShellEx "$@"