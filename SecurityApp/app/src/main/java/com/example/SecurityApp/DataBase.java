package com.example.SecurityApp;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class DataBase implements Serializable {
    // Database credentials
    final static String HOSTNAME = "sanb4019-sql-server.database.windows.net";

    final static String DBNAME = "security_project";

    final static String USERNAME = "sanb4019";

    final static String PASSWORD = "5VEXue6kJzUN7jZ";

    // Database connection string
    final static String URL = String.format(
            "jdbc:jtds:sqlserver://%s:1433/%s;user=%s;password=%s;encrypt=true;"
                    + "trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
            ,HOSTNAME, DBNAME, USERNAME, PASSWORD);


    ArrayList<User> users;
    ArrayList<Test> tests;
    ArrayList<Attempt> attempts;

    // Query Templates
    private final static String GET_USERS_QUERY = "SELECT name, id, scrabblePassword, patternPassword FROM USERS;";
    private final static String DELETE_USERS_QUERY = "DELETE FROM USERS;";
    private final static String DELETE_TESTS_QUERY = "DELETE FROM TEST;";
    private final static String DELETE_ATTEMPTS_QUERY = "DELETE FROM ATTEMPT;";
    private final static String INSERT_USER_QUERY = "INSERT INTO USERS VALUES (?, ?, ?, ?);";
    private final static String INSERT_TEST_QUERY = "INSERT INTO TEST VALUES (?, ?);";
    private final static String INSERT_ATTEMPT_QUERY = "INSERT INTO ATTEMPT VALUES (?, ?, ?, ?, ?, ?);";



    public DataBase() {
        users = getRemoteUsers();
        tests = new ArrayList<>();
        attempts = new ArrayList<>();
    }
    public int addTest(int uid){
        Test t = new Test(tests.size(),uid);
        tests.add(t);
        return t.getId();
    }
    public int newAttempt(int uid, double attemptTime, String lockType, boolean unlockSuccess, String unlockPattern){
        //If the last test is not complete, add new attempt to it, otherwise start new test
        System.out.println("ADDING NEW ATTEMPT");
        Attempt a = new Attempt(attempts.size(), attemptTime, lockType, unlockSuccess, unlockPattern);
        attempts.add(a);
        Test t;
        if(tests.size() == 0){
            t = new Test(tests.size(),uid);
            tests.add(t);
            t.addAttempt(a);
            System.out.print("ADDING NEW ATTEMPT ID: ");
            System.out.println(a.getId());
            setRemoteTestsAndAttempts();
            return t.getId();
        }
        t = tests.get((tests.size()-1));
        if(t.testComplete() == 1){
            t.addAttempt(a);
        }
        else{
            t = new Test(tests.size(),uid);
            tests.add(t);
            t.addAttempt(a);
        }
        setRemoteTestsAndAttempts();
        return t.getId();
    }
    //PASSWORD GETTERS AND SETTERS
    public void setScrabblePasswordById(int id,String password){
        users.get(id).setScrabblePassword(password);
        setRemoteUsers();
    }
    public String getScrabblePasswordById(int id){
        return users.get(id).getScrabblePassword();
    }
    public void setPatternPasswordById(int id,String password){
        users.get(id).setPatternPassword(password);
        setRemoteUsers();
    }
    public String getPatternPasswordById(int id){
        return users.get(id).getPatternPassword();
    }

    public ArrayList<User> getUsers() {
        return users;
    }
    public ArrayList<String> getUserNames(){
        ArrayList<String> user_names = new ArrayList<>();
        for(User user:users){
            user_names.add(user.getName());
        }
        return user_names;
    }
    public boolean addUser(String name){
        boolean nameExists = false;
        for(User u:users){if(u.getName().equals(name)) nameExists = true;}
        if(!nameExists) {
            users.add(new User(name, users.size(), "password", "123456"));
            setRemoteUsers();
            return true;
        }
        return false;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
        setRemoteUsers();
    }

    private void setRemoteAll(){

    }
    private ArrayList<User> getRemoteUsers(){

        ArrayList<User> users = new ArrayList<>();
        try
        {
            // Establish the connection.
            Connection connection = DriverManager.getConnection(URL);
            String sql = GET_USERS_QUERY;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while(resultSet.next())
            {
                users.add(new User(resultSet.getString(1),resultSet.getInt(2), resultSet.getString(3),resultSet.getString(4)));
            }
            System.out.println("Got Users");
            System.out.println(users);
            connection.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return users;
    }
    private void setRemoteUsers(){
        try {
            // Establish the connection.
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();

            // Delete all users for fresh start
            statement.executeUpdate(DELETE_USERS_QUERY);

            //Insert each user
            for (User user:users) {
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_QUERY);
                preparedStatement.setInt(1, user.getId());
                preparedStatement.setString(2, user.getName());
                preparedStatement.setString(3, user.getScrabblePassword());
                preparedStatement.setString(4, user.getPatternPassword());
                preparedStatement.executeUpdate();
            }
            System.out.println("Inserted Users.");
            connection.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setRemoteTestsAndAttempts(){
        try {
            // Establish the connection.
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();

            // Delete all tests and attempts for a fresh start
            statement.executeUpdate(DELETE_TESTS_QUERY);
            statement.executeUpdate(DELETE_ATTEMPTS_QUERY);

            //Insert each test
            for (Test test:tests) {
                //For each test, insert each Attempt
                for(Attempt attempt:test.getAttempts()){
                    PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ATTEMPT_QUERY);
                    preparedStatement.setInt(1, attempt.getId());
                    preparedStatement.setDouble(2, attempt.getAttemptTime());
                    preparedStatement.setString(3, attempt.getLockType());
                    preparedStatement.setString(4, attempt.getUnlockPattern());
                    preparedStatement.setBoolean(5, attempt.isUnlockSuccess());
                    preparedStatement.setInt(6, test.getId());
                    preparedStatement.executeUpdate();
                }
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TEST_QUERY);
                preparedStatement.setInt(1, test.getId());
                preparedStatement.setInt(2, test.getUid());

                preparedStatement.executeUpdate();
            }
            System.out.println("Inserted Tests and Attempts.");
            connection.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public User findUserByName(String name){
        System.out.println(String.format("Testing for %s",name));
        for(User u: users){

            System.out.println(u.getName());
            if(u.getName().equals(name)) return u;
        }
        final User o = null;
        return o;
    }
}
