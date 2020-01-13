package gameClient;

import org.json.JSONException;
import org.json.JSONObject;

import utils.Point3D;

public class Fruit {
	private double value;
	private int type;
	private Point3D pos;
	public Fruit(JSONObject fruit) throws JSONException {
		updateFruits(fruit);
	}
	
	public void updateFruits(JSONObject fruit) throws JSONException {
		this.value = fruit.getInt("value");
		this.type = fruit.getInt("type");
		String[] coords = fruit.getString("pos").split(",");
		Double x = Double.parseDouble(coords[0]);
		Double y = Double.parseDouble(coords[1]);
		this.pos = new Point3D(x, y);
	}
	
	public double getValue() {
		return value;
	}
	public int getType() {
		return type;
	}
	public Point3D getPos() {
		return pos;
	}
	
	
}
