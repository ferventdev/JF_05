package t01;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

/**
 * Created by Aleksandr Shevkunenko on 14.07.2017.
 */
public class FileViewer {
    static final String COMMAND_PREVIEW = "You can use the following commands:\n" +
            "pwd - to see the current (working) directory);\n" +
            "ls - to see the content of the current directory;\n" +
            "cat - to see the content of the text file;\n" +
            "cd - to change the current directory;\n" +
            "help - to see this prompt again;\n" +
            "exit - to quit from this program.";

    static Path workingDirectory = Paths.get(System.getProperty("user.dir"));



    public static void main(String[] args) {
        try (Scanner reader = new Scanner(System.in)) {
            System.out.println(COMMAND_PREVIEW);
            for (String input = null; ; ) {
                System.out.print("$ ");
                input = reader.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;
                if (input.isEmpty()) continue;
                parseCommand(input);
            }
        }
    }



    private static void parseCommand(String input) {
        assert input != null : "The input string mustn't be null!";

        switch (input.toLowerCase()) {
            case "help":
                System.out.println(COMMAND_PREVIEW);
                return;
            case "pwd":
                System.out.println(getCurrentDirectory());
                return;
            case "ls":
                System.out.println(getCurrentDirectoryContent());
                return;
        }

        String[] terms = input.split("\\s+");
        assert terms.length != 0 : "The input string mustn't be empty!";

        switch (terms[0].toLowerCase()) {
            case "help":
                break;
            case "cat":
                printTextFileContent(terms);
                break;
            case "cd":
                changeCurrentDirectory(terms);
                break;
            default:
                System.out.println("Such command doesn't exist, ot its using is incorrect.");
                System.out.println("Enter - help <COMMAND> - to know how to use the command.");
        }
    }

    public static void setWorkingDirectory(Path workingDirectory) {
        FileViewer.workingDirectory = workingDirectory;
    }

    private static void changeCurrentDirectory(String[] terms) {
        if (terms.length != 2) {
            System.out.println("This command requires one argument.");
            return;
        }

        Path dirname = normalizePath(terms[1]);
        if (dirname == null) return;

        if (!Files.isDirectory(dirname)) {
            System.out.println("There is no such directory, or it's inaccessible.");
            return;
        }

        String prevDir = getCurrentDirectory();
        setWorkingDirectory(dirname);
        if (!prevDir.equals(getCurrentDirectory()))
            System.out.printf("The current directory was changed to: %s%n", getCurrentDirectory());
    }

    private static void printTextFileContent(String[] terms) {
        if (terms.length < 2 || terms.length > 3) {
            System.out.println("This command requires either one or two arguments.");
            return;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return;

        if (!Files.isReadable(filename) || !Files.isRegularFile(filename)) {
            System.out.println("There is no such file to view, or it can't be read.");
            return;
        }

        Charset charset = null;
        try {
            if (terms.length == 3) charset = Charset.forName(terms[2]);
            else charset = Charset.defaultCharset();
        } catch (IllegalArgumentException e) {
            System.out.println("You've entered the wrong or unsupported charset.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filename, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.flush();
        } catch (SecurityException | IOException e) {
//            e.printStackTrace();
            System.out.println("Unfortunately, an error occured while reading this text file.");
            return;
        }
    }

    private static Path normalizePath(String aPath) {
        if (aPath.startsWith("..")) {
            Path parent = Paths.get(getCurrentDirectory()).getParent();
            if (parent == null) {
                System.out.println("The current directory doesn't have a parent directory.");
                return null;
            }
            return Paths.get(parent.toString(), aPath.substring(2));
        } else if (aPath.startsWith(".")) return Paths.get(getCurrentDirectory(), aPath.substring(1));
        else return Paths.get(aPath);
    }

    static String getCurrentDirectoryContent() {
        File[] filesAndDirs = workingDirectory.toFile().listFiles();
        List<String> files = new ArrayList<>();
        files.add("Files:");
        List<String> dirs = new ArrayList<>();
        dirs.add("Directories:");
        for (File f : filesAndDirs) {
            if (f.isDirectory()) dirs.add("\t" + f.getName());
            if (f.isFile()) files.add("\t" + f.getName());
        }
        dirs.addAll(files);
        return String.join("\n", dirs);
    }

    static String getCurrentDirectory() {
        return workingDirectory.toString();
    }
}
