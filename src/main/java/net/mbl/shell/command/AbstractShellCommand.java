package net.mbl.shell.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The base class for all the {@link ShellCommand} classes. It provides a default argument
 * validation method and a place to hold the client.
 */
@ThreadSafe
public abstract class AbstractShellCommand implements ShellCommand {

  protected static final Option RECURSIVE_OPTION =
      Option.builder("R")
            .required(false)
            .hasArg(false)
            .desc("recursive")
            .build();
  protected static final Option FORCE_OPTION =
      Option.builder("f")
          .required(false)
          .hasArg(false)
          .desc("force")
          .build();
  protected static final Option DEBUG_OPTION =
      Option.builder("D")
          .required(false)
          .hasArg(false)
          .desc("debug")
          .build();
  protected static final Option LIST_DIR_AS_FILE_OPTION =
      Option.builder("d")
          .required(false)
          .hasArg(false)
          .desc("list directories as plain files")
          .build();
  protected AbstractShellCommand() {
  }

  /**
   * Checks if the arguments are valid.
   *
   * @param args the arguments for the command, excluding the command name and options
   * @return whether the args are valid
   */
  protected boolean validateArgs(String... args) {
    boolean valid = args.length == getNumOfArgs();
    if (!valid) {
      System.out.println(getCommandName() + " takes " + getNumOfArgs() + " arguments, " + " not "
          + args.length + "\n");
    }
    return valid;
  }

  /**
   * Gets the expected number of arguments of the command.
   *
   * @return the number of arguments
   */
  protected abstract int getNumOfArgs();

  /**
   * Gets the supported Options of the command.
   *
   * @return the Options
   */
  protected Options getOptions() {
    return new Options();
  }

  @Override
  public CommandLine parseAndValidateArgs(String... args) {
    Options opts = getOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;

    try {
      cmd = parser.parse(opts, args, true /* stopAtNonOption */);
    } catch (ParseException e) {
      // TODO(ifcharming): improve the error message when an unregistered option appears
      System.err.println("Unable to parse input args: " + e.getMessage());
      return null;
    }

    if (!validateArgs(cmd.getArgs())) {
      return null;
    }
    return cmd;
  }
}
