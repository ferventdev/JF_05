package t01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
            "wr    - to write (append) into the existing text file;\n" +
            "help  - to see this command prompt again, or - help <command> - for the command description;\n" +
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
    static void parseCommand(String input, Scanner reader) {
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
                printCommandHelp(terms);
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
            case "wr":
                writeToEndOfFile(terms, reader);
                break;
            default:
                System.out.println("  Such command doesn't exist, ot its using is incorrect.");
                System.out.println("  Enter - help <command> - to know how to use the command.");
        }
    }

    private static void printCommandHelp(String[] terms) {
        String info =
        "pwd   - to see the current (working) directory;\n" +
                "ls    - to see the content of the current directory;\n" +
                "cat   - to see the content of the text file;\n" +
                "cd    - to change the current directory;\n" +
                "mk    - to create an empty file in the current directory;\n" +
                "rm    - to remove a file;\n" +
                "wr    - to write (append) into the existing text file;\n" +
                "help  - to see this command prompt again, or - help <COMMAND> - for the command description;\n" +
                "exit  - to quit from this program.";

        switch (terms[1].toLowerCase()) {
            case "help":
                info = "  This command prints all commands preview (prompt) if entered without any arguments.\n" +
                        "  And when it's entered with one argument (which stands for the command of interest), it prints the command's description.\n" +
                        "  Using:\n  help <command>";
                break;
            case "pwd":
                info = "  This command prints the current (working) directory. It needs no arguments.\n" +
                        "  Using:\n  pwd";
                break;
            case "ls":
                info = "  This command prints the content of the current (working) directory. It needs no agruments.\n" +
                        "  Using:\n  ls";
                break;
            case "exit":
                info = "  This command quits from this program. It needs no arguments.\n" +
                        "  Using:\n  exit";
                break;
            case "cat":
                info = "  This command prints the content of the text file. It needs at least one argument, which is the name of the file to be read.\n" +
                        "  Second argument is optional and, if present, means the charset (encoding) to read this file with.\n" +
                        "  Using:\n  cat <file_to_be_read> (<encoding>)";
                break;
            case "cd":
                info = "  This command changes the current (working) directory to another one. It needs one argument, which is the new directory.\n" +
                        "  Using:\n  cd <another_directory>";
                break;
            case "mk":
                info = "  This command creates an empty file in the current (working) directory. It needs one argument - the name of this new (empty) file.\n" +
                        "  The name of this new file musn't contain any directories.\n"
                        "  Using:\n  mk";
                break;
            case "rm":
                info = "";
                break;
            case "wr":
                info = "";
                break;
            default: info = "  There's no help on this command, because this command doesn't exist.";
        }
        System.out.println(info);
    }

    static boolean writeToEndOfFile(String[] terms, Scanner reader) {
        if (terms.length < 2 || terms.length > 3) {
            System.out.println("  This command requires either one or two arguments.");
            return false;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return false;

        if (!Files.isRegularFile(filename)) {
            filename = Paths.get(getCurrentDirectory(), filename.getFileName().toString());
            if (!Files.isRegularFile(filename)) {
                System.out.println("  There is no file with the name you've entered.");
                return false;
            }
        }

        if (!Files.isWritable(filename)) {
            System.out.println("  You don't have the permission to write into this file.");
            return false;
        }

        Charset charset = null;
        try {
            if (terms.length == 3) charset = Charset.forName(terms[2]);
            else charset = Charset.defaultCharset();
        } catch (IllegalArgumentException e) {
            System.out.println("  You've entered the wrong or unsupported charset.");
            return false;
        }

        System.out.println("  Enter the text that you'd like to write to the end of the file.");
        System.out.println("  When done, leave two empty lines (press Enter two times) to complete your input.");

        StringBuilder text = new StringBuilder();
        for (String input = null;;) {
            input = reader.nextLine();
            if (input.isEmpty()) {
                if ((input = reader.nextLine()).isEmpty()) break;
                else text.append(String.format("%n%s%n", input));
            } else text.append(String.format("%s%n", input));
        }

        System.out.println("  Text input is complete.");

        for (String input = null;;) {
            System.out.printf("  Would you like to write the entered text to the end of file %s and save the file? (y/n)%n", filename.toString());
            System.out.flush();
            input = reader.nextLine().trim();
            if (input.equalsIgnoreCase("y")) break;
            if (input.equalsIgnoreCase("n")) return false;
            System.out.println("  You should enter either 'y' or 'n'.");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filename, charset, StandardOpenOption.APPEND)) {
            writer.append(text);
            writer.flush();
        } catch (IOException e) {
            System.out.printf("  Unfortunately, an I/O error occurred while writing or saving the file %s.%n", filename);
            return false;
        }

        System.out.printf("  The file %s was successfully saved.%n", filename);
        return true;
    }

    // removes a file if possible
    static boolean removeFile(String[] terms, Scanner reader) {
        if (terms.length != 2) {
            System.out.println("  This command requires one argument.");
            return false;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return false;

        if (Files.isDirectory(filename)) {
            System.out.println("  This command does not allow to delete a directory.");
            return false;
        }

        if (!Files.isRegularFile(filename)) {
            filename = Paths.get(getCurrentDirectory(), filename.getFileName().toString());
            if (!Files.isRegularFile(filename)) {
                System.out.println("  There is no file with the name you've entered.");
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
            System.out.println("  There is no file with the name you've entered.");
            return false;
        } catch (IOException e) {
            System.out.println("  This file can't be deleted (check its permissions).");
            return false;
        }

        System.out.printf("  The file %s was successfully removed.%n", filename);
        return true;
    }

    // creates a new file if possible
    static boolean createNewFile(String[] terms) {
        if (terms.length != 2) {
            System.out.println("  This command requires one argument.");
            return false;
        }

        if (terms[1].contains("\\") || terms[1].contains("/")) {
            System.out.println("  You should use just a file name without any directory names included.");
            return false;
        }

        Path filename = null;
        try {
            filename = Paths.get(getCurrentDirectory(), terms[1]);
        } catch (InvalidPathException e) {
            System.out.println("  The file name you've entered is invalid.");
            return false;
        }

        try {
            Files.createFile(filename);
        } catch (FileAlreadyExistsException x) {
            System.out.println("  The file with this name already exists.");
            return false;
        } catch (IOException x) {
            System.out.println("  Unfortunately, an I/O error occurred during file creation.");
            return false;
        }

        System.out.printf("  The file %s was successfully created.%n", filename);
        return true;
    }

    // setter for the current (working) directory
    static void setWorkingDirectory(Path workingDirectory) {
        FileViewer.workingDirectory = workingDirectory;
    }

    // changes the current directory if possible
    static boolean changeCurrentDirectory(String[] terms) {
        if (terms.length != 2) {
            System.out.println("  This command requires one argument.");
            return false;
        }

        Path dirname = normalizePath(terms[1]);
        if (dirname == null) return false;

        if (!Files.isDirectory(dirname)) {
            System.out.println("  There is no such directory, or it's inaccessible.");
            return false;
        }

        String prevDir = getCurrentDirectory();
        setWorkingDirectory(dirname);
        if (!prevDir.equals(getCurrentDirectory())) {
            System.out.printf("  The current directory was changed to: %s%n", getCurrentDirectory());
            return true;
        }
        return false;
    }

    // prints a text file content to the standard output if possible
    static boolean printTextFileContent(String[] terms) {
        if (terms.length < 2 || terms.length > 3) {
            System.out.println("  This command requires either one or two arguments.");
            return false;
        }

        Path filename = normalizePath(terms[1]);
        if (filename == null) return false;

        if (!Files.isRegularFile(filename)) {
            filename = Paths.get(getCurrentDirectory(), filename.getFileName().toString());
            if (!Files.isRegularFile(filename)) {
                System.out.println("  There is no file with the name you've entered.");
                return false;
            }
        }

        if (!Files.isReadable(filename)) {
            System.out.println("  You don't have the permission to read this file.");
            return false;
        }

        Charset charset = null;
        try {
            if (terms.length == 3) charset = Charset.forName(terms[2]);
            else charset = Charset.defaultCharset();
        } catch (IllegalArgumentException e) {
            System.out.println("  You've entered the wrong or unsupported charset.");
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(filename, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.flush();
        } catch (SecurityException | IOException e) {
            System.out.println("  Unfortunately, an I/O error occurred while reading this text file.");
            System.out.println("  Maybe, you need to try another charset to read this file.");
            return false;
        }
        return true;
    }

    // replaces . with current dir and .. with parent dir at the beginning of the path string
    static Path normalizePath(String aPath) {
        if (aPath.startsWith("..")) {
            Path parent = Paths.get(getCurrentDirectory()).getParent();
            if (parent == null) {
                System.out.println("  The current directory doesn't have a parent directory.");
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
