package MedicusCore;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class dbConnector
{
    static Connection dbConnection;

    private static void setConnect()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnection = DriverManager.getConnection("jdbc:mysql://35.231.111.6/MedicusDBPrimary?cloudSqlInstance=extended-web-276505:us-east1:medicusdb&user=root"); //todo (aloysius_w) - Root connection is dangerous. Gotta fix that up.
            System.out.println("Connection successful");
        }
        catch (Exception e)
        {
            System.out.println("Some error occurred.");
            System.out.println(e);
        }
    }

    public static void getConnect()
    {
        setConnect();
    }

    private void checkLogin(String inputUsername, String inputPassword)
    {
        try
        {
            Statement stm = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String queryPass = "SELECT CLIENT_PASSWORD FROM CLIENT_INFO WHERE CLIENT_USERNAME = '" + inputUsername + "'";
            ResultSet checkPass = stm.executeQuery(queryPass);
            if (checkPass.toString().equals(inputPassword))
            {
                JOptionPane.showMessageDialog(null, "Success.");
            }
            else
            {
                JOptionPane.showMessageDialog(null, "Invalid credentials");
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "An error has occurred. \n" + e);
        }
    }

    public void validateLogin(String u, String p)
    {
        checkLogin(u, p);
    }
}
