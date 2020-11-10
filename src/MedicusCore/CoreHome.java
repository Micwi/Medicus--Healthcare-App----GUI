/*
 * Created by JFormDesigner on Sat May 09 21:51:12 EDT 2020
 */

package MedicusCore;

import com.bulenkov.darcula.DarculaLaf;
import net.miginfocom.swing.*;
import net.proteanit.sql.DbUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.Provider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


/*
 * Team             :   Elite Four
 * Developers       :   Aloysius Arno Wiputra (FE & BE), Louie Patrizi (BE), Chit Lam (FE)
 * Special Thanks   :   Louie Patrizi, Chit Lam - for roughing it out through the development of the initial prototype
 *                      Robert Doxey && Alex Vasquez-Zavala - for aiding us in figuring out why Android Studio is garbage. Yall awesome.
 *                      Team 1 && Team 2 - for giving us hope that we can actually do this.
 * Leech            :   Ravi Bera
 * E-mail           :   aloysiuswiputra@gmail.com / awiputra@nyit.edu | lpatz@nyit.edu
 *
 * Last modified (DD/MM/YY): 10/05/20
 * todo finish provider UI and active/completed functionality (like notifications)
 *  add order history with new table?
 *
 */

public class CoreHome extends JFrame
{

    /*JFormDesigner keeps overwriting this, remember to overwrite it before each run!
        String [] fieldStrings = {"Admin", "User", "Provider", "Physician"};
        field_Scroll = new JComboBox(fieldStrings);
        */
    CardLayout mainPanelLayout = new CardLayout(); //Basically a deck organizer, the cards are different pages
    CardLayout adminDashBoardLayout = new CardLayout();
    CardLayout userDashBoardLayout = new CardLayout();
    CardLayout physDashBoardLayout = new CardLayout();


    String currentUser = null;
    String currentUserFN = null;

    public CoreHome()
    {
        initComponents(); //Initialize everything

        masterPanel.setLayout(mainPanelLayout);
        panelAdmDashboard.setLayout(adminDashBoardLayout);
        panelUserDashboard.setLayout(userDashBoardLayout);

        panelPhysDashboard.setLayout(physDashBoardLayout);

        masterPanel.add(panelMain, "1"); //Login page
        masterPanel.add(panelAccCreate, "2"); //Account creation pag
        // e
        masterPanel.add(panelPageAdmin, "3"); //Admin page
        panelAdmDashboard.add(panelAdminTaskTable, "3a");
        panelAdmDashboard.add(panelAdminHome, "3b");

        masterPanel.add(panelPageUser, "4"); //User page
        panelUserDashboard.add(panelUserOrderTable, "4a");
        panelUserDashboard.add(panelUserHome, "4b");
        panelUserDashboard.add(panelUserOrder, "4c");

        masterPanel.add(panelPageProvider, "5");//Provider page

        masterPanel.add(panelPagePhysician, "6"); //Physician page
        panelPhysDashboard.add(panelPhysManaged, "6a");
        panelPhysDashboard.add(panelPhysHome, "6b");

        dbConnector.getConnect(); //Establish connection to Database, todo (aloysius_w) - Specify connection based on username with varying permission

        EncryptionCore hideYoShit = new EncryptionCore(); //thanks Louie for getting this to work

        btn_Login.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection authConnection = dbConnector.dbConnection; //Get connection
                    Statement stm = authConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPass = "SELECT CLIENT_PASSWORD, CLIENT_USERTYPE, CLIENT_NAMEFIRST, CLIENT_NAMELAST FROM CLIENT_INFO WHERE CLIENT_USERNAME = '" + field_Username.getText() + "'"; //Grab the username field, then checks database for PW

                    ResultSet checkPass = stm.executeQuery(queryPass);

                    if (checkPass.next()) //If the result exists, then check
                    {
                        String retrievedPassword = checkPass.getString("CLIENT_PASSWORD"); //Grabs the password value from the database
                        String decryptedPassword = new String(hideYoShit.Decrypt(retrievedPassword.getBytes())); //Decrypts the password

                        if (decryptedPassword.equals(String.valueOf(field_Password.getPassword())))
                        {
                            JOptionPane.showMessageDialog(null, "Success.");
                            currentUser = field_Username.getText();
                            currentUserFN = checkPass.getString("CLIENT_NAMEFIRST");
                            System.out.println(checkPass.getString("CLIENT_USERTYPE"));
                            if (checkPass.getString("CLIENT_USERTYPE").equalsIgnoreCase("Admin"))
                            {
                                mainPanelLayout.show(masterPanel, "3");
                                getContentPane().remove(panelAdmDashboard);
                                adminDashBoardLayout.show(panelAdmDashboard, "3b");
                                text_DashWelcome.setText("Welcome, " + currentUserFN); //Initialize the thing

                                String queryNotification = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE READ_STATUS='UNREAD'"; //Counts how many unread status there is
                                ResultSet resultPendingCount = stm.executeQuery(queryNotification); //IT'S AN EQUAL, LOUIE, NOT A MINUS
                                resultPendingCount.first();
                                text_Notifications.setText(resultPendingCount.getString("total") + " NEW NOTIFICATIONS"); //Slaps the pending unread to the notification text

                                String queryNumAccCreate = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE TASK_NAME = 'ACCOUNT CREATION'";
                                ResultSet resultNumAccCreate = stm.executeQuery(queryNumAccCreate);
                                resultNumAccCreate.first();
                                text_NumAccCreate.setText(resultNumAccCreate.getString("total") + " ACCCOUNT REQUESTS");

                                String queryNumAcc = "SELECT COUNT(*) AS total FROM CLIENT_INFO";
                                ResultSet resultNumAcc = stm.executeQuery(queryNumAcc);
                                resultNumAcc.first();
                                text_NumUsers.setText(resultNumAcc.getString("total") + " TOTAL USERS"); //Slaps the pending unread to the notification text

                            }
                            else if (checkPass.getString("CLIENT_USERTYPE").equalsIgnoreCase("User"))
                            {
                                mainPanelLayout.show(masterPanel, "4");
                                userDashBoardLayout.show(panelUserDashboard, "4b");
                                String patientNameFirst = checkPass.getString("CLIENT_NAMEFIRST");
                                String patientNameLast = checkPass.getString("CLIENT_NAMELAST");
                                label_NameLASTFIRST.setText(patientNameFirst + ", " + patientNameLast);

                                text_DashWelcomeUser.setText("Welcome, " + patientNameFirst); //Initialize the thing

                                String queryNotification = "SELECT COUNT(*) AS total FROM CLIENT_RECORDS_LOG WHERE READ_STATUS='UNREAD' AND CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many unread status there is
                                ResultSet resultPendingCount = stm.executeQuery(queryNotification);
                                resultPendingCount.first();

                                int notifyUserOne = Integer.parseInt(resultPendingCount.getString("total"));

                                text_NotificationsUser.setText(Integer.toString(notifyUserOne) + " NEW NOTIFICATIONS");


                                String queryPhys = "SELECT MANAGING_PHYSICIAN, LAST_UPDATE FROM CLIENT_RECORDS WHERE CLIENT_USERNAME = '" + currentUser + "'";
                                ResultSet phys = stm.executeQuery(queryPhys);
                                phys.first();
                                String physicianName = phys.getString("MANAGING_PHYSICIAN");

                                label_LU.setText(phys.getString("LAST_UPDATE"));

                                String grabPhysName = "SELECT CLIENT_NAMEFIRST, CLIENT_NAMELAST FROM CLIENT_INFO WHERE CLIENT_USERNAME = '" + physicianName + "'";
                                ResultSet physName = stm.executeQuery(grabPhysName);
                                physName.first();

                                String physicianNameFirst = physName.getString("CLIENT_NAMEFIRST");
                                String physicianNameLast = physName.getString("CLIENT_NAMELAST");

                                label_PhysLASTFIRST.setText(physicianNameFirst + ", " + physicianNameLast);


                            }
                            else if (checkPass.getString("CLIENT_USERTYPE").equalsIgnoreCase("Provider"))
                            {
                                text_DashWelcomeProvider.setText("Welcome, " + currentUserFN);
                                mainPanelLayout.show(masterPanel, "5");
                            }
                            else if (checkPass.getString("CLIENT_USERTYPE").equalsIgnoreCase("Physician"))
                            {
                                text_DashWelcomePhysician.setText("Welcome, " + currentUserFN);
                                mainPanelLayout.show(masterPanel, "6");
                            }
                            else
                            {
                                JOptionPane.showMessageDialog(null, "Hmm, something went wrong"); //Catch all if something else beyond my current foresight happens
                            }
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null, "Invalid credentials");
                        }
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, "User not found");
                    }
                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, "You cannot leave an empty field. \n " + f); //todo (aloysius_w) - Pile all these errors into a separated database/report
                }
            }
        });
        btn_NewCreate.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                field_CUsername.setText(""); //Cleans the field each time we move, todo (aloysius_w) - Make a method to auto clean everything without specifying each time
                field_CPass.setText("");
                field_FName.setText("");
                field_LName.setText("");
                field_dobD.setText("");
                field_dobM.setText("");
                field_dobY.setText("");
                field_Username.setText("");
                field_Password.setText("");
                mainPanelLayout.show(masterPanel, "2"); //Switch functions to change which panel is displayed
            }
        });
        /*---Account Creation buttons---*/

        btn_BackToLogin.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                field_CUsername.setText("");
                field_CPass.setText("");
                field_FName.setText("");
                field_LName.setText("");
                field_dobD.setText("");
                field_dobM.setText("");
                field_dobY.setText("");
                field_Username.setText("");
                field_Password.setText("");
                mainPanelLayout.show(masterPanel, "1"); //Switches back to panel 1
            }
        });
        btn_ResetFields.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                field_CUsername.setText("");
                field_CPass.setText("");
                field_FName.setText("");
                field_LName.setText("");
                field_dobD.setText("");
                field_dobM.setText("");
                field_dobY.setText("");
            }
        });
        btn_CreateAcc.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection createAccConnection = dbConnector.dbConnection;
                    Statement stm = createAccConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryTaskID = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE TASK_STATUS = 'PENDING'"; //Counts how many pending status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryTaskID);

                    resultPendingCount.first();

                    int assignIDnum = (resultPendingCount.getInt("total") + 1);
                    String assignIDstring = assignIDnum + "-PENDING"; //Creates TASK_ID with the format "NUM" + "-PENDING"
                    try
                    {
                        for (int i = 0; i < 1; i++)
                        {
                            String input_CUsername = field_CUsername.getText();
                            String input_CPass = String.valueOf(field_CPass.getPassword());

                            String input_CPass_Encrypted = new String(hideYoShit.Encrypt(input_CPass.getBytes()));

                            String input_FName = field_FName.getText().toUpperCase();
                            String input_LName = field_LName.getText().toUpperCase();
                            String input_Usertype = field_Scroll.getSelectedItem().toString().toUpperCase(); //Grabs the data from JComboBox depending on user selection
                            String input_dobD = field_dobD.getText().toUpperCase();
                            String input_dobM = field_dobM.getText().toUpperCase();
                            String input_dobY = field_dobY.getText().toUpperCase();
                            if (0 > Integer.parseInt(input_dobM) || 13 < Integer.parseInt(input_dobM))
                            {
                                JOptionPane.showMessageDialog(null, "Invalid birthday month");
                            }
                            else if ((0 > Integer.parseInt(input_dobD) || 32 < Integer.parseInt(input_dobM)))
                            {
                                JOptionPane.showMessageDialog(null, "Invalid birthday date");
                            }
                            else if ((0 < Integer.parseInt(input_dobD) || 31 > Integer.parseInt(input_dobM)) && input_dobM.equals(2) && input_dobM.equals(4) && input_dobM.equals(6) && input_dobM.equals(9) && input_dobM.equals(11) && input_dobM.equals(10))
                            {
                                JOptionPane.showMessageDialog(null, "Invalid birthday date");
                            }
                            else if (!((1900 < Integer.parseInt(input_dobY)) && 2021 > Integer.parseInt(input_dobY)))
                            {
                                JOptionPane.showMessageDialog(null, "Invalid birthday year");
                            }
                            else
                            {
                                String input_dob = (input_dobM + input_dobD + input_dobY);
                                String queryAddToPending = "INSERT INTO PENDING_TASKS (TASK_ID, TASK_NAME, TASK_STATUS, PENDING_USERNAME, PENDING_PASSWORD, PENDING_USERTYPE, PENDING_CLIENT_NAMEFIRST, PENDING_CLIENT_NAMELAST, PENDING_DATE_OF_BIRTH, READ_STATUS) VALUES ('" + assignIDstring + "', 'ACCOUNT CREATION' , 'PENDING', '" + input_CUsername + "', '" + input_CPass_Encrypted + "', '" + input_Usertype + "', '" + input_FName +
                                        "', '" + input_LName + "', " + input_dob + ", 'UNREAD');";

                                stm.executeUpdate(queryAddToPending);

                                JOptionPane.showMessageDialog(null, "Request has been successfully submitted.");
                                field_CUsername.setText("");
                                field_CPass.setText("");
                                field_FName.setText("");
                                field_LName.setText("");
                                field_dobD.setText("");
                                field_dobM.setText("");
                                field_dobY.setText("");
                                field_Username.setText("");
                                field_Password.setText("");
                                mainPanelLayout.show(masterPanel, "1"); //Switches back to panel 1
                            }
                        }
                    }
                    catch (Exception errorSomewhere)
                    {
                        JOptionPane.showMessageDialog(null, "Error has occurred while sending data.\n" + errorSomewhere);
                    }
                }
                catch (Exception createAccException)
                {
                    JOptionPane.showMessageDialog(null, "Error has occurred while creating account.\n" + createAccException);
                }
            }
        });

        /*--Admin--*/
        btn_LogOut.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                field_CUsername.setText("");
                field_CPass.setText("");
                field_FName.setText("");
                field_LName.setText("");
                field_dobD.setText("");
                field_dobM.setText("");
                field_dobY.setText("");
                field_Username.setText("");
                field_Password.setText("");

                currentUser = null;
                mainPanelLayout.show(masterPanel, "1");
            }
        });

        btn_AdmTasks.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT TASK_ID, TASK_NAME, TASK_STATUS, PENDING_USERNAME, PENDING_USERTYPE, PENDING_CLIENT_NAMEFIRST, PENDING_CLIENT_NAMELAST, PENDING_DATE_OF_BIRTH FROM PENDING_TASKS"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);
                    /*while(resultPendTask.next())
                    {
                        Object[] objects = new Object[colNo];
                        for(int i=0;i<colNo;i++)
                        {
                            objects[i]=resultPendTask.getObject(i+1);
                        }
                    }*/
                    table_AdminPendingTable.setModel(DbUtils.resultSetToTableModel(resultPendTask));
                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
                adminDashBoardLayout.show(panelAdmDashboard, "3a");
            }
        });
        btn_Read.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String queryPend = "UPDATE PENDING_TASKS SET READ_STATUS = 'READ'";
                    stm.executeUpdate(queryPend);
                    JOptionPane.showMessageDialog(null, "Status updated.");
                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, f);
                }
            }
        });

        btn_RefreshAdminPendingTable.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT TASK_ID, TASK_NAME, TASK_STATUS, PENDING_USERNAME, PENDING_USERTYPE, PENDING_CLIENT_NAMEFIRST, PENDING_CLIENT_NAMELAST, PENDING_DATE_OF_BIRTH FROM PENDING_TASKS"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);

                    table_AdminPendingTable.setModel(DbUtils.resultSetToTableModel(resultPendTask));

                    String queryNotification = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE READ_STATUS='UNREAD'"; //Counts how many unread status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryNotification); //IT'S AN EQUAL, LOUIE, NOT A MINUS

                    resultPendingCount.first();

                    text_Notifications.setText(resultPendingCount.getString("total") + " NEW NOTIFICATIONS"); //Slaps the pending unread to the notification text


                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, "Error. \n" + f);
                }
            }
        });
        btn_AdmHome.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getContentPane().remove(panelAdminTaskTable);
                adminDashBoardLayout.show(panelAdmDashboard, "3b");
            }
        });

        //todo (aloysius_w) - Finish approval
        btn_Approve.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String queryGetAllData = "SELECT * FROM PENDING_TASKS";
                    ResultSet AccountCreateData = stm.executeQuery(queryGetAllData);

                    AccountCreateData.last(); //this gets the first result in the thing
                    String pendingUsername = AccountCreateData.getString("PENDING_USERNAME");
                    String pendingPassword = AccountCreateData.getString("PENDING_PASSWORD");
                    String pendingUsertype = AccountCreateData.getString("PENDING_USERTYPE");
                    String pendingNameFirst = AccountCreateData.getString("PENDING_CLIENT_NAMEFIRST");
                    String pendingNameLast = AccountCreateData.getString("PENDING_CLIENT_NAMELAST");
                    int pendingDOB = AccountCreateData.getInt("PENDING_DATE_OF_BIRTH");

                    String querySendAllThisShit = "INSERT INTO CLIENT_INFO(CLIENT_USERNAME, CLIENT_PASSWORD, CLIENT_USERTYPE, CLIENT_NAMEFIRST, CLIENT_NAMELAST, DATE_OF_BIRTH) VALUES ('" + pendingUsername + "', '" + pendingPassword + "', '" + pendingUsertype + "', '" + pendingNameFirst + "', '" + pendingNameLast + "', '" + pendingDOB + "')";
                    stm.executeUpdate(querySendAllThisShit);

                    String queryTaskID = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE TASK_STATUS = 'PENDING'"; //Counts how many pending status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryTaskID);
                    resultPendingCount.first();
                    String pendingCountID = resultPendingCount.getString("total") + "-PENDING";
                    String queryDelete = "DELETE FROM PENDING_TASKS WHERE TASK_ID = '" + pendingCountID + "'";
                    stm.executeUpdate(queryDelete);

                    JOptionPane.showMessageDialog(null, "Username @" + pendingUsername + " has been approved.");
                }
                catch(Exception f)
                {
                    JOptionPane.showMessageDialog(null, f);
                }
            }
        });
        btn_Deny.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String queryTaskID = "SELECT COUNT(*) AS total FROM PENDING_TASKS WHERE TASK_STATUS = 'PENDING'"; //Counts how many pending status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryTaskID);
                    resultPendingCount.first();

                    String pendingID = resultPendingCount.getString("total") + "-PENDING";
                    String getUsername = "SELECT PENDING_USERNAME FROM PENDING_TASKS WHERE TASK_ID = '" + pendingID + "'";
                    String queryDelete = "DELETE FROM PENDING_TASKS WHERE TASK_ID = '" + pendingID + "'";

                    ResultSet resultGetUsername = stm.executeQuery(getUsername);
                    resultGetUsername.first();
                    String gotUsername = resultGetUsername.getString("PENDING_USERNAME");

                    stm.executeUpdate(queryDelete);
                    JOptionPane.showMessageDialog(null, "Username @" + gotUsername + " has been denied");

                }
                catch(Exception f)
                {
                    JOptionPane.showMessageDialog(null, f);
                }
            }
        });
        /*--User--*/
        btn_UserOrder.addActionListener(new ActionListener()
        { //placing order button
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection orderTaskQuery = dbConnector.dbConnection;
                    Statement stm = orderTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String getCountQuery = "SELECT COUNT(*) AS total FROM CLIENT_ORDERS WHERE CLIENT_USERNAME = '" + currentUser + "'";
                    ResultSet countQuery = stm.executeQuery(getCountQuery);
                    countQuery.first();
                    String orderIDGenerated = countQuery.getString("total") + "-ORDER-" + currentUser;

                    String queryOrder = "insert into CLIENT_ORDERS (CLIENT_ORDER_ID, CLIENT_USERNAME, CLIENT_ADDRESS, CLIENT_ORDER, CLIENT_QUANTITY, ORDER_STATUS) values ('" + orderIDGenerated + "', '" + currentUser + "', '" + field_UserOrderAddress.getText() + "', '" + field_UserOrder.getText() + "', " + field_UserOrderCount.getText() + ",'ACTIVE')";
                    stm.executeUpdate(queryOrder);
                    JOptionPane.showMessageDialog(null, "Order Placed!!");
                }
                catch (Exception m)
                {
                    JOptionPane.showMessageDialog(null, m);
                }
            }
        });
        btn_UserNewOrder.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                userDashBoardLayout.show(panelUserDashboard, "4c");
            }
        });

        btn_UserRecords.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT * FROM CLIENT_RECORDS_LOG WHERE CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);
                    table_UserOrderHistory.setModel(DbUtils.resultSetToTableModel(resultPendTask));

                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
                getContentPane().remove(panelUserOrderTable);
                userDashBoardLayout.show(panelUserDashboard, "4a");
            }
        });
        btn_RefreshUserOrder.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT * FROM CLIENT_RECORDS_LOG WHERE CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);
                    table_UserOrderHistory.setModel(DbUtils.resultSetToTableModel(resultPendTask));

                    String queryNotification = "SELECT COUNT(*) AS total FROM CLIENT_RECORDS_LOG WHERE READ_STATUS='UNREAD' AND CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many unread status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryNotification); //IT'S AN EQUAL, LOUIE, NOT A MINUS

                    resultPendingCount.first();

                    text_NotificationsUser.setText(resultPendingCount.getString("total") + " NEW NOTIFICATIONS"); //Slaps the pending unread to the notification text


                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, "Error. \n" + f);
                }
            }
        });
        btn_ReadOrder.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String queryPend = "UPDATE CLIENT_RECORDS SET READ_STATUS = 'READ' WHERE CLIENT_USERNAME = '" + currentUser + "'";
                    stm.executeUpdate(queryPend);
                    JOptionPane.showMessageDialog(null, "Status updated.");
                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, f);
                }
            }
        });
        btn_UserSwitchRecords.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT * FROM CLIENT_RECORDS_LOG WHERE CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);
                    table_UserOrderHistory.setModel(DbUtils.resultSetToTableModel(resultPendTask));

                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
                getContentPane().remove(panelUserOrderTable);
                userDashBoardLayout.show(panelUserDashboard, "4a");
            }
        });
        btn_UserSwitchOrder.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPendingTask = "SELECT * FROM CLIENT_ORDERS WHERE CLIENT_USERNAME = '" + currentUser + "'"; //Counts how many pending status there is

                    ResultSet resultPendTask = stm.executeQuery(queryPendingTask);
                    table_UserOrderHistory.setModel(DbUtils.resultSetToTableModel(resultPendTask));

                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
                getContentPane().remove(panelUserOrderTable);
                userDashBoardLayout.show(panelUserDashboard, "4a");
            }
        });
        btn_UserHome.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                getContentPane().remove(panelUserOrderTable);
                userDashBoardLayout.show(panelUserDashboard, "4b");
            }
        });


        btn_LogOutUser.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currentUser = null;
                field_Username.setText("");
                field_Password.setText("");

                mainPanelLayout.show(masterPanel, "1");
            }
        });

        /*--Physician--*/
        btn_PhysRecords.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPhys = "SELECT * FROM CLIENT_RECORDS WHERE MANAGING_PHYSICIAN = '" + currentUser + "'";
                    ResultSet resultPhys = stm.executeQuery(queryPhys);
                    table_PhysRecords.setModel(DbUtils.resultSetToTableModel(resultPhys));
                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
                physDashBoardLayout.show(panelPhysDashboard, "6a");

            }
        });
        btn_PhysSwitchRecords.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPhys = "SELECT * FROM CLIENT_RECORDS WHERE MANAGING_PHYSICIAN = '" + currentUser + "'";
                    ResultSet resultPhys = stm.executeQuery(queryPhys);
                    table_PhysRecords.setModel(DbUtils.resultSetToTableModel(resultPhys));
                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
            }
        });
        btn_PhysSwitchLog.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement
                    String queryPhys = "SELECT * FROM CLIENT_RECORDS_LOG WHERE MANAGING_PHYSICIAN = '" + currentUser + "'";
                    ResultSet resultPhys = stm.executeQuery(queryPhys);
                    table_PhysRecords.setModel(DbUtils.resultSetToTableModel(resultPhys));
                }
                catch (Exception errorFetch)
                {
                    JOptionPane.showMessageDialog(null, "Something went wrong while fetching the table. \n" + errorFetch);
                }
            }
        });
        btn_RefreshPhys.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); //Prepares SQL statement

                    String queryPendingTask = "SELECT * FROM CLIENT_RECORDS WHERE MANAGING_PHYSICIAN = '" + currentUser + "'"; //Counts how many pending status there is
                    ResultSet resultPhys = stm.executeQuery(queryPendingTask);
                    table_PhysRecords.setModel(DbUtils.resultSetToTableModel(resultPhys));

                    String queryRefreshPhysLog = "SELECT * FROM CLIENT_RECORDS_LOG WHERE MANAGING_PHYSICIAN ='" + currentUser + "'";
                    ResultSet resultPhysLog = stm.executeQuery(queryRefreshPhysLog);
                    table_PhysRecords.setModel(DbUtils.resultSetToTableModel(resultPhysLog));

                    String queryNotification = "SELECT COUNT(*) AS total FROM CLIENT_RECORDS WHERE READ_STATUS='UNREAD' AND MANAGING_PHYSICIAN = '" + currentUser + "'"; //Counts how many unread status there is
                    ResultSet resultPendingCount = stm.executeQuery(queryNotification);

                    resultPendingCount.first();

                    text_NotificationsUser.setText(resultPendingCount.getString("total") + " NEW NOTIFICATIONS"); //Slaps the pending unread to the notification text


                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, "Error. \n" + f);
                }
            }
        });

        btn_PhysHome.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                physDashBoardLayout.show(panelPhysDashboard, "6b");
            }
        });

        btn_ReadPhys.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection adminTaskQuery = dbConnector.dbConnection;
                    Statement stm = adminTaskQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String queryPend = "UPDATE CLIENT_RECORDS SET READ_STATUS = 'READ' WHERE MANAGING_PHYSICIAN = '" + currentUser + "'";
                    stm.executeUpdate(queryPend);
                    JOptionPane.showMessageDialog(null, "Status updated.");
                }
                catch (Exception f)
                {
                    JOptionPane.showMessageDialog(null, f);
                }
            }
        });

        btn_LogOutPhys.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                currentUser = null;
                field_Username.setText("");
                field_Password.setText("");

                mainPanelLayout.show(masterPanel, "1");
            }
        });
        btn_PhysUpdate.addActionListener(new ActionListener()
        { //updates clients records from physician page
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection orderHistoryQuery = dbConnector.dbConnection;
                    Statement stm = orderHistoryQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    /*update current records*/
                    String queryOrder = "update CLIENT_RECORDS set LAST_UPDATE = '" + field_PhysLU.getText() + "', CURRENT_PRESCRIPTION = '" + field_PhysPresc.getText() + "' where CLIENT_USERNAME = '" + field_PhysWhoClient.getText() + "' AND MANAGING_PHYSICIAN = '" + currentUser + "'";
                    stm.executeUpdate(queryOrder);

                    /*insert into log*/
                    String getClientCount = "SELECT COUNT(*) AS 'total' FROM CLIENT_RECORDS_LOG WHERE CLIENT_USERNAME = '" + field_PhysWhoClient.getText() + "'";
                    ResultSet physClientCountTotal = stm.executeQuery(getClientCount);
                    String generatedOrderID = physClientCountTotal.getString("total") + "-" + field_PhysWhoClient.getText();


                    String queryOrderLog = "INSERT INTO CLIENT_RECORDS_LOG(RECORD_ID, MANAGING_PHYSICIAN, CLIENT_USERNAME, LAST_UPDATE, CURRENT_PRESCRIPTION, READ_STATUS) VALUES('" + generatedOrderID + "', '" + currentUser + "', '" + field_PhysWhoClient.getText() + "', '" + field_PhysLU.getText() + "', '" + field_PhysPresc + "', 'UNREAD')";
                    stm.executeQuery(queryOrderLog);

                    JOptionPane.showMessageDialog(null, "Data Updated!!");

                }
                catch (Exception m)
                {
                    JOptionPane.showMessageDialog(null, m);
                }
            }
        });

        /*--Provider--*/
        btn_ProviderActive.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection providerActiveQuery = dbConnector.dbConnection;
                    Statement stm = providerActiveQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String queryActive = "select * from CLIENT_ORDERS where ORDER_STATUS = 'ACTIVE'";
                    ResultSet resultPhys = stm.executeQuery(queryActive);
                    ProviderActiveReqTable.setModel(DbUtils.resultSetToTableModel(resultPhys));
                }
                catch (Exception m)
                {
                    JOptionPane.showMessageDialog(null, m);
                }
            }
        });
        btn_ProviderCompleted.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Connection providerActiveQuery = dbConnector.dbConnection;
                    Statement stm = providerActiveQuery.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                    String queryActive = "select * from CLIENT_ORDERS where ORDER_STATUS = 'COMPLETED'";
                    ResultSet resultPhys = stm.executeQuery(queryActive);
                    ProviderActiveReqTable.setModel(DbUtils.resultSetToTableModel(resultPhys));
                }
                catch (Exception m)
                {
                    JOptionPane.showMessageDialog(null, m);
                }
            }
        });

        btn_LogOutProvider.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                field_CUsername.setText("");
                field_CPass.setText("");
                field_FName.setText("");
                field_LName.setText("");
                field_dobD.setText("");
                field_dobM.setText("");
                field_dobY.setText("");
                field_Username.setText("");
                field_Password.setText("");

                currentUser = null;
                mainPanelLayout.show(masterPanel, "1");
            }
        });


    }
    public static void main(String[] args)
    {
        CoreHome coreFrame = new CoreHome(); //Initializes JFrame
        try
        {
            UIManager.setLookAndFeel(new DarculaLaf()); //Sets the Look and Feel, todo (aloysius_w) - Get rid of that big red warning text
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        coreFrame.setContentPane(new CoreHome().masterPanel); //masterPanel serves as the backdrop container for everything
        coreFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Sets behavior to end program when exit button is pressed
        coreFrame.setVisible(true);
        coreFrame.pack(); //Sets size based on content

    }

    private void createUIComponents() {
        // TODO: add custom component creation code here
    }

    private void initComponents()
    {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Aloysius Arno Wiputra
        masterPanel = new JPanel();
        panelMain = new JPanel();
        panelLoginFields = new JPanel();
        field_Username = new JTextField();
        field_Password = new JPasswordField();
        panelWelcome = new JPanel();
        text_Welcome = new JLabel();
        panelLoginBottom = new JPanel();
        btn_Login = new JButton();
        btn_NewCreate = new JButton();
        panelAccCreate = new JPanel();
        text_FName = new JLabel();
        field_FName = new JTextField();
        field_LName = new JTextField();
        field_CUsername = new JTextField();
        text_CUsername = new JLabel();
        text_LName = new JLabel();
        text_CPass = new JLabel();
        field_CPass = new JPasswordField();
        String [] fieldStrings = {"Admin", "User", "Provider", "Physician"};
        field_Scroll = new JComboBox(fieldStrings);
        text_WhoAreYou = new JLabel();
        btn_CreateAcc = new JButton();
        btn_BackToLogin = new JButton();
        btn_ResetFields = new JButton();
        field_dobM = new JTextField();
        text_CUsername2 = new JLabel();
        field_dobD = new JTextField();
        field_dobY = new JTextField();
        panelPageAdmin = new JPanel();
        text_DashWelcome = new JLabel();
        panelAdmDashboard = new JPanel();
        panelAdminTaskTable = new JPanel();
        scroll_AdminPendingTable = new JScrollPane();
        String[] tableColumnNames = {"TASK ID", "TASK NAME", "TASK STATUS", "USERTYPE", "USERNAME", "FIRST NAME", "LAST NAME", "DATE OF BIRTH"};
        table_AdminPendingTable = new JTable();
        btn_Read = new JButton();
        btn_Approve = new JButton();
        btn_RefreshAdminPendingTable = new JButton();
        btn_Deny = new JButton();
        panelAdminHome = new JPanel();
        label_descName2 = new JLabel();
        text_NumUsers = new JLabel();
        label_descName3 = new JLabel();
        text_NumAccCreate = new JLabel();
        panelAdmNavigation = new JPanel();
        btn_AdmTasks = new JButton();
        btn_AdmHome = new JButton();
        btn_LogOut = new JButton();
        text_Notifications = new JLabel();
        panelPageUser = new JPanel();
        text_DashWelcomeUser = new JLabel();
        panelUserDashboard = new JPanel();
        panelUserHome = new JPanel();
        label_descName = new JLabel();
        label_NameLASTFIRST = new JLabel();
        label_descPhys = new JLabel();
        label_PhysLASTFIRST = new JLabel();
        label_desc_LU = new JLabel();
        label_LU = new JLabel();
        panelUserOrder = new JPanel();
        field_UserOrder = new JTextField();
        field_descUserOrder = new JLabel();
        field_descOrderAddress = new JLabel();
        field_UserOrderAddress = new JTextField();
        btn_UserOrder = new JButton();
        field_UserOrderCount = new JTextField();
        label_descUserOrderCount = new JLabel();
        panelUserOrderTable = new JPanel();
        scroll_UserOrderHistory = new JScrollPane();
        table_UserOrderHistory = new JTable();
        btn_ReadOrder = new JButton();
        btn_RefreshUserOrder = new JButton();
        btn_UserSwitchRecords = new JButton();
        btn_UserSwitchOrder = new JButton();
        panelUserNavigation = new JPanel();
        btn_UserRecords = new JButton();
        btn_UserNewOrder = new JButton();
        btn_UserHome = new JButton();
        btn_LogOutUser = new JButton();
        text_NotificationsUser = new JLabel();
        panelPagePhysician = new JPanel();
        text_DashWelcomePhysician = new JLabel();
        panelPhysDashboard = new JPanel();
        panelPhysHome = new JPanel();
        field_PhysWhoClient = new JTextField();
        label_descPhysWhoClient = new JLabel();
        label_descPhysLU = new JLabel();
        field_PhysLU = new JTextField();
        label_descPhysPresc = new JLabel();
        field_PhysPresc = new JTextField();
        btn_PhysUpdate = new JButton();
        panelPhysManaged = new JPanel();
        panelPhysNavigation = new JPanel();
        btn_PhysSwitchRecords = new JButton();
        btn_PhysSwitchLog = new JButton();
        btn_ReadPhys = new JButton();
        btn_RefreshPhys = new JButton();
        panelPhysTable = new JPanel();
        scroll_PhysTable = new JScrollPane();
        table_PhysRecords = new JTable();
        text_NotificationsPhys = new JLabel();
        panelPhysNav = new JPanel();
        btn_PhysRecords = new JButton();
        btn_PhysHome = new JButton();
        btn_LogOutPhys = new JButton();
        panelPageProvider = new JPanel();
        text_DashWelcomeProvider = new JLabel();
        panelProviderDashboard = new JPanel();
        panelProviderNavigation = new JPanel();
        text_NotificationsProvider = new JLabel();
        panelProviderMain = new JPanel();
        btn_ProviderActive = new JButton();
        btn_ProviderCompleted = new JButton();
        btn_LogOutProvider = new JButton();
        panelProviderActive = new JPanel();
        scrollPane1 = new JScrollPane();
        ProviderActiveReqTable = new JTable();

        //======== masterPanel ========
        {
            masterPanel.setForeground(new Color(51, 51, 51));
            masterPanel.setBorder ( new javax . swing. border .CompoundBorder ( new javax . swing. border .TitledBorder ( new javax . swing. border
            .EmptyBorder ( 0, 0 ,0 , 0) ,  "" , javax. swing .border . TitledBorder. CENTER ,javax
            . swing. border .TitledBorder . BOTTOM, new java. awt .Font ( "Dia\u006cog", java .awt . Font. BOLD ,
            12 ) ,java . awt. Color .red ) ,masterPanel. getBorder () ) ); masterPanel. addPropertyChangeListener( new java. beans
            .PropertyChangeListener ( ){ @Override public void propertyChange (java . beans. PropertyChangeEvent e) { if( "bord\u0065r" .equals ( e.
            getPropertyName () ) )throw new RuntimeException( ) ;} } );
            masterPanel.setLayout(new CardLayout());

            //======== panelMain ========
            {
                panelMain.setForeground(new Color(51, 51, 51));

                //======== panelLoginFields ========
                {

                    //---- field_Username ----
                    field_Username.setHorizontalAlignment(SwingConstants.CENTER);
                    field_Username.setToolTipText("Enter Username");

                    //---- field_Password ----
                    field_Password.setToolTipText("Enter Password");
                    field_Password.setHorizontalAlignment(SwingConstants.CENTER);

                    GroupLayout panelLoginFieldsLayout = new GroupLayout(panelLoginFields);
                    panelLoginFields.setLayout(panelLoginFieldsLayout);
                    panelLoginFieldsLayout.setHorizontalGroup(
                        panelLoginFieldsLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panelLoginFieldsLayout.createSequentialGroup()
                                .addGap(163, 163, 163)
                                .addGroup(panelLoginFieldsLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(field_Username)
                                    .addComponent(field_Password))
                                .addGap(163, 163, 163))
                    );
                    panelLoginFieldsLayout.setVerticalGroup(
                        panelLoginFieldsLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panelLoginFieldsLayout.createSequentialGroup()
                                .addComponent(field_Username, GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(field_Password, GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                                .addGap(7, 7, 7))
                    );
                }

                //======== panelWelcome ========
                {

                    //---- text_Welcome ----
                    text_Welcome.setText("Welcome");
                    text_Welcome.setFont(text_Welcome.getFont().deriveFont(text_Welcome.getFont().getSize() + 11f));
                    text_Welcome.setHorizontalAlignment(SwingConstants.CENTER);

                    GroupLayout panelWelcomeLayout = new GroupLayout(panelWelcome);
                    panelWelcome.setLayout(panelWelcomeLayout);
                    panelWelcomeLayout.setHorizontalGroup(
                        panelWelcomeLayout.createParallelGroup()
                            .addGroup(panelWelcomeLayout.createSequentialGroup()
                                .addGap(162, 162, 162)
                                .addComponent(text_Welcome, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                .addGap(164, 164, 164))
                    );
                    panelWelcomeLayout.setVerticalGroup(
                        panelWelcomeLayout.createParallelGroup()
                            .addGroup(panelWelcomeLayout.createSequentialGroup()
                                .addGap(110, 110, 110)
                                .addComponent(text_Welcome, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(45, 45, 45))
                    );
                }

                //======== panelLoginBottom ========
                {
                    panelLoginBottom.setLayout(new MigLayout(
                        "fillx,rtl,insets 20 90 0 90,hidemode 3,gap 5 5",
                        // columns
                        "[fill]" +
                        "[fill]",
                        // rows
                        "[fill]"));

                    //---- btn_Login ----
                    btn_Login.setText("LOGIN");
                    panelLoginBottom.add(btn_Login, "cell 0 0");

                    //---- btn_NewCreate ----
                    btn_NewCreate.setText("New User?");
                    panelLoginBottom.add(btn_NewCreate, "cell 1 0");
                }

                GroupLayout panelMainLayout = new GroupLayout(panelMain);
                panelMain.setLayout(panelMainLayout);
                panelMainLayout.setHorizontalGroup(
                    panelMainLayout.createParallelGroup()
                        .addGroup(panelMainLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelMainLayout.createParallelGroup()
                                .addComponent(panelWelcome, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelLoginFields, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelLoginBottom, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                );
                panelMainLayout.setVerticalGroup(
                    panelMainLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(panelWelcome, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelLoginFields, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelLoginBottom, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
            }
            masterPanel.add(panelMain, "card6");

            //======== panelAccCreate ========
            {
                panelAccCreate.setForeground(new Color(51, 51, 51));
                panelAccCreate.setPreferredSize(new Dimension(480, 406));
                panelAccCreate.setMaximumSize(new Dimension(32779, 32987));
                panelAccCreate.setMinimumSize(new Dimension(434, 379));

                //---- text_FName ----
                text_FName.setText("First Name");

                //---- text_CUsername ----
                text_CUsername.setText("Username");

                //---- text_LName ----
                text_LName.setText("Last Name");

                //---- text_CPass ----
                text_CPass.setText("Password");

                //---- text_WhoAreYou ----
                text_WhoAreYou.setText("Who are you?");

                //---- btn_CreateAcc ----
                btn_CreateAcc.setText("CREATE");

                //---- btn_BackToLogin ----
                btn_BackToLogin.setText("BACK");

                //---- btn_ResetFields ----
                btn_ResetFields.setText("RESET");

                //---- text_CUsername2 ----
                text_CUsername2.setText("Date of Birth (MM/DD/YYYY)");

                GroupLayout panelAccCreateLayout = new GroupLayout(panelAccCreate);
                panelAccCreate.setLayout(panelAccCreateLayout);
                panelAccCreateLayout.setHorizontalGroup(
                    panelAccCreateLayout.createParallelGroup()
                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelAccCreateLayout.createParallelGroup()
                                .addGroup(panelAccCreateLayout.createSequentialGroup()
                                    .addGroup(panelAccCreateLayout.createParallelGroup()
                                        .addComponent(field_CPass)
                                        .addComponent(field_CUsername)
                                        .addComponent(btn_CreateAcc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                                            .addComponent(text_CUsername2)
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                                            .addComponent(field_dobM, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(field_dobD, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(field_dobY)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelAccCreateLayout.createParallelGroup()
                                        .addComponent(field_Scroll)
                                        .addComponent(btn_BackToLogin, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btn_ResetFields, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(panelAccCreateLayout.createSequentialGroup()
                                    .addGroup(panelAccCreateLayout.createParallelGroup()
                                        .addComponent(text_CPass)
                                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                                            .addComponent(text_CUsername)
                                            .addGap(263, 263, 263)
                                            .addComponent(text_WhoAreYou))
                                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                                            .addGroup(panelAccCreateLayout.createParallelGroup()
                                                .addComponent(field_FName, GroupLayout.PREFERRED_SIZE, 236, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(text_FName))
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(panelAccCreateLayout.createParallelGroup()
                                                .addComponent(text_LName)
                                                .addComponent(field_LName, GroupLayout.PREFERRED_SIZE, 226, GroupLayout.PREFERRED_SIZE))))
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addContainerGap())
                );
                panelAccCreateLayout.setVerticalGroup(
                    panelAccCreateLayout.createParallelGroup()
                        .addGroup(panelAccCreateLayout.createSequentialGroup()
                            .addGroup(panelAccCreateLayout.createParallelGroup()
                                .addGroup(panelAccCreateLayout.createSequentialGroup()
                                    .addGap(22, 22, 22)
                                    .addGroup(panelAccCreateLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(text_FName)
                                        .addComponent(text_LName))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelAccCreateLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(field_FName, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(field_LName, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(panelAccCreateLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(text_CUsername)
                                        .addComponent(text_WhoAreYou))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelAccCreateLayout.createParallelGroup()
                                        .addComponent(field_CUsername, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(field_Scroll))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(text_CPass)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(field_CPass, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                    .addGap(75, 75, 75))
                                .addGroup(panelAccCreateLayout.createSequentialGroup()
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(text_CUsername2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelAccCreateLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(field_dobM, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(field_dobD, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(field_dobY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))))
                            .addComponent(btn_ResetFields)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(panelAccCreateLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btn_BackToLogin)
                                .addComponent(btn_CreateAcc))
                            .addContainerGap(12, Short.MAX_VALUE))
                );
            }
            masterPanel.add(panelAccCreate, "card2");

            //======== panelPageAdmin ========
            {
                panelPageAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                //---- text_DashWelcome ----
                text_DashWelcome.setText("Welcome");
                text_DashWelcome.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
                text_DashWelcome.setEnabled(false);

                //======== panelAdmDashboard ========
                {
                    panelAdmDashboard.setBackground(UIManager.getColor("Button.shadowColor"));

                    //======== panelAdminTaskTable ========
                    {

                        //======== scroll_AdminPendingTable ========
                        {
                            scroll_AdminPendingTable.setViewportView(table_AdminPendingTable);
                        }

                        //---- btn_Read ----
                        btn_Read.setText("READ");

                        //---- btn_Approve ----
                        btn_Approve.setText("APPROVE");

                        //---- btn_RefreshAdminPendingTable ----
                        btn_RefreshAdminPendingTable.setText("REFRESH");

                        //---- btn_Deny ----
                        btn_Deny.setText("DENY");

                        GroupLayout panelAdminTaskTableLayout = new GroupLayout(panelAdminTaskTable);
                        panelAdminTaskTable.setLayout(panelAdminTaskTableLayout);
                        panelAdminTaskTableLayout.setHorizontalGroup(
                            panelAdminTaskTableLayout.createParallelGroup()
                                .addGroup(panelAdminTaskTableLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelAdminTaskTableLayout.createParallelGroup()
                                        .addComponent(scroll_AdminPendingTable, GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                                        .addGroup(panelAdminTaskTableLayout.createSequentialGroup()
                                            .addComponent(btn_Read)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(btn_Approve)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(btn_Deny)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 153, Short.MAX_VALUE)
                                            .addComponent(btn_RefreshAdminPendingTable)
                                            .addContainerGap())))
                        );
                        panelAdminTaskTableLayout.setVerticalGroup(
                            panelAdminTaskTableLayout.createParallelGroup()
                                .addGroup(panelAdminTaskTableLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(scroll_AdminPendingTable, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addGroup(panelAdminTaskTableLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_Read)
                                        .addComponent(btn_Approve)
                                        .addComponent(btn_RefreshAdminPendingTable)
                                        .addComponent(btn_Deny))
                                    .addGap(24, 24, 24))
                        );
                    }

                    //======== panelAdminHome ========
                    {

                        //---- label_descName2 ----
                        label_descName2.setText("Current Number of Users");

                        //---- text_NumUsers ----
                        text_NumUsers.setText("[PH]numUsers");
                        text_NumUsers.setFont(new Font("Segoe UI", Font.PLAIN, 20));

                        //---- label_descName3 ----
                        label_descName3.setText("You have:");

                        //---- text_NumAccCreate ----
                        text_NumAccCreate.setText("[PH]numAccCreationUsers");
                        text_NumAccCreate.setFont(new Font("Segoe UI", Font.PLAIN, 20));

                        GroupLayout panelAdminHomeLayout = new GroupLayout(panelAdminHome);
                        panelAdminHome.setLayout(panelAdminHomeLayout);
                        panelAdminHomeLayout.setHorizontalGroup(
                            panelAdminHomeLayout.createParallelGroup()
                                .addGroup(panelAdminHomeLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelAdminHomeLayout.createParallelGroup()
                                        .addComponent(label_descName3)
                                        .addComponent(text_NumAccCreate, GroupLayout.PREFERRED_SIZE, 496, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(text_NumUsers, GroupLayout.PREFERRED_SIZE, 496, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label_descName2))
                                    .addContainerGap(43, Short.MAX_VALUE))
                        );
                        panelAdminHomeLayout.setVerticalGroup(
                            panelAdminHomeLayout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, panelAdminHomeLayout.createSequentialGroup()
                                    .addGap(23, 23, 23)
                                    .addComponent(label_descName3)
                                    .addGap(6, 6, 6)
                                    .addComponent(text_NumAccCreate, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                    .addGap(35, 35, 35)
                                    .addComponent(label_descName2)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_NumUsers, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(87, Short.MAX_VALUE))
                        );
                    }

                    GroupLayout panelAdmDashboardLayout = new GroupLayout(panelAdmDashboard);
                    panelAdmDashboard.setLayout(panelAdmDashboardLayout);
                    panelAdmDashboardLayout.setHorizontalGroup(
                        panelAdmDashboardLayout.createParallelGroup()
                            .addGroup(panelAdmDashboardLayout.createParallelGroup()
                                .addGroup(panelAdmDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelAdminHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGroup(panelAdmDashboardLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(panelAdminTaskTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                    );
                    panelAdmDashboardLayout.setVerticalGroup(
                        panelAdmDashboardLayout.createParallelGroup()
                            .addGroup(panelAdmDashboardLayout.createParallelGroup()
                                .addGroup(panelAdmDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelAdminHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGroup(panelAdmDashboardLayout.createSequentialGroup()
                                .addGap(0, 2, Short.MAX_VALUE)
                                .addComponent(panelAdminTaskTable, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 2, Short.MAX_VALUE))
                    );
                }

                //======== panelAdmNavigation ========
                {
                    panelAdmNavigation.setLayout(new GridLayout());

                    //---- btn_AdmTasks ----
                    btn_AdmTasks.setText("TASKS");
                    panelAdmNavigation.add(btn_AdmTasks);

                    //---- btn_AdmHome ----
                    btn_AdmHome.setText("HOME");
                    panelAdmNavigation.add(btn_AdmHome);

                    //---- btn_LogOut ----
                    btn_LogOut.setText("LOG OUT");
                    panelAdmNavigation.add(btn_LogOut);
                }

                //---- text_Notifications ----
                text_Notifications.setText("0 NEW NOTIFICATIONS");
                text_Notifications.setHorizontalAlignment(SwingConstants.RIGHT);
                text_Notifications.setForeground(new Color(255, 255, 153));
                text_Notifications.setFont(new Font("MS UI Gothic", Font.PLAIN, 12));

                GroupLayout panelPageAdminLayout = new GroupLayout(panelPageAdmin);
                panelPageAdmin.setLayout(panelPageAdminLayout);
                panelPageAdminLayout.setHorizontalGroup(
                    panelPageAdminLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, panelPageAdminLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageAdminLayout.createParallelGroup()
                                .addGroup(panelPageAdminLayout.createSequentialGroup()
                                    .addComponent(text_DashWelcome, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_Notifications, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
                                .addComponent(panelAdmNavigation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                        .addComponent(panelAdmDashboard, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                );
                panelPageAdminLayout.setVerticalGroup(
                    panelPageAdminLayout.createParallelGroup()
                        .addGroup(panelPageAdminLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageAdminLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(text_DashWelcome, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                .addComponent(text_Notifications))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelAdmDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelAdmNavigation, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
            }
            masterPanel.add(panelPageAdmin, "card3");

            //======== panelPageUser ========
            {
                panelPageUser.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                //---- text_DashWelcomeUser ----
                text_DashWelcomeUser.setText("Welcome");
                text_DashWelcomeUser.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
                text_DashWelcomeUser.setEnabled(false);

                //======== panelUserDashboard ========
                {
                    panelUserDashboard.setBackground(UIManager.getColor("Button.shadowColor"));

                    //======== panelUserHome ========
                    {

                        //---- label_descName ----
                        label_descName.setText("Name");

                        //---- label_NameLASTFIRST ----
                        label_NameLASTFIRST.setText("LAST, FIRST");
                        label_NameLASTFIRST.setFont(new Font("Segoe UI", Font.PLAIN, 20));

                        //---- label_descPhys ----
                        label_descPhys.setText("Current Physician");

                        //---- label_PhysLASTFIRST ----
                        label_PhysLASTFIRST.setText("LAST, FIRST");
                        label_PhysLASTFIRST.setFont(new Font("Segoe UI", Font.PLAIN, 20));

                        //---- label_desc_LU ----
                        label_desc_LU.setText("Last Update");

                        //---- label_LU ----
                        label_LU.setText("UPDATE_LAST");
                        label_LU.setFont(new Font("Segoe UI", Font.PLAIN, 20));

                        GroupLayout panelUserHomeLayout = new GroupLayout(panelUserHome);
                        panelUserHome.setLayout(panelUserHomeLayout);
                        panelUserHomeLayout.setHorizontalGroup(
                            panelUserHomeLayout.createParallelGroup()
                                .addGroup(panelUserHomeLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelUserHomeLayout.createParallelGroup()
                                        .addComponent(label_NameLASTFIRST, GroupLayout.PREFERRED_SIZE, 496, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label_descName)
                                        .addComponent(label_descPhys)
                                        .addComponent(label_PhysLASTFIRST, GroupLayout.PREFERRED_SIZE, 496, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label_desc_LU)
                                        .addComponent(label_LU, GroupLayout.PREFERRED_SIZE, 496, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(43, Short.MAX_VALUE))
                        );
                        panelUserHomeLayout.setVerticalGroup(
                            panelUserHomeLayout.createParallelGroup()
                                .addGroup(panelUserHomeLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label_descName)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(label_NameLASTFIRST, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(label_descPhys)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(label_PhysLASTFIRST, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(label_desc_LU)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(label_LU, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(62, Short.MAX_VALUE))
                        );
                    }

                    //======== panelUserOrder ========
                    {

                        //---- field_descUserOrder ----
                        field_descUserOrder.setText("Order?");
                        field_descUserOrder.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        //---- field_descOrderAddress ----
                        field_descOrderAddress.setText("Address");
                        field_descOrderAddress.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        //---- field_UserOrderAddress ----
                        field_UserOrderAddress.setHorizontalAlignment(SwingConstants.LEFT);

                        //---- btn_UserOrder ----
                        btn_UserOrder.setText("ORDER");

                        //---- label_descUserOrderCount ----
                        label_descUserOrderCount.setText("Count");
                        label_descUserOrderCount.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        GroupLayout panelUserOrderLayout = new GroupLayout(panelUserOrder);
                        panelUserOrder.setLayout(panelUserOrderLayout);
                        panelUserOrderLayout.setHorizontalGroup(
                            panelUserOrderLayout.createParallelGroup()
                                .addGroup(panelUserOrderLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelUserOrderLayout.createParallelGroup()
                                        .addGroup(panelUserOrderLayout.createSequentialGroup()
                                            .addGroup(panelUserOrderLayout.createParallelGroup()
                                                .addComponent(field_descOrderAddress)
                                                .addGroup(panelUserOrderLayout.createSequentialGroup()
                                                    .addGroup(panelUserOrderLayout.createParallelGroup()
                                                        .addComponent(field_UserOrder, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(field_descUserOrder))
                                                    .addGap(18, 18, 18)
                                                    .addGroup(panelUserOrderLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(label_descUserOrderCount, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(field_UserOrderCount))))
                                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(panelUserOrderLayout.createSequentialGroup()
                                            .addComponent(field_UserOrderAddress, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 165, Short.MAX_VALUE)
                                            .addComponent(btn_UserOrder)
                                            .addGap(51, 51, 51))))
                        );
                        panelUserOrderLayout.setVerticalGroup(
                            panelUserOrderLayout.createParallelGroup()
                                .addGroup(panelUserOrderLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelUserOrderLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(panelUserOrderLayout.createSequentialGroup()
                                            .addComponent(field_descUserOrder)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(field_UserOrder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panelUserOrderLayout.createSequentialGroup()
                                            .addComponent(label_descUserOrderCount)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(field_UserOrderCount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(field_descOrderAddress)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(panelUserOrderLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(field_UserOrderAddress, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_UserOrder))
                                    .addContainerGap(42, Short.MAX_VALUE))
                        );
                    }

                    //======== panelUserOrderTable ========
                    {

                        //======== scroll_UserOrderHistory ========
                        {
                            scroll_UserOrderHistory.setViewportView(table_UserOrderHistory);
                        }

                        //---- btn_ReadOrder ----
                        btn_ReadOrder.setText("READ");

                        //---- btn_RefreshUserOrder ----
                        btn_RefreshUserOrder.setText("REFRESH");

                        //---- btn_UserSwitchRecords ----
                        btn_UserSwitchRecords.setText("RECORDS");

                        //---- btn_UserSwitchOrder ----
                        btn_UserSwitchOrder.setText("ORDER");

                        GroupLayout panelUserOrderTableLayout = new GroupLayout(panelUserOrderTable);
                        panelUserOrderTable.setLayout(panelUserOrderTableLayout);
                        panelUserOrderTableLayout.setHorizontalGroup(
                            panelUserOrderTableLayout.createParallelGroup()
                                .addGroup(panelUserOrderTableLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelUserOrderTableLayout.createParallelGroup()
                                        .addComponent(scroll_UserOrderHistory, GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                                        .addGroup(panelUserOrderTableLayout.createSequentialGroup()
                                            .addComponent(btn_UserSwitchRecords)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btn_UserSwitchOrder)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                                            .addComponent(btn_ReadOrder)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btn_RefreshUserOrder)
                                            .addContainerGap())))
                        );
                        panelUserOrderTableLayout.setVerticalGroup(
                            panelUserOrderTableLayout.createParallelGroup()
                                .addGroup(panelUserOrderTableLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(scroll_UserOrderHistory, GroupLayout.PREFERRED_SIZE, 195, GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addGroup(panelUserOrderTableLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btn_RefreshUserOrder)
                                        .addComponent(btn_ReadOrder)
                                        .addComponent(btn_UserSwitchRecords)
                                        .addComponent(btn_UserSwitchOrder))
                                    .addGap(24, 24, 24))
                        );
                    }

                    GroupLayout panelUserDashboardLayout = new GroupLayout(panelUserDashboard);
                    panelUserDashboard.setLayout(panelUserDashboardLayout);
                    panelUserDashboardLayout.setHorizontalGroup(
                        panelUserDashboardLayout.createParallelGroup()
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(panelUserDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelUserOrderTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(panelUserDashboardLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(panelUserOrder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(44, Short.MAX_VALUE)))
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(panelUserDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelUserHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGap(0, 0, Short.MAX_VALUE)
                    );
                    panelUserDashboardLayout.setVerticalGroup(
                        panelUserDashboardLayout.createParallelGroup()
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(panelUserDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelUserOrderTable, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, panelUserDashboardLayout.createSequentialGroup()
                                    .addContainerGap(18, Short.MAX_VALUE)
                                    .addComponent(panelUserOrder, GroupLayout.PREFERRED_SIZE, 239, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panelUserDashboardLayout.createParallelGroup()
                                .addGroup(panelUserDashboardLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(panelUserHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addGap(0, 0, Short.MAX_VALUE)
                    );
                }

                //======== panelUserNavigation ========
                {
                    panelUserNavigation.setLayout(new GridLayout());

                    //---- btn_UserRecords ----
                    btn_UserRecords.setText("RECORDS");
                    panelUserNavigation.add(btn_UserRecords);

                    //---- btn_UserNewOrder ----
                    btn_UserNewOrder.setText("ORDER");
                    panelUserNavigation.add(btn_UserNewOrder);

                    //---- btn_UserHome ----
                    btn_UserHome.setText("HOME");
                    panelUserNavigation.add(btn_UserHome);

                    //---- btn_LogOutUser ----
                    btn_LogOutUser.setText("LOG OUT");
                    panelUserNavigation.add(btn_LogOutUser);
                }

                //---- text_NotificationsUser ----
                text_NotificationsUser.setText("0 NEW NOTIFICATIONS");
                text_NotificationsUser.setHorizontalAlignment(SwingConstants.RIGHT);
                text_NotificationsUser.setForeground(new Color(255, 255, 153));
                text_NotificationsUser.setFont(new Font("MS UI Gothic", Font.PLAIN, 12));

                GroupLayout panelPageUserLayout = new GroupLayout(panelPageUser);
                panelPageUser.setLayout(panelPageUserLayout);
                panelPageUserLayout.setHorizontalGroup(
                    panelPageUserLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, panelPageUserLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageUserLayout.createParallelGroup()
                                .addGroup(panelPageUserLayout.createSequentialGroup()
                                    .addComponent(text_DashWelcomeUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_NotificationsUser, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
                                .addComponent(panelUserNavigation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                        .addComponent(panelUserDashboard, GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                );
                panelPageUserLayout.setVerticalGroup(
                    panelPageUserLayout.createParallelGroup()
                        .addGroup(panelPageUserLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageUserLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(text_DashWelcomeUser, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                .addComponent(text_NotificationsUser))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelUserDashboard, GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelUserNavigation, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
            }
            masterPanel.add(panelPageUser, "card7");

            //======== panelPagePhysician ========
            {
                panelPagePhysician.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                //---- text_DashWelcomePhysician ----
                text_DashWelcomePhysician.setText("Welcome");
                text_DashWelcomePhysician.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
                text_DashWelcomePhysician.setEnabled(false);

                //======== panelPhysDashboard ========
                {
                    panelPhysDashboard.setBackground(UIManager.getColor("Button.shadowColor"));

                    //======== panelPhysHome ========
                    {

                        //---- label_descPhysWhoClient ----
                        label_descPhysWhoClient.setText("Who's your client?");
                        label_descPhysWhoClient.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        //---- label_descPhysLU ----
                        label_descPhysLU.setText("Last Update");
                        label_descPhysLU.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        //---- label_descPhysPresc ----
                        label_descPhysPresc.setText("Prescription update");
                        label_descPhysPresc.setFont(new Font("Segoe UI", Font.PLAIN, 24));

                        //---- btn_PhysUpdate ----
                        btn_PhysUpdate.setText("UPDATE");

                        GroupLayout panelPhysHomeLayout = new GroupLayout(panelPhysHome);
                        panelPhysHome.setLayout(panelPhysHomeLayout);
                        panelPhysHomeLayout.setHorizontalGroup(
                            panelPhysHomeLayout.createParallelGroup()
                                .addGroup(panelPhysHomeLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(panelPhysHomeLayout.createParallelGroup()
                                        .addComponent(field_PhysWhoClient, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label_descPhysWhoClient)
                                        .addComponent(label_descPhysLU)
                                        .addComponent(field_PhysLU, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label_descPhysPresc)
                                        .addComponent(field_PhysPresc, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btn_PhysUpdate))
                                    .addContainerGap(261, Short.MAX_VALUE))
                        );
                        panelPhysHomeLayout.setVerticalGroup(
                            panelPhysHomeLayout.createParallelGroup()
                                .addGroup(panelPhysHomeLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(label_descPhysWhoClient)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(field_PhysWhoClient, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(label_descPhysLU)
                                    .addGap(6, 6, 6)
                                    .addComponent(field_PhysLU, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(label_descPhysPresc)
                                    .addGap(6, 6, 6)
                                    .addComponent(field_PhysPresc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btn_PhysUpdate)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        );
                    }

                    //======== panelPhysManaged ========
                    {

                        //======== panelPhysNavigation ========
                        {
                            panelPhysNavigation.setLayout(new GridLayout());

                            //---- btn_PhysSwitchRecords ----
                            btn_PhysSwitchRecords.setText("CURRENT RECORDS");
                            panelPhysNavigation.add(btn_PhysSwitchRecords);

                            //---- btn_PhysSwitchLog ----
                            btn_PhysSwitchLog.setText("LOG");
                            panelPhysNavigation.add(btn_PhysSwitchLog);

                            //---- btn_ReadPhys ----
                            btn_ReadPhys.setText("READ");
                            panelPhysNavigation.add(btn_ReadPhys);

                            //---- btn_RefreshPhys ----
                            btn_RefreshPhys.setText("REFRESH");
                            panelPhysNavigation.add(btn_RefreshPhys);
                        }

                        //======== panelPhysTable ========
                        {

                            //======== scroll_PhysTable ========
                            {
                                scroll_PhysTable.setViewportView(table_PhysRecords);
                            }

                            GroupLayout panelPhysTableLayout = new GroupLayout(panelPhysTable);
                            panelPhysTable.setLayout(panelPhysTableLayout);
                            panelPhysTableLayout.setHorizontalGroup(
                                panelPhysTableLayout.createParallelGroup()
                                    .addGroup(panelPhysTableLayout.createParallelGroup()
                                        .addGroup(panelPhysTableLayout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(scroll_PhysTable, GroupLayout.PREFERRED_SIZE, 486, GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 0, Short.MAX_VALUE)))
                                    .addGap(0, 0, Short.MAX_VALUE)
                            );
                            panelPhysTableLayout.setVerticalGroup(
                                panelPhysTableLayout.createParallelGroup()
                                    .addGroup(panelPhysTableLayout.createParallelGroup()
                                        .addGroup(panelPhysTableLayout.createSequentialGroup()
                                            .addGap(0, 0, Short.MAX_VALUE)
                                            .addComponent(scroll_PhysTable, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 0, Short.MAX_VALUE)))
                                    .addGap(0, 212, Short.MAX_VALUE)
                            );
                        }

                        GroupLayout panelPhysManagedLayout = new GroupLayout(panelPhysManaged);
                        panelPhysManaged.setLayout(panelPhysManagedLayout);
                        panelPhysManagedLayout.setHorizontalGroup(
                            panelPhysManagedLayout.createParallelGroup()
                                .addGroup(panelPhysManagedLayout.createParallelGroup()
                                    .addGroup(panelPhysManagedLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(panelPhysManagedLayout.createParallelGroup()
                                            .addComponent(panelPhysTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(panelPhysNavigation, GroupLayout.PREFERRED_SIZE, 488, GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(0, 503, Short.MAX_VALUE)
                        );
                        panelPhysManagedLayout.setVerticalGroup(
                            panelPhysManagedLayout.createParallelGroup()
                                .addGroup(panelPhysManagedLayout.createParallelGroup()
                                    .addGroup(panelPhysManagedLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(panelPhysTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(6, 6, 6)
                                        .addComponent(panelPhysNavigation, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addGap(0, 266, Short.MAX_VALUE)
                        );
                    }

                    GroupLayout panelPhysDashboardLayout = new GroupLayout(panelPhysDashboard);
                    panelPhysDashboard.setLayout(panelPhysDashboardLayout);
                    panelPhysDashboardLayout.setHorizontalGroup(
                        panelPhysDashboardLayout.createParallelGroup()
                            .addGroup(panelPhysDashboardLayout.createParallelGroup()
                                .addGroup(panelPhysDashboardLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(panelPhysManaged, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panelPhysDashboardLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(panelPhysHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                    );
                    panelPhysDashboardLayout.setVerticalGroup(
                        panelPhysDashboardLayout.createParallelGroup()
                            .addGroup(panelPhysDashboardLayout.createParallelGroup()
                                .addGroup(panelPhysDashboardLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(panelPhysManaged, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(panelPhysDashboardLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(panelPhysHome, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                    );
                }

                //---- text_NotificationsPhys ----
                text_NotificationsPhys.setText("0 NEW NOTIFICATIONS");
                text_NotificationsPhys.setHorizontalAlignment(SwingConstants.RIGHT);
                text_NotificationsPhys.setForeground(new Color(255, 255, 153));
                text_NotificationsPhys.setFont(new Font("MS UI Gothic", Font.PLAIN, 12));

                //======== panelPhysNav ========
                {
                    panelPhysNav.setLayout(new GridLayout());

                    //---- btn_PhysRecords ----
                    btn_PhysRecords.setText("RECORDS");
                    panelPhysNav.add(btn_PhysRecords);

                    //---- btn_PhysHome ----
                    btn_PhysHome.setText("HOME");
                    panelPhysNav.add(btn_PhysHome);

                    //---- btn_LogOutPhys ----
                    btn_LogOutPhys.setText("LOG OUT");
                    panelPhysNav.add(btn_LogOutPhys);
                }

                GroupLayout panelPagePhysicianLayout = new GroupLayout(panelPagePhysician);
                panelPagePhysician.setLayout(panelPagePhysicianLayout);
                panelPagePhysicianLayout.setHorizontalGroup(
                    panelPagePhysicianLayout.createParallelGroup()
                        .addGroup(panelPagePhysicianLayout.createSequentialGroup()
                            .addGroup(panelPagePhysicianLayout.createParallelGroup()
                                .addComponent(panelPhysDashboard, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(GroupLayout.Alignment.TRAILING, panelPagePhysicianLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(text_DashWelcomePhysician, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_NotificationsPhys, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
                                .addComponent(panelPhysNav, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                );
                panelPagePhysicianLayout.setVerticalGroup(
                    panelPagePhysicianLayout.createParallelGroup()
                        .addGroup(panelPagePhysicianLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPagePhysicianLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(text_DashWelcomePhysician, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                .addComponent(text_NotificationsPhys))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelPhysDashboard, GroupLayout.PREFERRED_SIZE, 272, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(panelPhysNav, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
            }
            masterPanel.add(panelPagePhysician, "card8");

            //======== panelPageProvider ========
            {
                panelPageProvider.setFont(new Font("Segoe UI", Font.PLAIN, 22));

                //---- text_DashWelcomeProvider ----
                text_DashWelcomeProvider.setText("Welcome");
                text_DashWelcomeProvider.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
                text_DashWelcomeProvider.setEnabled(false);

                //======== panelProviderDashboard ========
                {
                    panelProviderDashboard.setBackground(UIManager.getColor("Button.shadowColor"));

                    GroupLayout panelProviderDashboardLayout = new GroupLayout(panelProviderDashboard);
                    panelProviderDashboard.setLayout(panelProviderDashboardLayout);
                    panelProviderDashboardLayout.setHorizontalGroup(
                        panelProviderDashboardLayout.createParallelGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                    );
                    panelProviderDashboardLayout.setVerticalGroup(
                        panelProviderDashboardLayout.createParallelGroup()
                            .addGap(0, 19, Short.MAX_VALUE)
                    );
                }

                //======== panelProviderNavigation ========
                {
                    panelProviderNavigation.setLayout(new GridLayout());
                }

                //---- text_NotificationsProvider ----
                text_NotificationsProvider.setText("0 NEW NOTIFICATIONS");
                text_NotificationsProvider.setHorizontalAlignment(SwingConstants.RIGHT);
                text_NotificationsProvider.setForeground(new Color(255, 255, 153));
                text_NotificationsProvider.setFont(new Font("MS UI Gothic", Font.PLAIN, 12));

                //======== panelProviderMain ========
                {

                    //---- btn_ProviderActive ----
                    btn_ProviderActive.setText("ACTIVE");

                    //---- btn_ProviderCompleted ----
                    btn_ProviderCompleted.setText("COMPLETED");

                    GroupLayout panelProviderMainLayout = new GroupLayout(panelProviderMain);
                    panelProviderMain.setLayout(panelProviderMainLayout);
                    panelProviderMainLayout.setHorizontalGroup(
                        panelProviderMainLayout.createParallelGroup()
                            .addGroup(panelProviderMainLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelProviderMainLayout.createParallelGroup()
                                    .addComponent(btn_ProviderCompleted, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_ProviderActive, GroupLayout.PREFERRED_SIZE, 129, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    );
                    panelProviderMainLayout.setVerticalGroup(
                        panelProviderMainLayout.createParallelGroup()
                            .addGroup(panelProviderMainLayout.createSequentialGroup()
                                .addGap(49, 49, 49)
                                .addComponent(btn_ProviderActive, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_ProviderCompleted, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE)
                                .addGap(56, 56, 56))
                    );
                }

                //---- btn_LogOutProvider ----
                btn_LogOutProvider.setText("LOG OUT");

                //======== panelProviderActive ========
                {

                    //======== scrollPane1 ========
                    {
                        scrollPane1.setViewportView(ProviderActiveReqTable);
                    }

                    GroupLayout panelProviderActiveLayout = new GroupLayout(panelProviderActive);
                    panelProviderActive.setLayout(panelProviderActiveLayout);
                    panelProviderActiveLayout.setHorizontalGroup(
                        panelProviderActiveLayout.createParallelGroup()
                            .addGroup(panelProviderActiveLayout.createParallelGroup()
                                .addGroup(panelProviderActiveLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                                    .addContainerGap()))
                            .addGap(0, 337, Short.MAX_VALUE)
                    );
                    panelProviderActiveLayout.setVerticalGroup(
                        panelProviderActiveLayout.createParallelGroup()
                            .addGroup(panelProviderActiveLayout.createParallelGroup()
                                .addGroup(panelProviderActiveLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                                    .addContainerGap()))
                            .addGap(0, 244, Short.MAX_VALUE)
                    );
                }

                GroupLayout panelPageProviderLayout = new GroupLayout(panelPageProvider);
                panelPageProvider.setLayout(panelPageProviderLayout);
                panelPageProviderLayout.setHorizontalGroup(
                    panelPageProviderLayout.createParallelGroup()
                        .addGroup(panelPageProviderLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panelPageProviderLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelProviderMain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(355, Short.MAX_VALUE)))
                        .addComponent(panelProviderDashboard, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.TRAILING, panelPageProviderLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageProviderLayout.createParallelGroup()
                                .addGroup(panelPageProviderLayout.createSequentialGroup()
                                    .addComponent(text_DashWelcomeProvider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(text_NotificationsProvider, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
                                .addGroup(panelPageProviderLayout.createSequentialGroup()
                                    .addComponent(panelProviderNavigation, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(panelProviderActive, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(GroupLayout.Alignment.TRAILING, panelPageProviderLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(btn_LogOutProvider, GroupLayout.PREFERRED_SIZE, 503, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap())
                );
                panelPageProviderLayout.setVerticalGroup(
                    panelPageProviderLayout.createParallelGroup()
                        .addGroup(panelPageProviderLayout.createParallelGroup()
                            .addGroup(GroupLayout.Alignment.TRAILING, panelPageProviderLayout.createSequentialGroup()
                                .addContainerGap(95, Short.MAX_VALUE)
                                .addComponent(panelProviderMain, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(95, Short.MAX_VALUE)))
                        .addGroup(panelPageProviderLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panelPageProviderLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(text_DashWelcomeProvider, GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
                                .addComponent(text_NotificationsProvider))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelProviderDashboard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(panelPageProviderLayout.createParallelGroup()
                                .addComponent(panelProviderNavigation, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
                                .addComponent(panelProviderActive, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(btn_LogOutProvider, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                );
            }
            masterPanel.add(panelPageProvider, "card9");
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Aloysius Arno Wiputra
    private JPanel masterPanel;
    private JPanel panelMain;
    private JPanel panelLoginFields;
    private JTextField field_Username;
    private JPasswordField field_Password;
    private JPanel panelWelcome;
    private JLabel text_Welcome;
    private JPanel panelLoginBottom;
    private JButton btn_Login;
    private JButton btn_NewCreate;
    private JPanel panelAccCreate;
    private JLabel text_FName;
    private JTextField field_FName;
    private JTextField field_LName;
    private JTextField field_CUsername;
    private JLabel text_CUsername;
    private JLabel text_LName;
    private JLabel text_CPass;
    private JPasswordField field_CPass;
    private JComboBox field_Scroll;
    private JLabel text_WhoAreYou;
    private JButton btn_CreateAcc;
    private JButton btn_BackToLogin;
    private JButton btn_ResetFields;
    private JTextField field_dobM;
    private JLabel text_CUsername2;
    private JTextField field_dobD;
    private JTextField field_dobY;
    private JPanel panelPageAdmin;
    private JLabel text_DashWelcome;
    private JPanel panelAdmDashboard;
    private JPanel panelAdminTaskTable;
    private JScrollPane scroll_AdminPendingTable;
    private JTable table_AdminPendingTable;
    private JButton btn_Read;
    private JButton btn_Approve;
    private JButton btn_RefreshAdminPendingTable;
    private JButton btn_Deny;
    private JPanel panelAdminHome;
    private JLabel label_descName2;
    private JLabel text_NumUsers;
    private JLabel label_descName3;
    private JLabel text_NumAccCreate;
    private JPanel panelAdmNavigation;
    private JButton btn_AdmTasks;
    private JButton btn_AdmHome;
    private JButton btn_LogOut;
    private JLabel text_Notifications;
    private JPanel panelPageUser;
    private JLabel text_DashWelcomeUser;
    private JPanel panelUserDashboard;
    private JPanel panelUserHome;
    private JLabel label_descName;
    private JLabel label_NameLASTFIRST;
    private JLabel label_descPhys;
    private JLabel label_PhysLASTFIRST;
    private JLabel label_desc_LU;
    private JLabel label_LU;
    private JPanel panelUserOrder;
    private JTextField field_UserOrder;
    private JLabel field_descUserOrder;
    private JLabel field_descOrderAddress;
    private JTextField field_UserOrderAddress;
    private JButton btn_UserOrder;
    private JTextField field_UserOrderCount;
    private JLabel label_descUserOrderCount;
    private JPanel panelUserOrderTable;
    private JScrollPane scroll_UserOrderHistory;
    private JTable table_UserOrderHistory;
    private JButton btn_ReadOrder;
    private JButton btn_RefreshUserOrder;
    private JButton btn_UserSwitchRecords;
    private JButton btn_UserSwitchOrder;
    private JPanel panelUserNavigation;
    private JButton btn_UserRecords;
    private JButton btn_UserNewOrder;
    private JButton btn_UserHome;
    private JButton btn_LogOutUser;
    private JLabel text_NotificationsUser;
    private JPanel panelPagePhysician;
    private JLabel text_DashWelcomePhysician;
    private JPanel panelPhysDashboard;
    private JPanel panelPhysHome;
    private JTextField field_PhysWhoClient;
    private JLabel label_descPhysWhoClient;
    private JLabel label_descPhysLU;
    private JTextField field_PhysLU;
    private JLabel label_descPhysPresc;
    private JTextField field_PhysPresc;
    private JButton btn_PhysUpdate;
    private JPanel panelPhysManaged;
    private JPanel panelPhysNavigation;
    private JButton btn_PhysSwitchRecords;
    private JButton btn_PhysSwitchLog;
    private JButton btn_ReadPhys;
    private JButton btn_RefreshPhys;
    private JPanel panelPhysTable;
    private JScrollPane scroll_PhysTable;
    private JTable table_PhysRecords;
    private JLabel text_NotificationsPhys;
    private JPanel panelPhysNav;
    private JButton btn_PhysRecords;
    private JButton btn_PhysHome;
    private JButton btn_LogOutPhys;
    private JPanel panelPageProvider;
    private JLabel text_DashWelcomeProvider;
    private JPanel panelProviderDashboard;
    private JPanel panelProviderNavigation;
    private JLabel text_NotificationsProvider;
    private JPanel panelProviderMain;
    private JButton btn_ProviderActive;
    private JButton btn_ProviderCompleted;
    private JButton btn_LogOutProvider;
    private JPanel panelProviderActive;
    private JScrollPane scrollPane1;
    private JTable ProviderActiveReqTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
