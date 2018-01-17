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
import alluxio.client.file.FileSystem;
import alluxio.exception.AlluxioException;

import net.mbl.shell.TimeExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Loads a file or directory in Alluxio space, makes it resident in memory.
 */
@ThreadSafe
public final class TestCommand extends WithWildCardPathCommand {

  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private FileSystem mFileSystem = FileSystem.Factory.get();
  private CountDownLatch endLatch;
  private static Lock sLock = new ReentrantLock();
  private static Condition sCondition = sLock.newCondition();
  private Map<Long,String> resultMap = new ConcurrentHashMap<>();
  private static final long DELAY_TIME = 1000L;
  @Override
  public String getCommandName() {
    return "test";
  }

  @Override
  protected Options getOptions() {
    return new Options()
        .addOption(Option.builder()
            .longOpt("host")
            .required(true)
            .hasArg(true)
            .desc("specify host to specify worker to show evictor queue.")
            .build())
        .addOption(Option.builder("S")
            .required(false)
            .hasArg(false)
            .desc("sync.")
            .build())
        .addOption(DEBUG_OPTION);
  }

  @Override
  protected void runCommand(String path, CommandLine cl) throws IOException {
    try {

      if (cl.hasOption(DEBUG_OPTION.getOpt())) {
        Date begin = new Date();
        System.out.println("test start at " + df.format(begin));
        test(path,cl);
        Date end = new Date();
        System.out.println("test end at " + df.format(end) + " include delay " + DELAY_TIME + "ms");
        float between = (end.getTime() - begin.getTime() - DELAY_TIME) / 1000.0f;
        System.out.printf("total test OK! diff: %f (s)" , between);
      } else {
        test(path,cl);
      }
    } catch (AlluxioException e) {
      e.printStackTrace();
    }
  }

  private void test(String path, CommandLine cl) throws IOException, AlluxioException {
    final String host = cl.getOptionValue("host");

    Path fsPath = new Path(path);
    Configuration conf = new Configuration();
    org.apache.hadoop.fs.FileSystem fileSystem=fsPath.getFileSystem(conf); //FileSystem.get(conf);
    if(fileSystem.exists(fsPath)){
      if(fileSystem.isDirectory(fsPath)){
        FileStatus[] stats=fileSystem.listStatus(fsPath);
        if (cl.hasOption("S")) {
          ExecutorService fixedThreadPool = Executors.newFixedThreadPool(stats.length);
          for (final FileStatus stat : stats) {
            endLatch = new CountDownLatch(stats.length);
            final int len = stats.length;
            fixedThreadPool.execute(new Runnable() {
              @Override
              public void run() {
                innerTest(host,stat,len);
              }
            });
          }
          try {
            endLatch.await();
            fixedThreadPool.shutdown();
            Thread.sleep(DELAY_TIME);
            System.out.println(resultMap);
            for(long c: resultMap.keySet()){
              System.out.printf("[%06d]=%s\n" , c,resultMap.get(c));
            }
            System.out.println("test Ok!");
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {
          for (final FileStatus stat : stats) {
            AlluxioBlockStore alluxioBlockStore = AlluxioBlockStore.create();
            System.out.println(alluxioBlockStore.getBlockList(host).size());
            TimeExecutor.run(true, new LoadCommand(),
                stat.getPath().toString());

            System.out.println(alluxioBlockStore.getBlockList(host).size());
          }
          try {
            Thread.sleep(DELAY_TIME);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println("test Ok!");
        }

      }else{
        System.out.println(path);
      }
    }else{
      System.out.println("Path " + path + " not exist.");
    }
  }

  private void innerTest(String host, FileStatus stat, int len){


    try {
//      sLock.lock();
      AlluxioBlockStore alluxioBlockStore = AlluxioBlockStore.create();
//      System.out.println(alluxioBlockStore.getBlockList(host).size());
      LoadCommand loadCommand = new LoadCommand();
      Date begin = new Date();
      TimeExecutor.run(false, loadCommand,
          stat.getPath().toString());
      endLatch.countDown();
      long count = endLatch.getCount();
      Date end = new Date();
      float between = (end.getTime() - begin.getTime()) / 1000.0f;
      String result = "test start : " + df.format(begin) + " , end : " +df.format(end) + ", diff "
          + " : " +between + " , file : " + stat.getPath();
      System.out.println(result);
      resultMap.put(count,result);
//      System.out.println(alluxioBlockStore.getBlockList(host).size());

//      endLatch.countDown();
//      System.out.printf(" task count : %d / %d .\n", (len - endLatch.getCount()) /  len);

    } catch (IOException e) {
      e.printStackTrace();
    } /*catch (AlluxioException e) {
      e.printStackTrace();
    } */finally {
//      sLock.unlock();
    }
  }
  @Override
  public String getUsage() {
    return "test [-D] [-S] --host=HOST <path>";
  }

  @Override
  public String getDescription() {
    return "test load files in Alluxio space, give a test report.";
  }
}
