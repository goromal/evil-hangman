package hangman;

import java.io.*;
import java.util.*;

public class EvilHangman {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int numGuesses = Integer.parseInt(args[2]);

        EvilHangmanGame game = new EvilHangmanGame();
        game.startGame(new File(args[0]), Integer.parseInt(args[1]));

        int guessCount = 0;

        while (game.getnumGuesses() < numGuesses) {
            System.out.println("You have " + (Integer.parseInt(args[2]) - game.getnumGuesses()) + " guesses left.");
            System.out.println("Used letters: " + game.getGuessString());
            System.out.print("Enter guess: ");

            String g = scanner.nextLine(); // processes as int
            boolean inputError = false;

            // check for all input error sources
            if (g == "")
                inputError = true;
            else
                if (g.length() != 1)
                    inputError = true;
                else
                    if (!Character.isAlphabetic(g.charAt(0)))
                        inputError = true;

            if (!inputError) {
                try {
                    Set<String> printSet = game.makeGuess(g.charAt(0));
                    System.out.println();
                } catch (IEvilHangmanGame.GuessAlreadyMadeException e) {
                    System.out.println("You already guessed " + g + ".");
                }
                System.out.println("Word: \t" + game.getGuessTemplate());
                if (game.isWinner())
                    break;
            }
            else
                System.out.println("User Input Error.");
        }
        if (game.getnumGuesses() == numGuesses) {
            System.out.println("You lose!");
            Set<String> total = game.getCurrentPartition();
            Iterator<String> it = total.iterator();
            System.out.println("The word was " + it.next());
        }
        else
            System.out.println("You win? I guess you cheated.");
    }
}
