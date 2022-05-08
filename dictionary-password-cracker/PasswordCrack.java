import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class PasswordCrack {
    private int numberOfManglesToPerform = 2;

    private File dictionaryFile;
    private File hashedPasswordsFile;

    private HashMap<String, String> hashesAndSalts;
    private ArrayList<String> userLastnamesAndFirstnames;
    private ArrayList<String> dictionary;

    public void validateInputFiles(String dictionaryFileName, String hashedPasswordsFileName) {
        dictionaryFile = new File(dictionaryFileName);
        hashedPasswordsFile = new File(hashedPasswordsFileName);
        if (
            (!dictionaryFile.exists()) ||
            (!dictionaryFile.isFile()) ||
            (!dictionaryFile.canRead())
        ) {
            System.out.println(
                "Something went wrong when attempting to read the dictionary file you supplied to the program." +
                "Either your file doesn't exist, can't be read, or is not a valid file. Please try again."
            );
            System.exit(1);
        }
        if (
            (!hashedPasswordsFile.exists()) ||
            (!hashedPasswordsFile.isFile()) ||
            (!hashedPasswordsFile.canRead())
        ) {
            System.out.println(
                "Something went wrong when attempting to read the password file you supplied to the program." +
                "Either your file doesn't exist, can't be read, or is not a valid file. Please try again."
            );
            System.exit(1);
        }
    }

    public void buildPasswordMap() throws IOException {

        dictionary = new ArrayList<String>();
        hashesAndSalts = new HashMap<String, String>();
        userLastnamesAndFirstnames = new ArrayList<String>();

        BufferedReader buffReader = new BufferedReader(
            new FileReader(hashedPasswordsFile)
        );
        String fileLine;
        while(
            (fileLine = buffReader.readLine()) != null
        ) {

            String[] fileLineComponents = fileLine.split(":");
            String hashedPassword = fileLineComponents[1];
            String salt = fileLineComponents[1].substring(0, 2);
            hashesAndSalts.put(hashedPassword, salt);

            String[] userFullName = fileLineComponents[4].split(" ");
            for (String s : userFullName) {
                dictionary.add(s);
            }
        }
        buffReader.close();
    }

    public void buildDictionary() throws IOException {
        BufferedReader buffReader = new BufferedReader(
            new FileReader(dictionaryFile)
        );
        String fileLine;
        while(
            (fileLine = buffReader.readLine()) != null
        ) {
            dictionary.add(fileLine);
        }
        addCommonPasswordComponentsToDictionary();
        addUserNameInfoToDictionary();
        buffReader.close();
    }

    public void addCommonPasswordComponentsToDictionary() {
        String[] commonPasswordComponents = {
            "password", "qwerty", "123", "1234", "123456", "1234567", "12345678", "123456789", "1234567890",
            "picture", "abc", "abc123", "million", "iloveyou", "password1", "password2", "qqww", "qqww1122",
            "111", "1111", "123123", "senha", "omg", "654321", "qwertyuiop", "123456a", "a123456",
            "qwe", "asd", "ghj", "yui", "ert", "bnm", "uio"
        };
        for (String s : commonPasswordComponents) dictionary.add(s);
    }

    public void addUserNameInfoToDictionary() {
        for (String s : userLastnamesAndFirstnames) dictionary.add(s);
    }

    public void crackPasswords() {
        if (numberOfManglesToPerform == 2) {
            for (String entry : dictionary) attemptCrack(entry);
        }
        if (
            (!hashesAndSalts.isEmpty()) && 
            (numberOfManglesToPerform != 0)
        ) {
            if (numberOfManglesToPerform == 2) {
                mangleDictionaryOnce();
                numberOfManglesToPerform--;
                crackPasswords();
            } else if (numberOfManglesToPerform == 1) {
                mangleDictionaryTwice();
                numberOfManglesToPerform--;
                crackPasswords();
            }
        }
    }

    public String attemptCrack(String dictionaryEntry) {
        ArrayList<String> crackedHashes = new ArrayList<String>();
        Set<String> hashes = hashesAndSalts.keySet();
        for (String hash : hashes) {
            String salt = hashesAndSalts.get(hash);
            String hashedDictionaryEntry = jcrypt.crypt(salt, dictionaryEntry);
            if (hashes.contains(hashedDictionaryEntry)) {
                System.out.println(dictionaryEntry);
                crackedHashes.add(hashedDictionaryEntry);
            }
        }
        for (String s : crackedHashes) hashesAndSalts.remove(s);
        return dictionaryEntry;
    }

    public void mangleDictionaryOnce() {
        ArrayList<String> mangledDictionary = new ArrayList<String>();
        for (String dictionaryEntry : dictionary) {

            mangledDictionary.add(attemptCrack(lowerCaseString(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(upperCaseString(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(reverseString(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(capitalizeString(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(ncapitalizeString(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(toggleStringCase1(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(toggleStringCase2(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(reflectFirst(dictionaryEntry)));
            mangledDictionary.add(attemptCrack(reflectLast(dictionaryEntry)));
            
            if (dictionaryEntry.length() < 9) {
                mangledDictionary.add(attemptCrack(removeFirstChar(dictionaryEntry)));
                mangledDictionary.add(attemptCrack(removeLastChar(dictionaryEntry)));
            }

            if (dictionaryEntry.length() < 8) {
                mangledDictionary.add(attemptCrack(duplicateString(dictionaryEntry)));
                for (int i = 48; i < 58; i ++) {
                    mangledDictionary.add(attemptCrack(((char) i) + dictionaryEntry));
                    mangledDictionary.add(attemptCrack(dictionaryEntry + ((char) i)));
                }
                for (int i = 65; i < 94; i++) {
                    mangledDictionary.add(attemptCrack(((char) i) + dictionaryEntry));
                    mangledDictionary.add(attemptCrack(dictionaryEntry + ((char) i)));
                }
            }
        }

        dictionary = mangledDictionary;
    }

    public void mangleDictionaryTwice() {
        for (String dictionaryEntry : dictionary) {

            attemptCrack(lowerCaseString(dictionaryEntry));
            attemptCrack(upperCaseString(dictionaryEntry));                          
            attemptCrack(reverseString(dictionaryEntry));           
            attemptCrack(capitalizeString(dictionaryEntry));                
            attemptCrack(ncapitalizeString(dictionaryEntry));            
            attemptCrack(toggleStringCase1(dictionaryEntry));
            attemptCrack(toggleStringCase2(dictionaryEntry));
            attemptCrack(reflectFirst(dictionaryEntry));
            attemptCrack(reflectLast(dictionaryEntry));
            
            if (dictionaryEntry.length() < 9) {
                attemptCrack(removeFirstChar(dictionaryEntry));
                attemptCrack(removeLastChar(dictionaryEntry));
            }

            if (dictionaryEntry.length() < 8) {
                attemptCrack(duplicateString(dictionaryEntry));
                for (int i = 48; i < 58; i ++) {
                    attemptCrack(((char) i) + dictionaryEntry);
                    attemptCrack(dictionaryEntry + ((char) i));
                }
                for (int i = 65; i < 94; i++) {
                    attemptCrack(((char) i) + dictionaryEntry);
                    attemptCrack(dictionaryEntry + ((char) i));
                }
            }
        }
    }

    public String toggleStringCase1(String entry) {
        String toggledString = "";
        for (int i = 0; i < entry.length(); i++) {
            if ((i & 0x1) == 0) {
                String charToToggle = entry.substring(i, i + 1);
                toggledString += charToToggle.toUpperCase();
            } else {
                toggledString += entry.substring(i, i + 1);
            }
        }
        return toggledString;
    }

    public String toggleStringCase2(String entry) {
        String toggledString = "";
        for (int i = 0; i < entry.length(); i++) {
            if ((i & 0x1) == 1) {
                String charToToggle = entry.substring(i, i + 1);
                toggledString += charToToggle.toUpperCase();
            } else {
                toggledString += entry.substring(i, i + 1);
            }
        }
        return toggledString;
    }

    public String reverseString(String entry) {
        StringBuilder sb = new StringBuilder(entry);
        String mangledEntry = sb.reverse().toString();
        return mangledEntry;
    }

    public String duplicateString(String entry) {
        String mangledEntry = entry + entry;
        return mangledEntry;
    }

    public String removeLastChar(String entry) {
        String mangledEntry = entry.substring(0, entry.length() - 1);
        return mangledEntry;
    }

    public String removeFirstChar(String entry) {
        String mangledEntry = entry.substring(1);
        return mangledEntry;
    }

    public String capitalizeString(String entry) {
        String mangledEntry = entry.substring(0,1).toUpperCase() + entry.substring(1);
        return mangledEntry;
    }

    public String ncapitalizeString(String entry) {
        String mangledEntry = entry.substring(0,1).toLowerCase() + entry.substring(1).toUpperCase();
        return mangledEntry;
    }

    public String reflectFirst(String entry) {
        String mangledEntry = reverseString(entry) + entry;
        return mangledEntry;
    }

    public String reflectLast(String entry) {
        String mangledEntry = entry + reverseString(entry);
        return mangledEntry;
    }

    public String upperCaseString(String entry) {
        String mangledEntry = entry.toUpperCase();
        return mangledEntry;
    }

    public String lowerCaseString(String entry) {
        String mangledEntry = entry.toLowerCase();
        return mangledEntry;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("You have supplied the program with the wrong amount of arguments. How to use: ");
            System.out.println("$ java PasswordCracker <dictionaryFile.txt> <passwordFile.txt>");
            System.exit(1);
        }
        String dictionaryFileName = args[0];
        String passwordsFileName = args[1];

        PasswordCrack passwordCracker = new PasswordCrack();
        passwordCracker.validateInputFiles(dictionaryFileName, passwordsFileName);
        try {
            passwordCracker.buildPasswordMap();
        } catch (IOException ioe) {
            System.out.println("Something went wrong when attempting to read from the password file");
            System.exit(1);
        }

        try {
            passwordCracker.buildDictionary();
        } catch (IOException ioe) {
            System.out.println("Something went wrong when attempting to read from the dictionary file");
            System.exit(1);
        }

        passwordCracker.crackPasswords();

    }
}