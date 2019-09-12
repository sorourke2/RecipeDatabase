import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Scanner;  // Import the Scanner class

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Project {

  private static String userName = "root";
  private static String password;
  private final String serverName = "localhost";
  private final int portNumber = 3306;
  private final String dbName = "recipedb";

  ResultSet recipeResults;
  ArrayList<Integer> recipeIDs = new ArrayList<Integer>();
  static Integer latestViewedRecipeID;

  // main method, Gets the username and password for the database connection
  public static void main(String[] args) {
    Scanner myObj = new Scanner(System.in); 
        System.out.println("Input the username:");
        Project.userName = myObj.nextLine();

        System.out.println("Input the password:");
        Project.password = myObj.nextLine();
    Project app = new Project();
    app.run();

    // end program
    System.out.println("\n--Ending application program--\n");
    return;
  }

  // Runs the database
  public void run() {
    latestViewedRecipeID = 0;
    Scanner myObj = new Scanner(System.in); 
    // Connect to MySQL
    Connection conn = null;
    try {
      conn = this.getConnection();
      System.out.println("Connected to database");

    } catch (SQLException e) {
      System.out.println(e.getErrorCode( )); 
    }

    String userFirstChoice = "";

    while (userFirstChoice != "Nothing") {
      System.out.println("-----------------------------------------------------");
      System.out.println("What would the user like to do?");
      System.out.println("-----------------------------------------------------");
      System.out.println("Please type: \n 'Create Recipe' to create a recipe \n 'Search Recipe'"
          + " to search for recipe \n 'Modify Data'"
          + " to modify data \n 'Nothing' to end the program");
      System.out.println("-----------------------------------------------------");

      userFirstChoice = myObj.nextLine();

      if (userFirstChoice.equalsIgnoreCase("Create Recipe")) {
        createRecipe(conn);
      }

      else if (userFirstChoice.equalsIgnoreCase("Search Recipe")) {
        searchRecipe(conn);
      }
      else if (userFirstChoice.equalsIgnoreCase("Modify Data")) {
        try {
          changeRecipe(conn);
        }
        catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      else if (userFirstChoice.equalsIgnoreCase("Nothing")) {
        try { 
          System.out.println("\n-- Closing Database Connection --\n");
          conn.close(); } catch (Exception e) { /* ignored */ }
        return;
      }

      else {
        System.out.println("\nInput not correct. Try Again.\n");
      }
    }
    return;
  }

  private void changeRecipe(Connection conn) throws SQLException {
    Scanner myObj = new Scanner(System.in); 
    String userResponse = "";
    CallableStatement pstmt;
    System.out.println("-----------------------------------------------------");
    System.out.println("How would you like to determine the recipe to augment?");
    System.out.println("1. Redo a search of recipes\n2. Input a recipe ID\n3. Type in a recipe name");
    System.out.println("-----------------------------------------------------");
    String userSearchChoice = myObj.nextLine();
    int recipeID = -1;
    if (userSearchChoice.equalsIgnoreCase("1")) {
      recipeID = searchRecipe(conn);

    }
    else if (userSearchChoice.equalsIgnoreCase("2")) {
      System.out.println("-----------------------------------------------------");
      System.out.println("Input a recipe ID:");
      System.out.println("-----------------------------------------------------");
      userResponse = myObj.nextLine();
      recipeID = Integer.parseInt(userResponse);
    }
    else if (userSearchChoice.equalsIgnoreCase("3")) {
      System.out.println("-----------------------------------------------------");
      System.out.println("Input a recipe Name:");
      System.out.println("-----------------------------------------------------");
      userResponse = myObj.nextLine();
      pstmt = conn.prepareCall("select recipe_id from recipe where (?) = name");
      pstmt.setString(1, userResponse);
      ResultSet r = pstmt.executeQuery();
      r.next();
      recipeID = r.getInt(1);
    }
    else {
      return;
    }

    CallableStatement cs = conn.prepareCall("call does_recipe_exist(?)");
    cs.setInt(1, recipeID);
    ResultSet r = cs.executeQuery();
    r.next();
    if (r.getInt(1) == 0) {
      System.out.println("No valid recipe ID found. Try again.");
      changeRecipe(conn);
    }
    else {
      System.out.println("Would you like to\n1. Delete the recipe?\n2. Augment the recipe?");
      String choice = myObj.nextLine();
      if (choice.equalsIgnoreCase("2")) {
        changeGivenRecipe(conn, recipeID);
      }
      else {
        cs = conn.prepareCall("delete from recipe where recipe_id = "+recipeID);
        cs.executeUpdate();
        System.out.println("Recipe deleted.");
      }
    }
  }

  private void changeGivenRecipe(Connection conn, int recipeID) throws SQLException {
    Scanner myObj = new Scanner(System.in); 

    System.out.println("What would you like to change about the recipe? Respond with the number associated with the given search options.");
    System.out.println("1. Cooked Before\n2. Cuisine\n3. Meal Type\n4. Difficulty\n"
        + "5. Minutes To Make\n6. Author\n7. Preparation Type\n"
        + "8. Ingredient\n9. Dietary Type\n10. Stop the change");

    Scanner myObj1 = new Scanner(System.in);
    String column = "";
    String input = "";
    String userSearchByChoice = "";
    Boolean quickUpdate = false;


    userSearchByChoice = myObj.nextLine();
    // Search by cookedBefore
    if (userSearchByChoice.equals("1")) {
      column = "cooked_before";
      System.out.println("What is your new Cooked Before value? '0' for false, '1' for true");
      input = myObj.nextLine();
      quickUpdate = true;

    }
    // Search by Cuisine
    else if (userSearchByChoice.equals("2")) {
      column = "cuisine";
      System.out.println("What is the recipe's new cuisine?");
      input = myObj.nextLine();
      CallableStatement s = conn.prepareCall("call create_cuisine(?)");
      s.setString(1, input);
      s.executeUpdate(); 
      quickUpdate = true;
    }
    // Search by Meal Type
    else if (userSearchByChoice.equals("3")) {
      column = "meal_type";
      System.out.println("What new meal type is the recipe?");
      input = myObj.nextLine();
      quickUpdate = true;

    }
    // Search by Difficulty
    else if (userSearchByChoice.equals("4")) {
      column = "difficulty";
      System.out.println("What is the new difficulty of the recipe?");
      input = myObj.nextLine();
      quickUpdate = true;

    }
    // Search by Minutes To Make
    else if (userSearchByChoice.equals("5")) {
      column = "minutes_to_make";
      System.out.println("Approximately how many minutes does the recipe now take to create?");
      input = myObj.nextLine();
      quickUpdate = true;
    }
    // Search by Author
    else if (userSearchByChoice.equals("6")) {
      column = "author";
      System.out.println("Author of recipe?");
      String userResponse = myObj.nextLine();
      String author = userResponse;
      CallableStatement p = conn.prepareCall("call doesAuthorNotExist(?)");
      p.setString(1, author);
      ResultSet r = p.executeQuery();
      r.next();
      // if author does not exist
      if (r.getInt(1) == 1) {
        p = conn.prepareCall("insert into author (name) values (?)");
        p.setString(1, author);
        p.executeUpdate();
        p = conn.prepareCall("SELECT LAST_INSERT_ID()");
        r = p.executeQuery();
        r.next();
        int authorID = r.getInt(1);
        p = conn.prepareCall("update recipe set author = (?) where recipe_id = (?)");
        p.setInt(1, authorID);
        p.setInt(2, recipeID);
        p.executeUpdate();

        // What cuisines is the author associated with?
        userResponse = "";;
        while(!userResponse.equalsIgnoreCase("stop")) {
          System.out.println("List a cuisine that the author specializes in.  Press enter after each cuisine. Type 'Stop' to stop adding cuisine.");
          userResponse = myObj.nextLine();
          String specialty = userResponse;
          p = conn.prepareCall("call update_author_with_cuisine(?,?)");
          p.setInt(1, authorID);
          p.setString(2, specialty);
          p.executeQuery();
        }
      }
      else {
        CallableStatement p1 = conn.prepareCall("call update_recipe_with_author(?,?)");
        p1.setString(1, author);
        p1.setInt(2, recipeID);
        p1.executeQuery();
      }
    }
    // Search by Preparation Type
    else if (userSearchByChoice.equals("7")) {
      column = "preparation_type";
      CallableStatement s = conn.prepareCall("delete from prepares where recipe = "+recipeID);
      s.executeUpdate();
      System.out.println("What is the new preparation method that the recipe utilizes?");
      input = myObj.nextLine();

      s = conn.prepareCall("call update_recipe_with_preparation(?,?)");
      s.setString(1, input);
      s.setInt(2, recipeID);
      s.executeUpdate();
    }
    // Search by Ingredient
    else if (userSearchByChoice.equals("8")) {
      column = "ingredient";
      CallableStatement s = conn.prepareCall("delete from made_up_of where recipe = "+recipeID);
      s.executeUpdate();
      while(!input.equalsIgnoreCase("stop")) {
        System.out.println("List the new ingredient in the recipe. Press enter after each "
            + "ingredient. Type 'Stop' to stop adding ingredients.");
        input = myObj.nextLine(); 
        if (!input.equalsIgnoreCase("stop")) {
          s = conn.prepareCall("call update_recipe_with_ingredient(?,?)");
          s.setString(1, input);
          s.setInt(2, recipeID);
          s.executeUpdate();
        }
      }
    }
    // Search by Dietary Type
    else if (userSearchByChoice.equals("9")) {
      column = "dietary_type";
      while(!input.equalsIgnoreCase("stop")) {
        System.out.println("List the new dietary restrictions.  Press enter after each restriction. "
            + "Type 'Stop' to stop adding dietary restrictions.");
        input = myObj.nextLine(); 
        CallableStatement s = conn.prepareCall("delete from has_dietary_type where recipe = "+recipeID);
        s.executeUpdate();
        if (!input.equalsIgnoreCase("stop")) {
          s = conn.prepareCall("call update_recipe_with_diet(?,?)");
          s.setString(1, input);
          s.setInt(2, recipeID);
          s.executeUpdate();
        }
      }
    }
    else if (userSearchByChoice.equalsIgnoreCase("10")) {
      return ;
    }
    else {
      System.out.println("No correct input found, try again.\n");
      searchRecipe(conn);
    } 

    if (quickUpdate) {
      CallableStatement s = conn.prepareCall("update recipe set "+column+" = '"+input+"' where recipe_id = "+recipeID);
      s.executeUpdate();
      System.out.println("Recipe Updated: ");
      viewRecipe(conn, recipeID);
      System.out.println("\nPress Enter to continue:");
      String breaker = myObj.nextLine(); 
    }




  }

  private void createRecipe(Connection conn) {
    Scanner myObj = new Scanner(System.in); 
    String userResponse ="";
    System.out.println("What is the name of the recipe you want to create?");
    userResponse = myObj.nextLine();
    String name = userResponse;
    System.out.println("Cuisine?");
    userResponse = myObj.nextLine();
    String cuisine = userResponse;
    System.out.println("Meal Type? -- breakfast, lunch, dinner -- ");
    userResponse = myObj.nextLine();
    String meal_type = userResponse;
    System.out.println("Difficulty? -- beginner, intermediate, expert -- ");
    userResponse = myObj.nextLine();
    String difficulty = userResponse;
    System.out.println("Minutes to make the recipe?");
    userResponse = myObj.nextLine();
    Integer minutes = Integer.parseInt(userResponse);
    System.out.println("Serving yield of recipe? (int)");
    userResponse = myObj.nextLine();
    Integer yield = Integer.parseInt(userResponse);
    userResponse = "";
    System.out.println("Have you cooked it before? '0' for false, '1' for true");
    userResponse = myObj.nextLine();
    Integer cookedBefore = Integer.parseInt(userResponse);
    userResponse = "";


    CallableStatement pstmt;
    try {
      pstmt = conn.prepareCall("call createRecipe(?,?,?,?,?,?,?)");
      pstmt.setString(1, name);
      pstmt.setString(2, cuisine);
      pstmt.setString(3, meal_type);
      pstmt.setString(4, difficulty);
      pstmt.setInt(5, minutes);
      pstmt.setInt(6, yield);
      pstmt.setInt(7, cookedBefore);
      pstmt.executeQuery();
      CallableStatement p = conn.prepareCall("SELECT LAST_INSERT_ID()");
      ResultSet r = p.executeQuery();
      r.next();
      int recipeID = r.getInt(1);

      while(!userResponse.equalsIgnoreCase("stop")) {
        System.out.println("List an ingredient in the recipe. Press enter after each ingredient. Type 'Stop' to stop adding ingredients.");
        userResponse = myObj.nextLine();
        if (!userResponse.equalsIgnoreCase("stop")) {
          p = conn.prepareCall("call update_recipe_with_ingredient(?,?)");
          p.setString(1, userResponse);
          p.setInt(2, recipeID);
          p.executeQuery();
        }
      }
      userResponse = "";

      System.out.println("What is the preparation method that the recipe utilizes?");
      userResponse = myObj.nextLine();
      String preparation = userResponse;
      p = conn.prepareCall("call update_recipe_with_preparation(?,?)");
      p.setString(1, preparation);
      p.setInt(2, recipeID);
      p.executeQuery();

      while(!userResponse.equalsIgnoreCase("stop")) {
        System.out.println("List the dietary restrictions.  Press enter after each restriction. Type 'Stop' to stop adding dietary restrictions.");
        userResponse = myObj.nextLine();

        p = conn.prepareCall("call update_recipe_with_diet(?,?)");
        p.setString(1, userResponse);
        p.setInt(2, recipeID);
        p.executeQuery();
      }

      System.out.println("Author of recipe?");
      userResponse = myObj.nextLine();
      String author = userResponse;


      p = conn.prepareCall("call doesAuthorNotExist(?)");
      p.setString(1, author);
      r = p.executeQuery();
      r.next();
      // if author does not exist
      if (r.getInt(1) == 1) {
        p = conn.prepareCall("insert into author (name) values (?)");
        p.setString(1, author);
        p.executeUpdate();
        p = conn.prepareCall("SELECT LAST_INSERT_ID()");
        r = p.executeQuery();
        r.next();
        int authorID = r.getInt(1);
        p = conn.prepareCall("update recipe set author = (?) where recipe_id = (?)");
        p.setInt(1, authorID);
        p.setInt(2, recipeID);
        p.executeUpdate();

        // What cuisines is the author associated with?
        userResponse = "";;
        while(!userResponse.equalsIgnoreCase("stop")) {
          System.out.println("List a cuisine that the author specializes in.  Press enter after each cuisine. Type 'Stop' to stop adding cuisine.");
          userResponse = myObj.nextLine();
          String specialty = userResponse;
          p = conn.prepareCall("call update_author_with_cuisine(?,?)");
          p.setInt(1, authorID);
          p.setString(2, specialty);
          p.executeQuery();
        }
      }
      else {
        CallableStatement p1 = conn.prepareCall("call update_recipe_with_author(?,?)");
        p1.setString(1, author);
        p1.setInt(2, recipeID);
        p1.executeQuery();
      }

    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


  }

  public int searchRecipe(Connection conn) {
    Scanner myObj = new Scanner(System.in); 

    System.out.println("How would you like to search for a recipe? Respond with the number associated with the given search options.");
    System.out.println("1. Cooked Before\n2. Cuisine\n3. Meal Type\n4. Difficulty\n"
        + "5. Minutes To Make\n6. Author\n7. Preparation Type\n"
        + "8. Ingredient\n9. Dietary Type\n10. Stop the search");

    Scanner myObj1 = new Scanner(System.in);
    String userSearchByChoice = "";
    String query = "";
    String input = "";


    userSearchByChoice = myObj.nextLine();

    CallableStatement pstmt;
    CallableStatement pstmt1;
    CallableStatement pstmt2;
    int viewedRecipe = -1;
    ResultSet rs;
    ResultSet rs1;
    ResultSet rs2;
    Integer result;
    ArrayList<Integer> resultArr = new ArrayList<Integer>();
    ArrayList<Integer> test = new ArrayList<Integer>();



    // Search by cookedBefore
    if (userSearchByChoice.equals("1")) {
      query = "{call recipeByCookedBefore(?)}";
      System.out.println("Have you cooked the recipe before? '0' for false, '1' for true");
      input = myObj.nextLine();

    }
    // Search by Cuisine
    else if (userSearchByChoice.equals("2")) {
      query = "{call recipeByCuisine(?)}";
      System.out.println("What cuisine type does the recipe fall under?");
      input = myObj.nextLine();
    }
    // Search by Meal Type
    else if (userSearchByChoice.equals("3")) {
      query = "{call recipeByMealType(?)}";
      System.out.println("What meal type is the recipe?");
      input = myObj.nextLine();

    }
    // Search by Difficulty
    else if (userSearchByChoice.equals("4")) {
      query = "{call recipeByDifficulty(?)}";
      System.out.println("What is the difficulty of the recipe?");
      input = myObj.nextLine();

    }
    // Search by Minutes To Make
    else if (userSearchByChoice.equals("5")) {
      query = "{call recipeByMinutesToMake(?)}";
      System.out.println("Approximately how many minutes does the recipe take to create?");
      input = myObj.nextLine();
    }
    // Search by Author
    else if (userSearchByChoice.equals("6")) {
      query = "{call recipeByAuthor(?)}";
      System.out.println("What is the name of the author?");
      input = myObj.nextLine();      
    }
    // Search by Preparation Type
    else if (userSearchByChoice.equals("7")) {
      query = "{call recipeByPreparationType(?)}";
      System.out.println("What is the preparation type of the recipe?");
      input = myObj.nextLine();  
    }
    // Search by Ingredient
    else if (userSearchByChoice.equals("8")) {
      query = "{call recipeByIngredientName(?)}";
      System.out.println("What is the ingredient used in the recipe?");
      input = myObj.nextLine();  
    }
    // Search by Dietary Type
    else if (userSearchByChoice.equals("9")) {
      query = "{call recipeByDietaryType(?)}";
      System.out.println("What is the dietary type of the recipe?");
      input = myObj.nextLine();  
    }
    else if (userSearchByChoice.equalsIgnoreCase("10")) {
      return viewedRecipe;
    }
    else {
      System.out.println("No correct input found, try again.\n");
      searchRecipe(conn);
    }

    try {
      pstmt = conn.prepareCall(query);
      pstmt.setString(1, input);
      rs = pstmt.executeQuery();

      // Move through the result set, printing out results

      if (!rs.next()) {
        System.out.println("No recipes with those specifications exist.");
      }
      else {
        rs.previous();
        while (rs.next()) {
          result = rs.getInt("recipe_id");
          resultArr.add(result);
          for(int i = 0; i <resultArr.size(); i++) {
            test.add(resultArr.get(0));
          }

          while (resultArr.size()!= 0) {
            pstmt2 = conn.prepareCall("SELECT name, recipe_id FROM recipe WHERE recipe_id = "+resultArr.remove(0));
            rs2 = pstmt2.executeQuery();
            System.out.println("--------------------------------------------");
            while(rs2.next()) {
              String recipeName = rs2.getString("name");
              Integer recipeID = rs2.getInt("recipe_id");
              System.out.println("ID: "+recipeID+", Name: "+recipeName+"\n");
            }
          }
        }
        System.out.println("Would you like to...");
        System.out.println("1. View a recipe\n2. Narrow down your search further?");
        String input2 = myObj.nextLine();
        if (input2.equalsIgnoreCase("1")) {
          System.out.println("Type the ID of the recipe you would like to view:");
          input2 = myObj.nextLine();
          System.out.println("-----------------------------------------------------");
          viewedRecipe = viewRecipe(conn,Integer.parseInt(input2));
          System.out.println("-----------------------------------------------------");
          System.out.println("Press a key to continue...");
          String break1 = myObj.nextLine();  
        }
        else {
          viewedRecipe = narrowSearch(conn, test);
        }
      }
    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return viewedRecipe;
  }





  private int narrowSearch(Connection conn, ArrayList<Integer> r) {
    System.out.println("How would you like to search for a recipe? Respond with the number associated with the given search options.");
    System.out.println("1. Cooked Before\n2. Cuisine\n3. Meal Type\n4. Difficulty\n"
        + "5. Minutes To Make\n6. Author\n7. Preparation Type\n"
        + "8. Ingredient\n9. Dietary Type\n10. Stop the search");
    Scanner myObj = new Scanner(System.in);
    String userSearchByChoice = "";
    String query = "";
    String input = "";


    userSearchByChoice = myObj.nextLine();
    ArrayList<Integer> test = new ArrayList<Integer>();
    CallableStatement pstmt;
    CallableStatement pstmt1;
    CallableStatement pstmt2;
    int viewedRecipe = -1;
    ResultSet rs;
    ResultSet rs1;
    ResultSet rs2;
    Integer result;
    ArrayList<Integer> resultArr = new ArrayList<Integer>();

    // Search by cookedBefore
    if (userSearchByChoice.equals("1")) {
      query = "{call recipeByCookedBefore(?)}";
      System.out.println("Have you cooked the recipe before? '0' for false, '1' for true");
      input = myObj.nextLine();

    }
    // Search by Cuisine
    else if (userSearchByChoice.equals("2")) {
      query = "{call recipeByCuisine(?)}";
      System.out.println("What cuisine type does the recipe fall under?");
      input = myObj.nextLine();
    }
    // Search by Meal Type
    else if (userSearchByChoice.equals("3")) {
      query = "{call recipeByMealType(?)}";
      System.out.println("What meal type is the recipe?");
      input = myObj.nextLine();

    }
    // Search by Difficulty
    else if (userSearchByChoice.equals("4")) {
      query = "{call recipeByDifficulty(?)}";
      System.out.println("What is the difficulty of the recipe?");
      input = myObj.nextLine();

    }
    // Search by Minutes To Make
    else if (userSearchByChoice.equals("5")) {
      query = "{call recipeByMinutesToMake(?)}";
      System.out.println("Approximately how many minutes does the recipe take to create?");
      input = myObj.nextLine();
    }
    // Search by Author
    else if (userSearchByChoice.equals("6")) {
      query = "{call recipeByAuthor(?)}";
      System.out.println("What is the name of the author?");
      input = myObj.nextLine();      
    }
    // Search by Preparation Type
    else if (userSearchByChoice.equals("7")) {
      query = "{call recipeByPreparationType(?)}";
      System.out.println("What is the preperation type of the recipe?");
      input = myObj.nextLine();  
    }
    // Search by Ingredient
    else if (userSearchByChoice.equals("8")) {
      query = "{call recipeByIngredientName(?)}";
      System.out.println("What is the ingredient used in the recipe?");
      input = myObj.nextLine();  
    }
    // Search by Dietary Type
    else if (userSearchByChoice.equals("9")) {
      query = "{call recipeByDietaryType(?)}";
      System.out.println("What is the dietary type of the recipe?");
      input = myObj.nextLine();  
    }
    else if (userSearchByChoice.equalsIgnoreCase("10")) {
      return viewedRecipe;
    }
    else {
      System.out.println("No correct input found, try again.\n");
      searchRecipe(conn);
    }

    
    try {
      pstmt = conn.prepareCall(query);
      pstmt.setString(1, input);
      rs = pstmt.executeQuery();

      // Move through the result set, printing out results
      while (rs.next()) {
        result = rs.getInt("recipe_id");
        resultArr.add(result);
      }
      resultArr.retainAll(r);
      for(int i = 0; i < resultArr.size(); i++) {
        test.add(resultArr.get(i));
      }
      if (resultArr.size() == 0) {
        System.out.println("No recipe with given specifications.");
        return viewedRecipe;
      }
      while (resultArr.size() != 0) {
        pstmt2 = conn.prepareCall("SELECT name, recipe_id FROM recipe WHERE recipe_id = "+resultArr.remove(0));
        rs2 = pstmt2.executeQuery();

        System.out.println("--------------------------------------------");
        if (!rs2.next()) {
          System.out.println("No such recipes meet given specifications.");
        }
        rs2.previous();
        while(rs2.next()) {
          String recipeName = rs2.getString("name");
          Integer recipeID = rs2.getInt("recipe_id");
          System.out.println("ID: "+recipeID+", Name: "+recipeName+"\n");
        }
      }
      System.out.println("Would you like to...");
      System.out.println("1. View a recipe\n2. Narrow down your search further?");
      String input2 = myObj.nextLine();
      if (input2.equalsIgnoreCase("1")) {
        System.out.println("Type the ID of the recipe you would like to view:");
        input2 = myObj.nextLine();
        viewedRecipe = viewRecipe(conn,Integer.parseInt(input2));
        r = new ArrayList<Integer>();
      }
      else {
        viewedRecipe = narrowSearch(conn, test);
      }
    }

    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return viewedRecipe;
  }

  /**
   * Get a new database connection
   * 
   * @return
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", Project.userName);
    connectionProps.put("password", Project.password);

    conn = DriverManager.getConnection("jdbc:mysql://"
        + this.serverName + ":" + this.portNumber + "/" + this.dbName,
        connectionProps);

    return conn;
  }

  // Displays all unique characters in the table lotr_first_encounter
  public static int viewRecipe(Connection conn, int id)
      throws SQLException {

    CallableStatement pstmt = conn.prepareCall("call fullRecipe(?)");
    pstmt.setInt(1, id);
    ResultSet rs = pstmt.executeQuery();

    if (!rs.next()) {
      System.out.println("No such recipe with that ID exists.");
    }
    rs.previous();
    while(rs.next()) {

      Integer recipeID = rs.getInt("recipe_id");
      String name = rs.getString("name");
      String cuisine = rs.getString("cuisine");
      String meal_type = rs.getString("meal_type");
      String difficulty = rs.getString("difficulty");
      Integer minutes = rs.getInt("minutes_to_make");
      Integer yield = rs.getInt("num_person_yield");
      Integer cookedBefore = rs.getInt("cooked_before");
      String author = rs.getString("author_name1");
      String preparation_type = rs.getString("prep");
      String ingredients = rs.getString("ing1");
      String dietary = rs.getString("diet_type");
      System.out.println(" Recipe ID: "+ recipeID +",\n Name: "+ name +",\n Cuisine: "+ cuisine +",\n Meal Type: "+ meal_type +",\n Difficulty: "+ difficulty +",\n Minutes To Make: "+ minutes +",\n Dietary Indicators: "+dietary
          + "\n Yield: "+ yield +",\n Cooked Before: "+ cookedBefore +",\n Author: "+author+",\n Preparation Type: "+ preparation_type+ "\n Ingredients: " + ingredients);
      latestViewedRecipeID = recipeID;
      return recipeID;
    }
    return -1;
  }
}