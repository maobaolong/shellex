package net.mbl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
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
    private static final String DEFAULT_PATH = "/";

    String path = DEFAULT_PATH;
    private String fsAction;
    public void doTest0() {
        try {
            Path path = new Path(this.path);
            Configuration conf = new Configuration();
//            conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
//            conf.set("fs.default.name", "hdfs://192.168.1.5:8021");
            FileSystem fileSystem=path.getFileSystem(conf); //FileSystem.get(conf);
            if(fileSystem.exists(path)){
                if(fileSystem.isDirectory(path)){
                    System.out.println("Path " + this.path + " is a folder.");
                    FileStatus[] stats=fileSystem.listStatus(path);
                    //循环遍历该文件夹中的文件
                    for(int i=0;i<stats.length;i++) {
                        System.out.println(stats[i].getPath().toString());
                    }
                }else{
                    System.out.println("Path " + this.path + " is a file.");
                }
            }else{
                System.out.println("Path " + this.path + " not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showEmptyDir(String pathStr) {
        try {
            Path path = new Path(pathStr);
            Configuration conf = new Configuration();
            FileSystem fileSystem=path.getFileSystem(conf); //FileSystem.get(conf);
            if(fileSystem.exists(path)){
                if(fileSystem.isDirectory(path)){
                    FileStatus[] stats=fileSystem.listStatus(path);
                    //循环遍历该文件夹中的文件
                    if(stats == null || stats.length==0){
                        System.out.println("Empty Folder: " + path);
                    }else {
                        System.out.println("Path " + pathStr + " is a folder.");
                        for (int i = 0; i < stats.length; i++) {
                            showEmptyDir(stats[i].getPath().toString());
                        }
                    }
                }else{
//                    System.out.println("Path " + pathStr + " is a file.");
                }
            }else{
                System.out.println("Path " + pathStr + " not exist.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final RmEmptyDir ta = new RmEmptyDir();
        Options opts = new Options();
        opts.addOption("h", false, "help");
        opts.addOption("path", true, "file path. default " + DEFAULT_PATH);
        opts.addOption("fs", true, "showEmptyDir|rmEmptyDir");
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
                    }else if (cl.hasOption("fs")){
                        ta.fsAction = cl.getOptionValue("fs");
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