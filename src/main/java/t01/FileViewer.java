package t01;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Aleksandr Shevkunenko on 14.07.2017.
 */
public class FileViewer {

    static final String COMMAND_PREVIEW = "You can use the following commands:\n" +
            "pwd   - to see the current (working) directory;\n" +
            "ls    - to see the content of the current directory;\n" +
            "cat   - to see the content of the text file;\n" +
            "cd    - to change the current directory;\n" +
            "mk    - to create an empty file in the current directory;\n" +
            "rm    - to remove a file;\n" +
            "help  - to see this prompt again;\n" +
            "exit  - to quit from this program.";

    static Path workingDirectory = Paths.get(System.getProperty("user.dir"));


    // main
    public static void main(String[] args) {
        try (Scanner reader = new Scanner(System.in)) {
            System.out.println(COMMAND_PREVIEW);
            for (String input = null; ; ) {
                System.out.print("$ ");
                input = reader.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) break;
                if (input.isEmpty()) continue;
                parseCommand(input, reader);
            }
        }
    }


    // tries to recognize the command and invoke the appropriate command handler
    private static void parseCommand(String input, Scanner reader) {
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
            case "mk":
                createNewFile(terms);
                break;
            case "rm":
                removeFile(terms, reader);
                break;
            default:
                System.out.println("Such command doesn't exist, ot its using is incorrect.");
                System.out.println("Enter - help <COMMAND> - to know how to use the command.");
        }
    }

    // removes a file if possible
    private static boolean removeFile(String[] terms, Scanner reader) {
        if (terms.length != 2) {
            System.out.println("This command requires one argument.");
            return false;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return false;

        if (Files.isDirectory(filename)) {
            System.out.println("This command does not allow to delete a directory.");
            return false;
        }

        if (!Files.isRegularFile(filename)) {
            filename = Paths.get(getCurrentDirectory(), filename.getFileName().toString());
            if (!Files.isRegularFile(filename)) {
                System.out.println("There is no file with the name you've entered.");
                return false;
            }
        }

        for (String input = null; ; ) {
            System.out.printf("  Are you sure you want to delete the file %s? (y/n)%n", filename.toString());
            input = reader.nextLine().trim();
            if (input.equalsIgnoreCase("y")) break;
            if (input.equalsIgnoreCase("n")) return false;
            System.out.println("  You should enter either 'y' or 'n'.");
        }

        try {
            Files.delete(filename);
        } catch (NoSuchFileException e) {
            System.out.println("There is no file with the name you've entered.");
            return false;
        } catch (IOException e) {
            System.out.println("This file can't be deleted (check its permissions).");
            return false;
        }

        return true;
    }

    // creates a new file if possible
    private static boolean createNewFile(String[] terms) {
        if (terms.length != 2) {
            System.out.println("This command requires one argument.");
            return false;
        }

        if (terms[1].contains("\\") || terms[1].contains("/")) {
            System.out.println("You should use just a file name without any directory names included.");
            return false;
        }

        Path filename = null;
        try {
            filename = Paths.get(getCurrentDirectory(), terms[1]);
        } catch (InvalidPathException e) {
            System.out.println("The file name you've entered is invalid.");
            return false;
        }

        try {
            Files.createFile(filename);
        } catch (FileAlreadyExistsException x) {
            System.out.println("The file with this name already exists.");
            return false;
        } catch (IOException x) {
            System.out.println("Unfortunately, an I/O error occurred during file creation.");
            return false;
        }
        return true;
    }

    // setter for the current (working) directory
    private static void setWorkingDirectory(Path workingDirectory) {
        FileViewer.workingDirectory = workingDirectory;
    }

    // changes the current directory if possible
    private static boolean changeCurrentDirectory(String[] terms) {
        if (terms.length != 2) {
            System.out.println("This command requires one argument.");
            return false;
        }

        Path dirname = normalizePath(terms[1]);
        if (dirname == null) return false;

        if (!Files.isDirectory(dirname)) {
            System.out.println("There is no such directory, or it's inaccessible.");
            return false;
        }

        String prevDir = getCurrentDirectory();
        setWorkingDirectory(dirname);
        if (!prevDir.equals(getCurrentDirectory())) {
            System.out.printf("The current directory was changed to: %s%n", getCurrentDirectory());
            return true;
        }
        return false;
    }

    // prints a text file content to the standard output if possible
    private static boolean printTextFileContent(String[] terms) {
        if (terms.length < 2 || terms.length > 3) {
            System.out.println("This command requires either one or two arguments.");
            return false;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return false;

        if (!Files.isReadable(filename) || !Files.isRegularFile(filename)) {
            System.out.println("There is no such file to view, or it can't be read.");
            return false;
        }

        Charset charset = null;
        try {
            if (terms.length == 3) charset = Charset.forName(terms[2]);
            else charset = Charset.defaultCharset();
        } catch (IllegalArgumentException e) {
            System.out.println("You've entered the wrong or unsupported charset.");
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(filename, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.flush();
        } catch (SecurityException | IOException e) {
            System.out.println("Unfortunately, an I/O error occurred while reading this text file.");
            return false;
        }
        return true;
    }

    // replaces . with current dir and .. with parent dir at the beginning of the path string
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

    // lists all directories and files in the current directory
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

    // returns the current directory in a string representation
    static String getCurrentDirectory() {
        return workingDirectory.toString();
    }
}
