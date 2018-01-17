package net.mbl.shell;

import net.mbl.shell.command.ShellCommand;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * net.mbl.shell <br>
 *
 * @author maobaolong@139.com
 * @version 1.0.0
 */
public class TimeExecutor {
  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static void run(boolean debug, ShellCommand command, String... argv) throws IOException {
    CommandLine cmdline = command.parseAndValidateArgs(argv);
    if (debug){
      Date begin = new Date();
      System.out.println(df.format(begin));
      command.run(cmdline);
      Date end = new Date();
      System.out.println(df.format(end));
      float between = (end.getTime() - begin.getTime()) / 1000.0f;
      System.out.printf("diff: %f (s)\n\n" , between);
    } else {
      command.run(cmdline);
    }
  }
}
