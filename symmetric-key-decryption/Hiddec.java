import java.util.*;
import java.nio.file.*;
import java.security.*;

import java.io.IOException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;
import java.util.Map;

public class Hiddec {
    private static final int MD5_DIGEST_LENGTH = 16;
    private Path inputFile;
    private Path outputFile;
    private String AESMode;
    private String keyString;
    private byte[] key;
    private String CTRInitValue;
    private byte[] CTRVector;
    private Cipher cipherManager;
    byte[] inputData;
    byte[] keyHash;

    public Hiddec(Map<String, String> programArguments) {
        inputFile = Paths.get(
            programArguments.get("inputFileName")
        );
        outputFile = Paths.get(
            programArguments.get("outputFileName")
        );
        AESMode = programArguments.get("mode");
        if (programArguments.containsKey("CTRInitValue")) {
            CTRInitValue = programArguments.get("CTRInitValue");
        }
        key = hexStringToByteArray(
            programArguments.get("keyString")
        );
    }

    public void findHiddenData() {
        /* Initialize cipher in EBC or CTR mode */
        configureAESMode(); // (Cipher) cipherManager init
        /* Read the input file and store the data */
        getInputDataFromFile(); // (byte[]) inputData init
        /* Produce and save the H(k) value */
        computeSecretKeyHash(); // (byte[]) keyHash init
        /* Count number of H(k) occurrences in input file */
        int numberOfKeyHashesInFile = countKeyHashesInFile();
        System.out.println("The input file contains " + numberOfKeyHashesInFile + " H(k)");
    }

    public int countKeyHashesInFile() {
        int counter = 0;
        for (int i = 0; i < inputData.length; i++) {
            byte[] chunk = Arrays.copyOfRange(inputData, i, i + 16);
            byte[] decryptedChunk = decrypt(chunk);
            if (byteArraysAreEqual(decryptedChunk, keyHash)) {
                counter++;
            }
        }
        return counter;
    }

    public byte[] decrypt(byte[] encryptedBytes) {
        byte[] decryptedBytes = null;
        try {
            decryptedBytes = cipherManager.doFinal(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("En error occurred when attempting to decrypt data from the input file.");
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
        return decryptedBytes;
    }

    public boolean byteArraysAreEqual(byte[] byteArray1, byte[] byteArray2) {
        return Arrays.equals(byteArray1, byteArray2);
    }

    public void computeSecretKeyHash() {
        try {
            MessageDigest hash = MessageDigest.getInstance("MD5");
            hash.update(key);
            keyHash = hash.digest();
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("Could not initialize a MD5 object of the class MessageDigest");
            System.out.println(nsae.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void getInputDataFromFile() {
        try {
            inputData = Files.readAllBytes(inputFile);
        } catch (IOException ioe) {
            System.out.println("Something went wrong when attempting to read from the input file " + inputFile.toString());
            System.out.println(ioe.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void configureAESMode() {
        if (AESMode.equals("ECB")) {
            setECBMode();
        } else {
            setCTRMode();
        }
    }

    public void setCTRMode() {
        try {
            CTRVector = hexStringToByteArray(CTRInitValue);
            cipherManager = Cipher.getInstance("AES/CTR/NoPadding");
            IvParameterSpec ivParamSpec = new IvParameterSpec(CTRVector);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipherManager.init(Cipher.DECRYPT_MODE, secretKey, ivParamSpec);
        } 
        catch (
            NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e
        ) {
            System.out.println("The key supplied to the program could not be used to initialize a Cipher object in CTR mode");
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void setECBMode() {
        try {
            cipherManager = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipherManager.init(Cipher.DECRYPT_MODE, secretKey);
        } 
        catch (
            NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e
        ) {
            System.out.println("The key supplied to the program could not be used to initialize a Cipher object in AES mode");
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    public byte[] hexStringToByteArray(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i = i + 2) {
            int firstChar = Character.digit(hexString.charAt(i), 16);
            int secondChar = Character.digit(hexString.charAt(i + 1), 16);
            byteArray[i / 2] = (byte) ((firstChar << 4) + (secondChar));
        }
        return byteArray;
    }

    static Map<String, String> validateArgs(String[] args) {
        int len = args.length;
        System.out.println(len);
        if ((len != 3) && (len != 4)) {
            System.out.println(" You have supplied the program with the wrong amount of arguments");
            System.out.println(" How to use if running AES-128 in ECB mode (3 arguments total):");
            System.out.println(" java Hiddec --key=KEY --input=INPUT --output=OUTPUT");
            System.out.println("------------------------------------");
            System.out.println(" How to use if running AES-128 in CTR mode (4 arguments total):");
            System.out.println(" java Hiddec --key=KEY --ctr=CTR --input=INPUT --output=OUTPUT");
            System.exit(1);
        }
        String delim = "=";
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("mode", "ECB");
        for (String commandLineArg : args) {
            String[] argComponents = commandLineArg.split(delim);
            String key = argComponents[0];
            String value = argComponents[1];
            if (key.equals("--key")) argsMap.put("keyString", value);
            else if (key.equals("--ctr")) {
                argsMap.put("mode", "CTR");
                argsMap.put("CTRInitValue", value);
            }
            else if (key.equals("--input")) argsMap.put("inputFileName", value);
            else if (key.equals("--output")) argsMap.put("outputFileName", value);
        }
        return argsMap;
    }
    public static void main(String[] args) throws Exception {
        Map<String, String> commandLineArgs = validateArgs(args);
        Hiddec hiddec = new Hiddec(commandLineArgs);
        hiddec.findHiddenData();
    }
}