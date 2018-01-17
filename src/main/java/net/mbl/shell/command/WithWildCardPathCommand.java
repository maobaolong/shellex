package net.mbl.shell.command;


import alluxio.AlluxioURI;
import com.google.common.base.Joiner;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * An abstract class for the commands that take exactly one path that could contain wildcard
 * characters.
 *
 * It will first do a glob against the input pattern then run the command for each expanded path.
 */
@ThreadSafe
public abstract class WithWildCardPathCommand extends AbstractShellCommand {

  protected WithWildCardPathCommand() {

  }

  /**
   * Actually runs the command against one expanded path.
   *
   * @param path the expanded input path
   * @param cl the parsed command line object including options
   * @throws IOException if the command fails
   */
  protected abstract void runCommand(String path, CommandLine cl)
      throws IOException;

  @Override
  protected int getNumOfArgs() {
    return 1;
  }

  @Override
  public void run(CommandLine cl) throws IOException {
    String[] args = cl.getArgs();
    String inputPath = args[0];


    List<String> errorMessages = new ArrayList<>();
    try {
      runCommand(inputPath, cl);
    } catch (IOException e) {
      errorMessages.add(e.getMessage());
    }

    if (errorMessages.size() != 0) {
      throw new IOException(Joiner.on('\n').join(errorMessages));
    }
  }
  public static Comparator<AlluxioURI> createAlluxioURIComparator() {
    return new Comparator<AlluxioURI>() {
      @Override
      public int compare(AlluxioURI tUri1, AlluxioURI tUri2) {
        // ascending order
        return tUri1.getPath().compareTo(tUri2.getPath());
      }
    };
  }
}
