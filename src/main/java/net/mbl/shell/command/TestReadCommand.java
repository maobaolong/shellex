/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package net.mbl.shell.command;

import alluxio.Constants;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import java.io.EOFException;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;
/**
 * Loads a file or directory in Alluxio space, makes it resident in memory.
 */
@ThreadSafe
public final class TestReadCommand extends WithWildCardPathCommand {

  @Override
  public String getCommandName() {
    return "testRead";
  }

  @Override
  protected Options getOptions() {
    return new Options()
        .addOption(Option.builder()
            .longOpt("position")
            .required(false)
            .hasArg(true)
            .desc("the read position.")
            .build())
        .addOption(DEBUG_OPTION);
  }

  @Override
  protected void runCommand(String path, CommandLine cl) throws IOException {
    long position = 0;
    if (cl.hasOption("position")) {
      position = Long.parseLong(cl.getOptionValue("position"));
    }
    System.out.println("position = " + position);
    Path fsPath = new Path(path);
    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fileSystem = fsPath.getFileSystem(conf);
    try(FSDataInputStream inputStream = fileSystem.open(fsPath)) {
      FileStatus fileStatus = fileSystem.getFileStatus(fsPath);
      byte[] buffer = new byte[8 * Constants.MB];
      long readLen = (fileStatus.getLen() - position) > buffer.length ? buffer.length : fileStatus.getLen() - position;
      inputStream.readFully(position, buffer, 0, (int)readLen);
    } catch (EOFException e) {
      e.printStackTrace();
    }
  }


  @Override
  public String getUsage() {
    return "testRead [--position=POST] <path>";
  }

  @Override
  public String getDescription() {
    return "test read files in Alluxio space, give a test report.";
  }
}
