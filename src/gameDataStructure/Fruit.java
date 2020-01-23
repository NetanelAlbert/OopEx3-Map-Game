package gameDataStructure;

import org.json.JSONException;
import org.json.JSONObject;
import utils.Point3D;

/**
 * This class represent a fruit in the game
 * 
 * @author Netanel Albert
 */
public class Fruit implements Comparable<Fruit>{
	private double value;
	private int type;
	private Point3D pos;
	
	/**
	 * @param robot - JSON with the fruit information
	 * @throws JSONException - if 'fruit' isn't contains the correct fields
	 */
	public Fruit(JSONObject fruit) throws JSONException {
		updateFruit(fruit);
	}
	/**
	 * @param robot - JSON with the fruit information
	 * @throws JSONException - if 'fruit' isn't contains the correct fields
	 */
	public void updateFruit(JSONObject fruit) throws JSONException {
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

	/*
	 * using to sort a fruits array from high to low value
	 */
	@Override
	public int compareTo(Fruit f) {
		Double v = this.value;
		return -v.compareTo(f.value);
	}
	/**
	 * for debugging
	 */
	@Override
	public String toString() {
		return ""+pos;
	}

	
}
