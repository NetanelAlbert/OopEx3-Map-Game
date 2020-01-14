package gameDataStructure;

import org.json.JSONException;
import org.json.JSONObject;

import utils.Point3D;

public class Robot {
	private int id;
	private double value;
	private int src;
	private int dest;
	private double speed;
	private Point3D pos;
	
	
	
	public Robot(JSONObject robot) throws JSONException {
		updateRobot(robot);
	}
	
	public void updateRobot(JSONObject robot) throws JSONException {
		this.id = robot.getInt("id");
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
	
	@Override
	public String toString() {
		return ""+id;
	}
}
