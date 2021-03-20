/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Simple_Banking_System;
import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class App {
    public static Scanner s = new Scanner(System.in);
    static Random random = new Random();
    static DataBaseWorker dataBaseWorker = null;
    static boolean endRunning = false;

    public static void main(String[] args) {
        dataBaseWorker = new DataBaseWorker(args[1]);
        //args[1] because -fileName will be the first argument
        try {
            while (!endRunning) {
                mainMenu();
            }
        catch (SQLException e) {
            e.printStackTrace();       
        }
    }

    private static void mainMenu() throws SQLException {
        int choice;
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
        choice = s.nextInt();
        switch (choice) {
            case 1 -> creation();
            case 2 -> {
                System.out.println("Enter your card number:");
                long cardNumber = s.nextLong();
                System.out.println("Enter your PIN:");
                logIntoAccount(cardNumber, s.nextInt());
            }
            case 0 -> {
                endRunning = true;
                System.out.println("Bye!");
            }
            default -> endRunning = false;
        }
    }

    private static void creation() {
        long randomNumber = (long) random.nextInt(999_999_999 - 100_000_000) + 100_000_000;
//        System.out.println(randomNumber + " : is rando");
        long cardNumber = 4_000_000_000_000_000L + (randomNumber * 10);
        cardNumber += luhnAlgo(cardNumber);
//        int pinOfCard = random.nextInt(10_000 - 1000) + 1000;
        int pinOfCard = random.nextInt(10_000 - 1000) + 1000;
        
        dataBaseWorker.enterNewUserDetails(cardNumber, pinOfCard);
        
        System.out.println("Your card number has been created");
        
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pinOfCard);

    }

    private static void logIntoAccount(long cardNumber, int pinOfCard) throws SQLException {
        if (luhnAlgoTest(cardNumber)) { //checks if card number is luhn algorithm compatible or not
            //if the algorithm is fine then we can start logging in to database
            if (dataBaseWorker.logOnFunction(cardNumber, pinOfCard)) { //log on to the database using the card number and pin of the card
                System.out.println("You have successfully logged in!");
                optionsAfterLogon(cardNumber); //show options for the card number
            } else {
                System.out.println("Wrong card number or PIN!");
            }
        } else {
            System.out.println("Account information is wrong! Try Again!");
        }
    }

    private static void optionsAfterLogon(long cardNumber) throws SQLException {
        Scanner inside = new Scanner(System.in);
        while (!endRunning) {
            System.out.println("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit");
            switch (inside.nextInt()) {
                case 1 -> System.out.println("Balance : " + dataBaseWorker.accessBalance(cardNumber));
                case 2 -> {
                    System.out.println("Enter income:");
                    dataBaseWorker.addIncome(cardNumber, s.nextInt());
                }
                case 3 -> {
                    System.out.println("Enter card number:");
                    long transferNum = s.nextLong();
                    if (cardNumber != transferNum) {
                        if (luhnAlgoTest(transferNum)) {
                            if (dataBaseWorker.getCard(transferNum)) {
                                System.out.println("Enter how much money you want to transfer:\n");
                                System.out.println(dataBaseWorker.transferMoney(cardNumber, transferNum, s.nextInt()));
                            } else {
                                System.out.println("Such a card does not exist.\n");
                            }
                        } else {
                            System.out.println("Probably you made mistake in the card number. Please try again!");
                        }
                    } else {
                        System.out.println("You can't transfer money to the same account!");
                    }
                }
                case 4 -> {
                    dataBaseWorker.deleteAccount(cardNumber);
                    mainMenu();
                }
                case 5 -> mainMenu();
                case 0 -> {
                    endRunning = true;
                    System.out.println("Bye!");
                }
                default -> endRunning = false;
            }
        }
    }


    private static long luhnAlgo(long cardNumber) {
        long checksum = 0; // the value which needs to be at the end of the credit card number
        int sum = 0; // sum of all the digits that we want
        long[] requiredDigits = new long[16];
        for (int i = 0; i < 16; i++) {
            requiredDigits[i] = cardNumber % 10;
            cardNumber /= 10;
            if ((i % 2) != 0) {
                requiredDigits[i] *= 2;
            }
        }
        
        for (int i = 0; i < 16; i++) {
            if (requiredDigits[i] > 9) {
                requiredDigits[i] -= 9;
            }
            sum += requiredDigits[i];
        }

        for (int i = 0; i <= 9; i++) {
            if ((sum + i) % 10 == 0) {
                checksum = i;
            }
        }
        return checksum;
    }

    private static boolean luhnAlgoTest(long cardNumber) {
        long checksum = cardNumber % 10; // the value which is at the end of the current credit card number
        cardNumber -= checksum;//this will set the last digit to be zero

        return checksum == luhnAlgo(cardNumber);
    }
}
