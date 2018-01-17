package net.mbl.shell.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * Displays information for the path specified in args. Depends on different options, this command
 * can also display the information for all directly children under the path, or recursively.
 */
@ThreadSafe
public final class ShowEmptyDirCommand extends WithWildCardPathCommand {
  @Override
  public String getCommandName() {
    return "showEmptyDir";
  }

  @Override
  protected int getNumOfArgs() {
    return 1;
  }

  @Override
  protected Options getOptions() {
    return new Options()
        .addOption(RECURSIVE_OPTION);
  }

  /**
   * Displays information for all directories and files directly under the path specified in args.
   *
   * @param path The path as the input of the command
   * @param recursive Whether list the path recursively
   * @throws IOException when ioexception occurs
   */
  private void showEmptyDir(String path, boolean recursive)
      throws IOException {
    Path fsPath = new Path(path);
    Configuration conf = new Configuration();
    FileSystem fileSystem=fsPath.getFileSystem(conf); //FileSystem.get(conf);
    if(fileSystem.exists(fsPath)){
      if(fileSystem.isDirectory(fsPath)){
        FileStatus[] stats=fileSystem.listStatus(fsPath);
        //循环遍历该文件夹中的文件
        if(stats == null || stats.length==0){
          System.out.println(path);
        }else {
          if(recursive){
            for (int i = 0; i < stats.length; i++) {
              showEmptyDir(stats[i].getPath().toString(),recursive);
            }
          }
        }
      }else{
      }
    }else{
      System.out.println("Path " + path + " not exist.");
    }
  }


  @Override
  public void runCommand(String path, CommandLine cl) throws IOException {
    System.out.println("show empty dir " + path + (cl.hasOption("R") ? " with flag -R" :"") );
    showEmptyDir(path, cl.hasOption("R"));
  }

  @Override
  public String getUsage() {
    return "showEmptyDir [-R] <path>";
  }

  @Override
  public String getDescription() {
    return "Displays information for all empty directories directly under the specified path."
        + " Specify -R to display empty directories recursively.";
  }
}
