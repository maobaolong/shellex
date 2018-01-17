## Description

A tool to run some shellex command.

It support local filesystem, hdfs and alluxio filesystem.

## How to install it

```bash
$ mvn install
```

## How to use it

You can use the `shellex.sh` to execute the program. use `-h` argument to get more help 
information. If you want to access filesystem on hdfs, you must give the schema `hdfs` 
and you must use activeNameNodeIp:port, such as hdfs://192.168.1.2:8021/user/test/dir.

```bash

$ ./shellex.sh -h

```
