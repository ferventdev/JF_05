package t01;

import java.io.File;
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
            default:
                System.out.println("Such command doesn't exist, ot its using is incorrect.");
                System.out.println("Enter - help <COMMAND> - to know how to use the command.");
        }
    }

    private static void printTextFileContent(String[] terms) {
        if (terms.length < 2) {
            System.out.println("This command requires at least one argument (the file you'd like to view).");
            return;
        }

        Path filename = null;
        if (terms[1].startsWith("..")) {
            Path parent = Paths.get(getCurrentDirectory()).getParent();
            if (parent == null) {
                System.out.println("The current directory doesn't have a parent directory.");
                return;
            }
            filename = Paths.get(parent.toString(), terms[1].substring(2));
        } else if (terms[1].startsWith(".")) {
            filename = Paths.get(getCurrentDirectory(), terms[1].substring(1));
        } else filename = Paths.get(terms[1]);

        if (!Files.exists(filename)) {
            System.out.println("There is no such file to view.");
            return;
        }


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
