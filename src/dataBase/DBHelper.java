package dataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
/**
 * This class propose is to get information from server Database.
 * 
 * @author Netanel Albert
 */
public class DBHelper {
	public static final String jdbcUrl = "jdbc:mysql://db-mysql-ams3-67328-do-user-4468260-0.db.ondigitalocean.com:25060/oop?useUnicode=yes&characterEncoding=UTF-8&useSSL=false";
	public static final String jdbcUser = "student";
	public static final String jdbcUserPassword = "OOP2020student";

	/**
	 * 
	 * @param userId - id for the data filter
	 * @return - MyGamesData object with all the data about the user (from server)
	 */
	public static MyGamesData userInfo(int userId) {
		MyGamesData data = new MyGamesData();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
			Statement statement = connection.createStatement();
			String allCustomersQuery = "SELECT * FROM Logs 	WHERE UserID = " + userId;
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);

			while (resultSet.next()) {
				LogDao log = new LogDao(resultSet.getInt("UserID"), resultSet.getInt("levelID"), resultSet.getInt("moves"),
						resultSet.getDate("time"), resultSet.getInt("score"));
				
				if(DBHelper.maxMovesAllaowd(log.getLevelId()) == -1)
					continue;
				
				data.CheckSetCurrentLevel(log.getLevelId());
				data.insertIfBigger(log);
				data.increedGames();
			}
			resultSet.close();
			statement.close();
			connection.close();

			return data;
		}

		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param levelID - the required level.
	 * @param myScore - user best score of this level
	 * @return the number of users that have better scores in this level + 1 (cause if no one is better then you  so you'r in 1st place, not 0.
	 */
	public static int placeInLevel(int levelID, int myScore) {
		HashSet<Integer> greaters = new HashSet<Integer>();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcUserPassword);
			Statement statement = connection.createStatement();
			String allCustomersQuery = "SELECT * FROM Logs 	WHERE levelID = "+levelID
					+" AND score > "+myScore
					+" AND moves <= "+ DBHelper.maxMovesAllaowd(levelID);
			ResultSet resultSet = statement.executeQuery(allCustomersQuery);

			while (resultSet.next()) {
				greaters.add(resultSet.getInt("UserID"));
			}
			resultSet.close();
			statement.close();
			connection.close();
		}

		catch (SQLException sqle) {
			System.out.println("SQLException: " + sqle.getMessage());
			System.out.println("Vendor Error: " + sqle.getErrorCode());
			return -1;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		
		return greaters.size()+1;
	}
	
	/**
	 * @param graph - the graph (level) number
	 * @return - the the max moves [game_service.move()] allowd to pass this game according to server settings.
	 * (server has only this levels)
	 */
	public static int maxMovesAllaowd(int graph) {
		switch (graph) {
		case 0:
			return 290;
			
		case 1:
			return 580;
		
		case 2:
			return 580;
		
		case 5:
			return 500;
			
		case 9:
			return 580;

		case 11:
			return 580;

		case 13:
			return 580;

		case 16:
			return 290;

		case 19:
			return 580;

		case 20:
			return 290;
			
		case 23:
			return 1140;

		default:
			return -1;
		}
	}
	
	/**
	 * @param graph - the graph(level) number
	 * @return - the minimum points that required to pass this game according to server settings.
	 * (server has only this levels)
	 */
	public static int minGradeNeed(int graph) {
		switch (graph) {
		case 0:
			return 125;
			
		case 1:
			return 436;
		
		case 3:
			return 713;
		
		case 5:
			return 570;
			
		case 9:
			return 480;

		case 11:
			return 1050;

		case 13:
			return 310;

		case 16:
			return 235;

		case 19:
			return 250;

		case 20:
			return 200;
			
		case 23:
			return 1000;

		default:
			return Integer.MAX_VALUE;
		}
	}
}
