import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class App {

        // list of letter positions in the ascii keyboard, from A-Z
        // the format is {line, column}
        public static final int[][] keyPositions = {
            {4, 4},     // a
            {6, 24},    // b
            {6, 16},    // c
            {4, 12},    // d
            {2, 10},    // e
            {4, 16},    // f
            {4, 20},    // g
            {4, 24},    // h
            {2, 30},    // i
            {4, 28},    // j
            {4, 32},    // k
            {4, 36},    // l
            {6, 32},    // m
            {6, 28},    // n
            {2, 34},    // o
            {2, 38},    // p
            {2, 2},     // q
            {2, 14},    // r
            {4, 8},     // s
            {2, 18},    // t
            {2, 26},    // u
            {6, 20},    // v
            {2, 6},     // w
            {6, 12},    // x
            {2, 22},    // y
            {6, 8},     // z   
        };
        public static int wordsGuessed = 0; // how many guesses have been inputted, public variable so it can be used outside the main loop
    
    /**
     * reads a character of input from the user without hitting enter
     * also returns special characters (like arrow keys)
     * 
     * @return  A string with the character entered, or the escape code if it's a special character
     */
    public static String getChar() throws IOException {
        int ch = -1;
        String esc = ""; // if it's a special character, this var will catch it
        while (ch == -1) {
            ch = System.in.read();
        }
        if (ch == 27) {
            esc += ((char) System.in.read() + ""); // extra '['
            esc += ((char) System.in.read() + ""); // actual escape character
        }
        return ((char) ch) + esc;
    }

    /**
     * Sets a single box with lines, a color, and a character in the middle (6x3 because terminal characters are 1x2)
     * example: ╭───╮
     *          │ x │
     *          ╰───╯
     * @param x     The x position of the top left corner of the box (in columns in the terminal)
     * @param color The ANSI escape code for the color of the box and the character inside
     * @param ch    The character to go inside the box
     */
    public static void drawBox(int x, int width, String color, String text) {
        String move = (x != 0) ? ("\033[" + x + "C") : "";
        boolean accessibilityMode = false; // mostly for debugging and stuff, maybe it'll be a feature latre
        if (accessibilityMode) {
            System.out.print(
                "\033[s" +
                move + color + "▗" + "▄".repeat(width) + "▖\r\n" + 
                move + color + "▐\033[7m " + text + " \033[0m" + color + "▌\r\n" +
                move + color + "▝" + "▀".repeat(width) + "▘\r\n" +
                "\033[0m\033[3A\033[" + (x + width + 3) + "D" 
            );
        } else {
            System.out.print(
                "\033[1D" +
                move + color + "╭" + "─".repeat(width) + "╮\r\n" + 
                move + color + "│ " + text + " │\r\n" +
                move + color + "╰" + "─".repeat(width) + "╯\r\n" +
                "\033[0m\033[3A\033[" + (x + width + 3) + "D" 
            );
        }
    }


    /**
     * Sets a letter in the ascii keyboard to some other text
     * 
     * @param letter    The letter to set
     * @param replace   What to replace it with
     * @param start     How many rows to move to get to the top left corner of the game grid
     */
    public static void setLetter(char letter, String replace, int start) {
        int[] pos = keyPositions["abcdefghijklmnopqrstuvwxyz".indexOf(letter)];
        System.out.print(
            "\033[" + start + "A" +
            "\033[" + (pos[0]) + "B" +
            "\033[" + (pos[1] + 38) + "C" +
            replace +
            "\033[" + start + "B" + 
            "\033[" + (pos[0]) + "A" +
            "\033[" + (pos[1] + 38) + "D" +
            "\033[0m"
        );
    }


    /**
     * Counts all occurrences of char `ch` in String `text`
     * 
     * @param text  The text to search through
     * @param ch    The character to count
     * @return      How many times `ch` occurs in `text`
     */
    public static int countChars(String text, char ch) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }
    /**
     * Counts all occurrences of char `ch` in array `letters`
     * 
     * @param letters   The array to search through
     * @param ch        The character to count
     * @return          How many times `ch` occurs in `text`
     */
    public static int countChars(char[] letters, char ch) {
        int count = 0;
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == ch) {
                count++;
            }
        }
        return count;
    }
    /**
     * Counts all occurrences of char `ch` in String `word`, where boolean[] `places` has `true` at that index
     * 
     * @param letters   The array to search through
     * @param ch        The character to count
     * @return          How many times `ch` occurs in `text`
     */
    public static int countChars(boolean[] places, String word, char ch) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (places[i] && word.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }


    /**
     * Uses a binary search to search through ArrayList `arr` for String `target`
     * 
     * @param arr   The ArrayList to search through 
     * @param val   The String to search for
     * @return      The index of `target` in `arr`
     */
    public static int binarySearch(ArrayList<String> arr, String target) {
        ArrayList<String> leftToSearch = arr; // the list of values left to search through
        int pivotIndex = 0; // the current index of the value to search on either side of
        int startIndex = 0; // the starting index of the area to search through
        int endIndex = arr.size() - 1; // the ending index of the area to search through 
        int diff = 0; // the result of compareTo, how much`target` lexographically comes after the value in `leftToSearch` at index `pivotIndex`
        // int iter = 0;
        while ((endIndex - startIndex) > 2) {
            pivotIndex = (int) ((endIndex - startIndex) / 2) + startIndex + 1;
            // compareTo returns a negative value if first val is sooner in the alphabet than the second val, egative if it's reversed, and 0 if it's the same
            diff = leftToSearch.get(pivotIndex).compareTo(target);
            /*iter++;
            System.out.println(
                "\nIteration " + iter +
                "\nvalues left: " + (endIndex - startIndex) +
                "\ndiff: " + diff +
                "\npivotIndex: " + pivotIndex + ", start: " + startIndex + ", end: " + endIndex +
                "\nlist: " + leftToSearch.subList(startIndex, endIndex).toString() +
                "\nstart val: " + leftToSearch.get(startIndex) +
                "\nend val: " + leftToSearch.get(endIndex)
            );
            */
            if (diff == 0) { // if it's the same (the value has been found)
                return pivotIndex;
            } else if (diff > 0) { // if it comes earlier in the list
                endIndex = pivotIndex;
            } else { // if it comes later in the list
                startIndex = pivotIndex;
            }
        }

        if (leftToSearch.get(startIndex).compareTo(target) == 0) {
            return startIndex;
        } else if (leftToSearch.get(endIndex - 1).compareTo(target) == 0) {
            return endIndex - 1;
        } else {
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        
        // word list
        // there are two lists, one for possible answers, and one for words that will be accepted as guesses
        ArrayList<String> answers = new ArrayList<String>();
        ArrayList<String> words = new ArrayList<String>();
        String line;
        boolean splitWords = false; // when the reader encounters a newline, it will set this var to true, and start adding the words to `words` instead of `answers`
        try {
            File f = new File("words.txt");
            Scanner reader = new Scanner(f);
            while (reader.hasNextLine()) {
                line = reader.nextLine();
                if (line.length() != 5) {
                    splitWords = true;
                } else {
                    if (!splitWords) {
                        answers.add(line);
                    } else {
                        words.add(line);
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error: please put words.txt in the same directory as the jar file!");
        }

        // terminal settings
        Terminal term = TerminalBuilder.terminal();
        term.enterRawMode();
        term.echo(false);
        System.out.println("\033[?25l"); // hide cursor and move line down by one

        // set first row of blocks, with first block light blue
        drawBox(1, 3, "\033[96m", " ");
        for (int i = 1; i < 5; i++) {
            drawBox((i * 6) + 1, 3, "", " ");
        }
        // draw the other 5 rows
        for (int i = 0; i < 5; i++) {
            System.out.print("\n\n\n");
            for (int j = 0; j < 5; j++) {
                drawBox((j * 6) + 1, 3, "\033[90m", " ");
            }
        }
        System.out.print("\033[15A"); // go back to top

        // draw keyboard
        String[] keyboard = {
            "╭───┬───┬───┬───┬───┬───┬───┬───┬───┬───╮",
            "│ q │ w │ e │ r │ t │ y │ u │ i │ o │ p │",
            "├───┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴─┬─┴───┤",
            "│   a │ s │ d │ f │ g │ h │ j │ k │ l   │",
            "├─────┼───┼───┼───┼───┼───┼───┼───┼─────┤",
            "│  ❯  │ z │ x │ c │ v │ b │ n │ m │  ❮  │",
            "╰─────┴───┴───┴───┴───┴───┴───┴───┴─────╯"
        };
        System.out.print("\033[s");
        for (int i = 0; i < keyboard.length; i++) {
            System.out.println("\033[38C" + keyboard[i]);
        }
        
        // add the wordle text
        /*
        String[] wordleText = {

        },
        */
        String[] wordleText = {
            "╦ ╦╔═╗╦═╗╔╦╗╦  ╔═╗",
            "║║║║ ║╠╦╝ ║║║  ╠╣ ",
            "╚╩╝╚═╝╩╚══╩╝╩═╝╚═╝"
        };
        for (int i = 0; i < wordleText.length; i++) {
            System.out.print("\r\n\033[49C\033[92m\033[1m" + wordleText[i]);
        }
        System.out.print("\r\n\r\n\033[48C\033[93m\033[3mCreated by Gus Ruben");
        System.out.print("\033[u");

        
        
        // hook into program exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                term.echo(true);
                System.out.println("\r\n".repeat(18 - (wordsGuessed * 3)) + "\033[?25h"); // make cursor visible again and move down 2 lines
                if (wordsGuessed >= 6) {
                    System.out.print("\r\n\r\n"); // add another 2 lines because the cursor moves if you finish the game
                } 
            }
        });
        
        // main loop variables
        String word = answers.get((int) (Math.random() * answers.size()));
        //String word = "sheep";
        // System.out.println(word + "\033[1A");
        String allowedChars = "abcdefghijklmnopqrstuvwxyz";
        String currentGuess = "";
        int currentChar = 0; // what character space is highlighted
        // ArrayList<String> highlightedLetters = new ArrayList<String>();

        // main loop
        while (true) {
            //test(currentGuess);
            String ch = getChar().toLowerCase();
            if (allowedChars.indexOf(ch) != -1) {
                if (currentChar != 5) {
                    currentGuess += ch;
                    
                    drawBox((currentChar * 6) + 1, 3, "", currentGuess.charAt(currentChar) + ""); // clear color of last block 
                    
                    currentChar++;
                    // highlight next block
                    if (currentGuess.length() < 5) {
                        drawBox((currentChar * 6) + 1, 3, "\033[96m", " ");
                    } else { // if it's the 5th one, show the enter button
                        drawBox((currentChar * 6) + 1, 4, "\033[96m", "Go");
                    }
                }  
            } else if (ch.charAt(0) == (char) 127) { // backspace
                if (currentChar != 0) {
                    if (currentChar != 5) {
                        drawBox((currentChar * 6) + 1, 3, "", " "); // clear color of current box
                    } else {
                        // clear color of enter button
                        drawBox((currentChar * 6) + 1, 4, "", "Go");
                    }
                    currentChar--;
                    currentGuess = currentGuess.substring(0, currentChar);
                    drawBox((currentChar * 6) + 1, 3, "\033[96m", " "); // highlight and clear text of last box
                }
            } else if ((ch.charAt(0) == '\r') && (currentChar == 5)) { // newline / enter
                if ((binarySearch(words, currentGuess) == -1) && (binarySearch(answers, currentGuess) == -1)) { // check if it's a valid word
                    System.out.print( // if it is, display an error msg
                        "\r\n".repeat(19 - (wordsGuessed * 3)) + 
                        "\033[91m\033[4CThat's not a valid word!" +
                        "\033[4D\033[" + (19 - (wordsGuessed * 3)) + "A" +
                        "\033[0m"
                    );
                } else {
                    wordsGuessed++;
                    // highlight the boxes with the correct letters
                    boolean[] checkedLetters = new boolean[5]; // boolean array that stores what letters have already been highlighted
                    // so it's highlighted properly, there are 3 loops, first green, then yellow, then gray
                    for (int c = 0; c < 3; c++) {
                        for (int i = 0; i < 5 ; i++) {
                            char letter = currentGuess.charAt(i);

                            if (checkedLetters[i]) {
                                // if the letter has already been highlighted, then skip it
                                continue;
                            }


                            String color = "";
                            switch (c) {
                                case 0: // green
                                    if (word.charAt(i) == letter) {
                                        color = "\033[1m\033[92m";
                                        checkedLetters[i] = true;
                                        break;
                                    }
                                    
                                case 1: // yellow
                                    // check to make sure that letter hasn't already been highlighted as many times as it appears, then highlight it
                                    if (countChars(checkedLetters, word, letter) < countChars(word, letter)) {
                                        if (word.indexOf(letter) != -1) {
                                            color = "\033[93m";
                                            checkedLetters[i] = true;
                                        }
                                    }
                                    break;
                                
                                case 2: // gray
                                    color = "\033[90m";
                                    break;
                                }

                            setLetter(letter, "\033[1m" + color + letter, ((wordsGuessed * 3) - 2)); // highlight letter on keyboard
                            drawBox((i * 6) + 1, 3, color, currentGuess.charAt(i) + ""); // highlight box, if it's gray then do nothing (keyboard color is different then box color)
                        }
                    }

                    /*
                    for (int i = 0; i < currentGuess.length(); i++) {
                        char letter = currentGuess.charAt(i);
                        // if that letter has already been highlighted (in checkedLetters), if there aren't more of that letter in `word` (than there are in checkedLetters), then dont highlight it
                        if (!(countChars(word, letter) > countChars(checkedLetters, letter))) {
                            // it's already colored gray, and the word on the keyboard is already the right color because of the previous letters
                            if (word.indexOf(letter) != -1) {
                                continue;
                            }
                        }

                        // checkedLetters += letter + "";
                        if (word.indexOf(letter) != -1) { // if it's in the word
                            if (word.charAt(i) == letter) { // if it's correct
                                color = "\033[1m\033[92m"; // green
                                setLetter(letter, "\033[1m\033[92m" + letter, ((wordsGuessed * 3) - 2)); // set the letter ont he ascii keybord to be green
                            } else {
                                color = "\033[1m\033[93m"; // yellow
                                setLetter(letter, "\033[1m\033[93m" + letter, ((wordsGuessed * 3) - 2)); // set the letter ont he ascii keybord to be yellow
                            }
                        } else {
                            color = "\033[1m"; // gray (default)
                            setLetter(letter, "\033[1m\033[90m" + letter, ((wordsGuessed * 3) - 2)); // gray out letter on the ascii keyboard
                        }
                        
                        drawBox((i * 6) + 1, 3, color, currentGuess.charAt(i) + ""); // highlight box
                    }
                    */

                    //  clear enter button and moves the cursor down to the next line of boxes
                    System.out.print(
                        (
                            "\033[30C" + 
                            " ".repeat(7) +
                            "\r\n"
                        ).repeat(3)
                    );
                    // clears warning text (if it's there)
                    System.out.print(
                        "\r\n".repeat(19 - (wordsGuessed * 3)) + 
                        " ".repeat(28) +
                        "\033[28D\033[" + (19 - (wordsGuessed * 3)) + "A"
                    );

                    if (currentGuess.compareTo(word) == 0) {
                        System.out.println("\r\n".repeat(18 - (wordsGuessed * 3)) + "\033[4A\033[45C\033[92mYou got it after " + wordsGuessed + " guess" + ((wordsGuessed == 1) ? "" : "es") + "!");
                        System.out.println("\033[49C\033[92mThe word was \033[93m" + word.toUpperCase() + "\033[92m!\033[0m");
                        System.exit(0);
                    } else if (wordsGuessed >= 6) {
                        System.out.println("\033[4A\033[37C\033[91mSorry, you didn't get it after 7 guesses...");
                        System.out.println("\033[49C\033[91mThe word was \033[93m" + word.toUpperCase() + "\033[91m!\033[0m"); // 12 spaces
                        System.exit(0);
                    } else {
                        // re-draw the boxes for the next line in default color, with the first one highlighted
                        drawBox(1, 3, "\033[96m", " ");
                        for (int i = 1; i < 5; i++) {
                            drawBox((i * 6) + 1, 3, "", " ");
                        }
                    }
                    
                    
                    currentGuess = "";
                    currentChar = 0;
                }
            }

        }
        
    }
}
