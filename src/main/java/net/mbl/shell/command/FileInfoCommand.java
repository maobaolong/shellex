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

import alluxio.AlluxioURI;
import alluxio.client.block.AlluxioBlockStore;
import alluxio.client.file.FileSystem;
import alluxio.client.file.URIStatus;
import alluxio.exception.AlluxioException;
import alluxio.exception.InvalidPathException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * Displays the file's all blocks info.
 */
@ThreadSafe
public final class FileInfoCommand extends WithWildCardPathCommand {
  private static final String OPTION_BLOCK_ID = "blockId";
  private static final String OPTION_BLOCK_ID_DEFAULT_VALUE = "0";
  private FileSystem mFileSystem = FileSystem.Factory.get();

  @Override
  public String getCommandName() {
    return "fileInfo";
  }

  @Override
  protected Options getOptions() {
    return new Options().addOption(Option.builder()
        .longOpt(OPTION_BLOCK_ID)
        .required(false)
        .hasArg(true)
        .desc("specify block id to filter file info.")
        .build());
  }

  private boolean findAndRunFileInfoCmd(URIStatus status, long blockId) throws IOException,
      AlluxioException {
    if (status.isFolder()) {
      for (URIStatus childStatus : mFileSystem.listStatus(new AlluxioURI(status.getPath()))) {
        if (findAndRunFileInfoCmd(childStatus, blockId)) {
          return true;
        }
      }
    } else {
      if (status.getBlockIds().contains(blockId)) {
        runFileInfoCmd(status);
        return true;
      }
    }
    return false;
  }

  private void runFileInfoCmd(URIStatus status) throws IOException {
    System.out.println(status);
    System.out.println("Containing the following blocks: ");
    AlluxioBlockStore blockStore = AlluxioBlockStore.create();
    for (long blockId : status.getBlockIds()) {
      System.out.println(blockStore.getInfo(blockId));
    }
  }

  @Override
  protected void runCommand(String strPath, CommandLine cl) throws IOException {
    try {
      AlluxioURI path = new AlluxioURI(strPath);
      URIStatus status = mFileSystem.getStatus(path);
      if (cl.hasOption(OPTION_BLOCK_ID)) {
        findAndRunFileInfoCmd(status,
            Long.parseLong(cl.getOptionValue(OPTION_BLOCK_ID, OPTION_BLOCK_ID_DEFAULT_VALUE)));
      } else {
        if (status.isFolder()) {
          throw new InvalidPathException(path + " is a directory path so does not have file blocks.");
        }
        runFileInfoCmd(status);
      }
    }catch(AlluxioException e){
      e.printStackTrace();
    }

  }

  @Override
  public String getUsage() {
    return "fileInfo [--blockId=BLOCKID] <path>";
  }

  @Override
  public String getDescription() {
    return "Displays all block info for the specified file.";
  }
}
