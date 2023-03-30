/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package atminterface;

/**
 *
 * @author tanvi
 */

// Libraries
import java.sql.*;  
import java.util.*;
import databaseConnection.ConnectionProvider;

import java.util.logging.Level;
import java.util.logging.Logger;



public class AtmInterface {
    
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner scanner = new Scanner(System.in);
        AtmOps obj = new AtmOps();
        int choice;
        choice = 0;
        System.out.println("Welcome to Atm Operations! Please select a given operation!\n\n");
        while(choice!=6){
            System.out.println("------------MENU------------");
            System.out.println("0.Login");
            System.out.println("1.Withdraw");
            System.out.println("2.Deposit");
            System.out.println("3.Transfer");
            System.out.println("4.View Transaction History");
            System.out.println("5.Create new user");
            System.out.println("6.Quit");
            System.out.println("-----------------------------");
            System.out.println("Enter choice : ");
            choice = scanner.nextInt();
            switch(choice){
                case 0:
                    obj.userLogin();
                    break;
                case 1:
                    obj.withdraw();
                    break;
                case 2:
                    obj.deposit();
                    break;
                case 3:
                    obj.transfer();
                    break;
                case 4:
                    obj.viewTransacHistory();
                    break;
                case 5:
                    obj.createUser();
                    break;
                case 6:
                    obj.quit();
                    break;
                default :
                    System.out.println("Invalid Operation!");
                    break;
            }
        }
        scanner.close();
    }
    
}


class AtmOps{
    Connection con;
    Statement stmt;
    
    Scanner sc;
    
    boolean loggedIn;   
    String username;
    String userId;
    String pin;
    float currentBalance;
    
    public AtmOps(){
        try{
            loggedIn = false;
            con = ConnectionProvider.getConnection();
            stmt = con.createStatement();
            sc = new Scanner(System.in);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e);
        }
    }
    
    void createUser(){
        String uname;
        String upin;
        String uid;
        
        System.out.println("Enter username : ");
        uname = sc.nextLine();       
        do{
            System.out.println("Enter 4 digit pin");
            upin = sc.nextLine();
        }while(!validatePin(upin));                
        
        do{
            System.out.println("Enter userId(unique)");
            uid = sc.nextLine();
            //System.out.println(validateUid(uid)); // check1
        }while(!validateUid(uid)); 
        
        
            
        String query = "INSERT INTO user_details VALUES('"+uname+"','"+uid+"','"+upin+"',0);";
        try{
            stmt.executeUpdate(query);
            System.out.println("User Created!You may now login!");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Sorry! User could not be created!");
        }
    }
    
    
    
    void userLogin(){
        String upin;
        String uid;        
        
        do{
            System.out.println("Enter userId(unique)");
            uid = sc.nextLine();
        }while(validateUid(uid));
        
        do{
            System.out.println("Enter 4 digit pin");
            upin = sc.nextLine();
        }while(!validatePin(upin));                
         
        
        String query = "SELECT * FROM user_details WHERE userid='"+uid+"' AND pin='"+upin+"';";
        try{
            ResultSet rs = stmt.executeQuery(query);
            if(rs!=null){
                rs.next();
                loggedIn = true;
                userId = uid;
                pin = upin;
                username = rs.getString("username");                
                currentBalance = rs.getFloat("balance");
                System.out.println("Welcome " + username + "! You are now logged in-");
            rs.close();    
            }else{
                System.out.println("User with given credentials does not exist! Please create new User");
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Invalid Credentials! Could not login" + e);
        }
    }
    
    
    
    void withdraw(){
        if(!loggedIn){
            System.out.println("Please login first!");
            return;
        }
        
        float amount;
        System.out.println("Enter amount to withdraw : ");
        amount = Float.parseFloat(sc.nextLine());
        if(amount < 0 || amount > currentBalance)
        {
            System.out.println("Error! Invalid amount");
            return;
        }
        float newAmount = currentBalance - amount;
        String query = "UPDATE user_details SET balance = "+Float.toString(newAmount)+" WHERE userid = '"+this.userId+"';";
        String queryTemp = "INSERT INTO trans_hist(type,userid,amount,balance) VALUES('withdraw','"+userId+"',"+Float.toString(amount)+","+Float.toString(newAmount)+");";
        try{
            stmt.executeUpdate(query);
            stmt.executeUpdate(queryTemp);
            currentBalance = newAmount;
            System.out.println("Amount withdrawn!");
        }catch(Exception e){
            System.out.println("Couldn't withdraw Amount!" + e);
            e.printStackTrace();
        }
    }
    
    void deposit(){
        if(!loggedIn){
            System.out.println("Please login first!");
            return;
        }

        float amount;
        System.out.println("Enter amount to deposit : ");
        amount = Float.parseFloat(sc.nextLine());
        if(amount < 0)
        {
            System.out.println("Error! Invalid amount");
            return;
        }
        float newAmount = currentBalance + amount;
        String query = "UPDATE user_details SET balance = "+Float.toString(newAmount)+" WHERE userid = '"+this.userId+"';";
        String queryTemp = "INSERT INTO trans_hist(type,userid,amount,balance) VALUES('deposit','"+userId+"',"+Float.toString(amount)+","+Float.toString(newAmount)+");";
        try{
            stmt.executeUpdate(query);
            stmt.executeUpdate(queryTemp);
            currentBalance = newAmount;
            System.out.println("Amount deposited!");
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Couldn't deposit Amount!" + e);
        }
    }
    
    
    void transfer(){
        
        if(!loggedIn){
            System.out.println("Please login first!");
            return;
        }

        float amount;
        String uidTransTo;
        System.out.println("Enter amount to deposit : ");
        amount = Float.parseFloat(sc.nextLine());
        if(amount < 0 || amount > currentBalance)
        {
            System.out.println("Error! Invalid amount");
            return;
        }
        
        System.out.println("Enter userID of account to tranfer to : ");
        uidTransTo = sc.nextLine();
        String query = "SELECT * FROM user_details WHERE userid = '"+uidTransTo+"';";
        try{
            ResultSet rs = stmt.executeQuery(query);
            if(rs == null){
                System.out.println("Error! User doesn't exist");
                return;
            }
             rs.next();
             float newAmount = rs.getFloat("balance") + amount;
             currentBalance = currentBalance - amount;
             String query1 = "UPDATE user_details SET balance = "+Float.toString(newAmount)+" WHERE userid = '"+uidTransTo+"';";
             String query2 = "UPDATE user_details SET balance = "+Float.toString(currentBalance)+" WHERE userid = '"+userId+"';";
             String queryTemp1 = "INSERT INTO trans_hist(type,userid,amount,balance) VALUES('transfer','"+userId+"',"+Float.toString(amount)+","+Float.toString(currentBalance)+");";
             String queryTemp2 = "INSERT INTO trans_hist(type,userid,amount,balance) VALUES('received','"+uidTransTo+"',"+Float.toString(amount)+","+Float.toString(newAmount)+");";
            try{
                stmt.executeUpdate(query1);
                stmt.executeUpdate(query2);
                stmt.executeUpdate(queryTemp1);
                stmt.executeUpdate(queryTemp2);
                System.out.println("Amount Transfered!");
            }catch(Exception e){
                e.printStackTrace();
                System.out.println("Couldn't deposit Amount!" + e);
            }
            rs.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error! Could not retreive user's details" +e);
        }
              
    }
    
    
    void viewTransacHistory(){
        if(!loggedIn){
            System.out.println("Please login first!");
            return;
        }
        
        String type;
        float amount;
        float currBalance;
        
        String query = "SELECT * FROM trans_hist WHERE userid = '"+userId+"';";
        try{
            ResultSet rs = stmt.executeQuery(query);
            if(rs == null)
            {
                System.out.println("No Transaction made!");
                return;
            }
            System.out.println("Type\tAmount\tBalance\tDateTime");
            System.out.println("---------------------------------------------------");
            while(rs.next())
            {
                type = rs.getString("type");
                amount = rs.getFloat("amount");
                currBalance = rs.getFloat("balance");
                Timestamp date = rs.getTimestamp("date_time");
                java.sql.Date d = new java.sql.Date(date.getTime());  
                System.out.println(""+type+"\t"+amount+"\t"+currBalance+"\t"+d+"");
            }
            System.out.println("---------------------------------------------------");
            rs.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error! Could not retreive user's transaction history" + e);
        }
    }
    
    
    void quit(){
        loggedIn = false;
        username = null;
        userId = null;
        pin = null;
        currentBalance = 0.0f;
        System.out.println("You have successfully logged out! Thankyou!");
    }
    
    
    boolean validatePin(String pin){
        if(pin.length() != 4)
            return false;
        try{
            int i = Integer.parseInt(pin);
        }catch(NumberFormatException e){
            e.printStackTrace();
            System.out.println("Incorrect Pin Entered! Enter again pls : " + e);
            return false;
        }
        
        return true;
    }
    
    boolean validateUid(String uid){
        String query = "SELECT * FROM user_details WHERE userid = '"+uid+"';";
        try{
        ResultSet rs = stmt.executeQuery(query);
        if(rs.next())
        {
            rs.close();
            return false;
        }else{
            return true;
        }
        }catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            return false;
        }
        
    }
    
    
}

/* 
create table user_details(
username varchar(255) not null,
userid varchar(255),
pin varchar(4) not null,
balance float not null,
primary key(userid)
);

create table trans_hist(
type varchar(255) not null,
userid varchar(255) not null,
amount float not null,
balance float not null,
date_time timestamp default current_timestamp not null
);

insert into user_details values("user1","1","1111",0);
insert into user_details values("user2","2","2222",0);
insert into user_details values("user3","3","3333",0);
insert into user_details values("user4","4","4444",0);
insert into user_details values("user5","5","5555",0);


*/