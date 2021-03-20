package Simple_Banking_System;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseWorker {
    Connection con = null;
    Statement statement = null;
    SQLiteDataSource dataSource;

    //we are using the constructor to create the basic necessities for our database functionality
    //connection,statement object that is.
    public DataBaseWorker(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try {
            con = dataSource.getConnection();
//            if(con.isValid(5)){
//                System.out.println("Connection is working");
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // now we need to create statement object using which we can execute SQL statements
        try {
            statement = con.createStatement();
            //create the table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS card("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "number TEXT NOT NULL,"
                    + "pin TEXT NOT NULL,"
                    + "balance INTEGER DEFAULT 0);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // now function to insert details of the user
    public void enterNewUserDetails(long cardNumber, int pinOfCard) {
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            System.out.println("card : " + cardNumber + " pin " + pinOfCard);
            statement.executeUpdate("INSERT INTO card (number, pin) VALUES" + "('" + cardNumber + "','" + pinOfCard + "')");
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //now we want a function that will fetch data for logon of the user to his bank details
    public boolean logOnFunction(long cardNumber, int pinOfCard) throws SQLException {
        try {
            if (getCard(cardNumber)) {
                con = dataSource.getConnection();
                statement = con.createStatement();
                ResultSet bankRecord = statement.executeQuery("SELECT pin FROM card WHERE number = " + cardNumber);
                if (!bankRecord.isClosed() && bankRecord.getInt("pin") == pinOfCard) {
                    con.close();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.close();
        return false;
    }

    public int accessBalance(long cardNumber) {
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet bankBalance = statement.executeQuery("SELECT balance FROM card WHERE number = " + cardNumber);
            int bal = bankBalance.getInt("balance");
            con.close();
            return bal;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addIncome(long cardNumber, int addToBalance) {
        try {
            int balance = accessBalance(cardNumber) + addToBalance;
            con = dataSource.getConnection();
            statement = con.createStatement();
            if (con.isClosed()) {
                System.out.println("Conn is closed");
            }
            statement.executeUpdate("UPDATE card SET balance = " + balance + " WHERE number = " + cardNumber);
            System.out.println("Income was added!");
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewAll() {
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet bankRecord = statement.executeQuery("SELECT * from card");
            while (bankRecord.next()) {
                System.out.println(bankRecord.getString("number"));
                System.out.println(bankRecord.getString("pin"));
                System.out.println(bankRecord.getInt("balance"));
            }
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getCard(long cardNumber) {
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            ResultSet bankRecord = statement.executeQuery("SELECT number from card WHERE number = " + cardNumber);
            if (bankRecord != null) {
                con.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String transferMoney(long cardNumber, long transferAcc, int amount) {
        try {
//            System.out.println("Enter how much money you want to transfer:\n");

            //I had to create a new connection as calling accessBalance() was closing the connection earlier than expected
            //causing issue with updating the accounts
            //find the current balance of the user
            int currBalance = accessBalance(cardNumber);
            if (currBalance < amount && amount != 0) {
                return "Not enough money!";
            } else {
                int transfer = accessBalance(transferAcc) + amount;
                int debit = currBalance - amount;
                Connection conNew = dataSource.getConnection();
                statement = conNew.createStatement();
                //if everything is fine then we need to update the balance in the other person's account
                statement.executeUpdate("UPDATE card SET balance = " + transfer + " WHERE number = " + transferAcc);
                //after completing the transfer we need to reduce that amount from original account
                statement.executeUpdate("UPDATE card SET balance = " + debit + " WHERE number = " + cardNumber);
                conNew.close();
                return "Success!";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Some error occured! Try again!";
    }

    public void deleteAccount(long cardNumber) {
        try {
            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate("DELETE FROM card\n" +
                    "WHERE " + cardNumber);
            System.out.println("The account has been closed!");
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
