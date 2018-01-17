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

import alluxio.client.block.AlluxioBlockStore;
import alluxio.client.block.BlockWorkerInfo;
import alluxio.client.file.FileSystem;
import alluxio.exception.AlluxioException;
import org.apache.commons.cli.CommandLine;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.List;

/**
 * Gets the worker info list of the {@link FileSystem}.
 */
@ThreadSafe
public final class GetWorkerInfoListCommand extends AbstractShellCommand {

  @Override
  public String getCommandName() {
    return "workerInfo";
  }

  @Override
  protected int getNumOfArgs() {
    return 0;
  }

  @Override
  public void run(CommandLine cl) throws IOException {
    AlluxioBlockStore alluxioBlockStore = AlluxioBlockStore.create();
    List<BlockWorkerInfo> workerInfoList = null;
    try {
      workerInfoList = alluxioBlockStore.getWorkerInfoList();
    } catch (AlluxioException e) {
      e.printStackTrace();
    }
    System.out.println("worker count : " + workerInfoList.size());
    for (BlockWorkerInfo blockWorkerInfo:workerInfoList) {
      System.out.println("netAddress   : " + blockWorkerInfo.getNetAddress());
      System.out.println("capacityBytes: " + blockWorkerInfo.getCapacityBytes());
      System.out.println("usedBytes    : " + blockWorkerInfo.getUsedBytes());
      System.out.println();
    }
  }

  @Override
  public String getUsage() {
    return "workerInfo";
  }

  @Override
  public String getDescription() {
    return "Gets the workerInfo of the Alluxio file system.";
  }
}
