package hangman;

import java.io.*;
import java.util.*;
import java.lang.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    Set<String> currentPartition;
    int wordLength;
    int maxGuess;
    int numGuesses;
    byte guessArray[];
    char evilTemplate[];

    public EvilHangmanGame() {}

    public String getGuessTemplate() {
        return new String(evilTemplate);
    }

    public byte[] getGuessArray() {
        return guessArray;
    }

    public Set<String> getCurrentPartition() {
        return currentPartition;
    }

    int getNumSpaces(String str) {
        int count = 0;
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == '-')
                count++;
        return count;
    }

    public int getnumGuesses() {
        return numGuesses;
    }

    public void incrnumGuesses() {
        numGuesses++;
    }

    public String getGuessString() {
        char ch = 'a';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < guessArray.length; i++) {
            if (guessArray[i] == 1) {
                sb.append(ch);
                sb.append(" ");
            }
            ch++;
        }
        return sb.toString();
    }

    int wordContainsGuess(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++)
            if (str.charAt(i) == ch)
                count++;
        return count;
    }

    public boolean isWinner() {
        for (int i = 0; i < evilTemplate.length; i++)
            if (!Character.isAlphabetic(evilTemplate[i])) // still blank spots left
                return false;
        return true;
    }

    @SuppressWarnings("serial")
    public static class GuessAlreadyMadeException extends Exception {}

    public void startGame(File dictionary, int wordLength) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(dictionary);
            // scanner.useDelimiter("(\\s+)(#[^\\n]*\\n)?(\\s*)|(#[^\\n]*\\n)(\\s*)");
        } catch (FileNotFoundException e) {
            System.err.println("USAGE: java hangman.EvilHangman filename.txt numLetters numGuesses");
            e.printStackTrace(System.out);
        }

        Set<String> startSet = new HashSet<String>(); // hold all possible choices
        this.wordLength = wordLength;
        this.numGuesses = 0;

        guessArray = new byte[26];
        evilTemplate = new char[wordLength];
        for (int i = 0; i < wordLength; i++) {
            evilTemplate[i] = '-';
        }
        for (byte b : guessArray) {
            b = 0;
        }

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            if(s.length() == wordLength)
                startSet.add(s);
        }

        currentPartition = new HashSet<String>();
        currentPartition.addAll((startSet));
    }

    public Set<String> makeGuess(char guess) throws IEvilHangmanGame.GuessAlreadyMadeException {
        guess = Character.toLowerCase(guess);
        guess -= 'a';

        if (guessArray[guess] == 1)
            throw new IEvilHangmanGame.GuessAlreadyMadeException();

        guessArray[guess] = 1;

        guess += 'a';

        List<String> templates = new ArrayList<String>();
        HashMap<String,Set<String>> partitionMap;
        partitionMap = new HashMap<String,Set<String>>();
        partitionMap.put(new String(evilTemplate), new HashSet<String>());

        templates.add(new String(evilTemplate));

        char temp[] = new char[wordLength];

        for (String s : currentPartition) {
            boolean isNewTemp = false;
            for (int i = 0; i < wordLength; i++)
                temp[i] = evilTemplate[i];
            for (int i = 0; i < s.length(); i++) {
                if(!Character.isAlphabetic(temp[i])) {
                    if(s.charAt(i) == guess) {
                        temp[i] = guess;
                        isNewTemp = true;
                    }
                }
            }
            if (isNewTemp) {
                String str = new String(temp);
                if (!partitionMap.containsKey(str)) {
                    Set<String> tempSet = new HashSet<String >();
                    tempSet.add(s);
                    templates.add(str);
                    partitionMap.put(str,tempSet);
                }
                else
                    partitionMap.get(str).add(s);
            }
            else // add to outer set
                partitionMap.get(new String(evilTemplate)).add(s);
        }

        // choose the largest
        int maxSize = 0;
        String maxKey = new String();
        Map<String, Set<String>> tempMap = new HashMap<String, Set<String>>();
        for(String s : templates) {
            if (partitionMap.get(s).size() > maxSize) {
                tempMap.clear();
                maxSize = partitionMap.get(s).size();
                maxKey = s;
                tempMap.put(s,partitionMap.get(s));
            }
            else if ((partitionMap.get(s).size() != 0) && (partitionMap.get(s).size() == maxSize))
                tempMap.put(s,partitionMap.get(s));
        }
        if (tempMap.size() == 1) {
            for (int i = 0; i < wordLength; i++)
                evilTemplate[i] = maxKey.charAt(i);
            currentPartition.clear();
            currentPartition.addAll(partitionMap.get(maxKey));
            if (wordContainsGuess(maxKey,guess) == 0) {
                System.out.println("Sorry, there are no " + guess + "'s");
                incrnumGuesses();
            }
            else
                System.out.println("Yes, there is " + wordContainsGuess(maxKey,guess) + " " + guess);

            return partitionMap.get(maxKey);
        }
        else { // use the three criteria to pick the preferred set of words
            for (String s : tempMap.keySet()) {
                boolean letterAppears = false;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == guess)
                        letterAppears = true;
                }
                if (!letterAppears) {
                    for (int i = 0; i < wordLength; i++)
                        evilTemplate[i] = s.charAt(i);
                    currentPartition.clear();
                    currentPartition.addAll(partitionMap.get(s));
                    if (wordContainsGuess(maxKey,guess) == 0) {
                        System.out.println("Sorry, there are no " + guess + "'s");
                        incrnumGuesses();
                    }
                    else
                        System.out.println("Yes, there is " + wordContainsGuess(maxKey,guess) + " " + guess);

                    return partitionMap.get(s);
                }
            }

            int maxSpaces = 0;
            boolean isUnique = true;

            for (String s : tempMap.keySet()) {
                int count = 0;
                for (int i = 0; i < s.length(); i++)
                    if (s.charAt(i) == '-')
                        count++;
                if (count > maxSpaces) {
                    maxSpaces = count;
                    maxKey = s;
                }
                else if (count == maxSpaces) {
                    isUnique = false;
                }
            }
            if (isUnique) {
                for (int i = 0; i < wordLength; i++)
                    evilTemplate[i] = maxKey.charAt(i);
                currentPartition.clear();
                currentPartition.addAll(partitionMap.get(maxKey));
                if (wordContainsGuess(maxKey,guess) == 0) {
                    System.out.println("Sorry, there are no " + guess + "'s");
                    incrnumGuesses();
                }
                else
                    System.out.println("Yes, there is " + wordContainsGuess(maxKey,guess) + " " + guess);

                return partitionMap.get(maxKey);
            }

            boolean setSelected = false;
            isUnique = true;

            while (!setSelected) {
                for (int j = wordLength - 1; j >= 0; j--) {
                    int numUnique = 0;
                    for (String s : tempMap.keySet()) {
                        if (getNumSpaces(s) == maxSpaces) {
                            if (s.charAt(j) == guess) {
                                numUnique++;
                                maxKey = s;
                            }
                        }
                    }
                    if (numUnique == 1) {
                        for(int i = 0; i < wordLength; i++)
                            evilTemplate[i] = maxKey.charAt(i);
                        currentPartition.clear();
                        currentPartition.addAll(tempMap.get(maxKey));
                        if(wordContainsGuess(maxKey,guess) == 0) {
                            System.out.println("Sorry, there are no " + guess + "'s");
                            incrnumGuesses();
                        }
                        else
                            System.out.println("Yes, there is " + wordContainsGuess(maxKey,guess) + " " + guess);

                        return partitionMap.get(maxKey);
                    }
                }
            }
        }
        return null;
    }
}
