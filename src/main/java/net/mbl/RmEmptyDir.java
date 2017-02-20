package net.mbl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Remove the empty Directories.
 *
 * @author maobaolong@jd.com
 * @version 1.0.0
 */
public class RmEmptyDir {
    private static final String DEFAULT_PATH =
        "/";

    String path = DEFAULT_PATH;

    public void doTest0() {
        try {
            Path path = new Path(this.path);
            Configuration configuration = new Configuration();
            FileSystem fileSystem = path.getFileSystem(configuration);
            long size = fileSystem.getFileStatus(path).getLen();
            System.out.println("size: " + size);
            FSDataInputStream inputStream = fileSystem.open(path);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final RmEmptyDir ta = new RmEmptyDir();
        Options opts = new Options();
        opts.addOption("h", false, "help");
        opts.addOption("path", true, "file path. default " + DEFAULT_PATH);
        DefaultParser parser = new DefaultParser();
        CommandLine cl;
        try {
            cl = parser.parse(opts, args);
            if (cl.getOptions().length > 0) {
                if (cl.hasOption('h')) {
                    HelpFormatter hf = new HelpFormatter();
                    hf.printHelp("Options", opts);
                    return;
                } else {
                    if (cl.hasOption("path")) {
                        ta.path = cl.getOptionValue("path");
                    }
                }
            } else {
                System.out.println("You are using default argument, use -h argument to get help.");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("path : " + ta.path);

        System.out.println("After 3 second , test will started !");
        for (int i = 0; i < 3; i++) {
            System.out.println(i);
            Thread.sleep(1000);
        }

        ta.doTest0();
    }
}