package t01;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Aleksandr Shevkunenko on 14.07.2017.
 */
public class FileViewer {
    static final String COMMAND_PREVIEW = "You can use the following commands:\n" +
            "help - to see this prompt again;\n" +
            "exit - to quit from this program.";

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
        String[] terms = input.split("\\s+");
        assert terms.length != 0 : "The input string mustn't be empty!";

        switch (terms[0].toLowerCase()) {
            case "help":
                if (input.equalsIgnoreCase("help")) System.out.println(COMMAND_PREVIEW);
                break;
            default:
                System.out.println("Such command doesn't exist, ot its using is incorrect.");
                System.out.println("Enter - help <COMMAND> - to know how to use the command.");
        }
    }
}
