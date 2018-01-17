package net.mbl.shell.command;

import net.mbl.cli.ShellEx;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;

/**
 * An interface for all the commands that can be run from {@link ShellEx}.
 */
public interface ShellCommand {

  /**
   * Gets the command name as input from the shell.
   *
   * @return the command name
   */
  String getCommandName();

  /**
   * Parses and validates the arguments.
   *
   * @param args the arguments for the command, excluding the command name
   * @return the parsed command line object. If the arguments are invalid, return null
   */
  CommandLine parseAndValidateArgs(String... args);

  /**
   * Runs the command.
   *
   * @param cl the parsed command line for the arguments
   * @throws IOException when io exception occurs
   */
  void run(CommandLine cl) throws IOException;

  /**
   * @return the usage information of the command
   */
  String getUsage();

  /**
   * @return the description information of the command
   */
  String getDescription();
}
