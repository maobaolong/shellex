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
import alluxio.Constants;
import alluxio.client.ReadType;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileSystem;
import alluxio.client.file.URIStatus;
import alluxio.client.file.options.OpenFileOptions;
import alluxio.exception.AlluxioException;

import com.google.common.base.Joiner;
import com.google.common.io.Closer;
import net.mbl.shell.AlluxioShellUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Loads a file or directory in Alluxio space, makes it resident in memory.
 */
@ThreadSafe
public final class LoadCommand extends WithWildCardPathCommand {

  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private FileSystem mFileSystem = FileSystem.Factory.get();

  @Override
  public String getCommandName() {
    return "load";
  }

  @Override
  protected Options getOptions() {
    return new Options().addOption(DEBUG_OPTION);
  }

  @Override
  public void run(CommandLine cl) throws IOException {
    String[] args = cl.getArgs();
    AlluxioURI inputPath = new AlluxioURI(args[0]);

    List<AlluxioURI> paths = AlluxioShellUtils.getAlluxioURIs(mFileSystem, inputPath);
    if (paths.size() == 0) { // A unified sanity check on the paths
      throw new IOException(inputPath + " does not exist.");
    }
    Collections.sort(paths, createAlluxioURIComparator());

    List<String> errorMessages = new ArrayList<>();
    for (AlluxioURI path : paths) {
      try {
        runCommand(path.toString(), cl);
      } catch (IOException e) {
        errorMessages.add(e.getMessage());
      }
    }

    if (errorMessages.size() != 0) {
      throw new IOException(Joiner.on('\n').join(errorMessages));
    }
  }

  @Override
  protected void runCommand(String path, CommandLine cl) throws IOException {
    AlluxioURI filePath = new AlluxioURI(path);
    try {
      if (cl.hasOption(DEBUG_OPTION.getOpt())) {
        Date begin = new Date();
        System.out.println(begin);
        load(filePath);
        Date end = new Date();
        System.out.println(end);
        float between = (end.getTime() - begin.getTime()) / 1000.0f;
        System.out.printf("diff: %f (s)\n" , between);
      } else
        load(filePath);
    } catch (AlluxioException e) {
      e.printStackTrace();
    }
  }

  /**
   * Loads a file or directory in Alluxio space, makes it resident in memory.
   *
   * @param filePath The {@link AlluxioURI} path to load into Alluxio memory
   * @throws AlluxioException when Alluxio exception occurs
   * @throws IOException when non-Alluxio exception occurs
   */
  private void load(AlluxioURI filePath) throws AlluxioException, IOException {
    FileSystem fileSystem = FileSystem.Factory.get();
    URIStatus status = fileSystem.getStatus(filePath);
    if (status.isFolder()) {
      List<URIStatus> statuses = fileSystem.listStatus(filePath);
      for (URIStatus uriStatus : statuses) {
        AlluxioURI newPath = new AlluxioURI(uriStatus.getPath());
        load(newPath);
      }
    } else {
      if (status.getInMemoryPercentage() == 100) {
        // The file has already been fully loaded into Alluxio memory.
        return;
      }
      Closer closer = Closer.create();
      try {
        OpenFileOptions options = OpenFileOptions.defaults().setReadType(ReadType.CACHE_PROMOTE);
        FileInStream in = closer.register(fileSystem.openFile(filePath, options));
        byte[] buf = new byte[8 * Constants.MB];
        while (in.read(buf) != -1) {
        }
      } catch (Exception e) {
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    }
    System.out.println(filePath + " loaded");
  }

  @Override
  public String getUsage() {
    return "load <path>";
  }

  @Override
  public String getDescription() {
    return "Loads a file or directory in Alluxio space, makes it resident in memory.";
  }
}
