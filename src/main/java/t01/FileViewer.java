package t01;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Aleksandr Shevkunenko on 14.07.2017.
 */
public class FileViewer {
    public static void main(String[] args) {
        try (Scanner reader = new Scanner(System.in)) {
            for (String input = null; ; ) {
                System.out.print("$ ");
                input = reader.nextLine();
                if (input.equalsIgnoreCase("exit")) break;
                if (input.isEmpty()) continue;
                parseCommand(input);
            }
        }
    }

    private static void parseCommand(String input) {
        assert input != null : "The input string mustn't be null!";
        String[] terms = input.trim().split("\\s+");
    }
}
