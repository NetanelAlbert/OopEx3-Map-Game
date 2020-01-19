package gameDataStructure;

import org.json.JSONException;
import org.json.JSONObject;
import utils.Point3D;

/**
 * This class represent a robot in the game
 * 
 * @author Netanel Albert
 */
public class Robot {
	private int id;
	private double value;
	private int src;
	private int dest;
	private double speed;
	private Point3D pos;
	
	boolean firstUpdate = true;
	
	/**
	 * 
	 * @param robot - JSON with the robot information
	 * @throws JSONException - if 'robot' isn't contains the correct fields
	 */
	public Robot(JSONObject robot) throws JSONException {
		updateRobot(robot);
		firstUpdate = false;
	}
	/**
	 * 
	 * @param robot - JSON with the robot information
	 * @throws JSONException - if 'robot' isn't contains the correct fields
	 */
	public void updateRobot(JSONObject robot) throws JSONException {
		int id = robot.getInt("id");
		if(firstUpdate)
			this.id = id;
		else if(this.id != id)
			throw new RuntimeException("id of robot shouldn't change");
		
		this.value = robot.getDouble("value");
		this.src = robot.getInt("src");
		this.dest = robot.getInt("dest");
		this.speed = robot.getDouble("speed");
		String[] coords = robot.getString("pos").split(",");
		Double x = Double.parseDouble(coords[0]);
		Double y = Double.parseDouble(coords[1]);
		this.pos = new Point3D(x, y);
	}
	
	public int getId() {
		return id;
	}
	public double getValue() {
		return value;
	}
	public int getSrc() {
		return src;
	}
	public int getDest() {
		return dest;
	}
	public double getSpeed() {
		return speed;
	}
	public Point3D getPos() {
		return pos;
	}
	
	/**
	 * @return this.id as a string (for debugging)
	 */
	@Override
	public String toString() {
		return ""+id;
	}
}
