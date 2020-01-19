package gameClient;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import Server.game_service;
import algorithms.Graph_Algo;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import grapgDataStructure.edge_data;
import grapgDataStructure.graph;
import grapgDataStructure.node_data;
import utils.Point3D;

public class KML_Logger {
//	public static void main(String[] args) {
//		KML_Logger kml = new KML_Logger(null, null, null, null);
//		System.out.println(kml.log());
//	}

	private final Graph_Algo algo;
	private final FruitsContainer fruits;
	private final RobotsContainer robots;

	private StringBuilder kmlOut = new StringBuilder();
	private StringBuilder kmlRob = new StringBuilder();
	private StringBuilder kmlFru = new StringBuilder();

	private SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdfT = new SimpleDateFormat("hh:mm:ss");
	
	long time = 0;

	public KML_Logger(Graph_Algo algo, FruitsContainer fruits, RobotsContainer robots) {
		this.algo = algo;
		this.fruits = fruits;
		this.robots = robots;

		initKml();
		writeGraph();
		
		initRob();
		initFru();			
		
	}
	
	public void writeRobots() {
		for (Robot rob : robots.getRobots()) {
			writeRobot(rob);
		}
	}
	
	public void writeFruits() {
		for (Fruit fru : fruits.getFruits()) {
			writeFruit(fru);
		}
	}
	
	private void writeFruit(Fruit fru) {
		kmlFru.append("<Placemark>\r\n");
		
		kmlFru.append("<TimeSpan>\r\n<begin>"); //start
		kmlFru.append(time);
		kmlFru.append("</begin>\r\n<end>"); //start
		kmlFru.append(time+1);
		kmlFru.append("</end>\r\n</TimeSpan>\r\n");
			
		if(fru.getType() < 0)
			kmlFru.append("<styleUrl>#bananaStyle</styleUrl>");
		else
			kmlFru.append("<styleUrl>#appleStyle</styleUrl>");			
		
		kmlFru.append("<Point><coordinates>");
		kmlFru.append(fru.getPos());
		kmlFru.append("</coordinates></Point>\r\n");
		kmlFru.append("</Placemark>\r\n");	
		
	}

	private void writeRobot(Robot rob) {
		kmlRob.append("<Placemark>\r\n");
		
	
		kmlRob.append("<TimeSpan>\r\n<begin>"); //start
//		kmlRob.append(sdfD.format(start));
//		kmlRob.append("T");
//		kmlRob.append(sdfT.format(start));
//		kmlRob.append("Z");
		kmlRob.append(time);
		kmlRob.append("</begin>\r\n<end>"); //start
//		kmlRob.append(sdfD.format(end));
//		kmlRob.append("T");
//		kmlRob.append(sdfT.format(end));
//		kmlRob.append("Z");
		kmlRob.append(time+1);
		kmlRob.append("</end>\r\n</TimeSpan>\r\n");
		
		
		
		
		kmlRob.append("<styleUrl>#roboStyle</styleUrl>");
		
		kmlRob.append("<Point><coordinates>");
		kmlRob.append(rob.getPos());
		kmlRob.append("</coordinates></Point>\r\n");
		kmlRob.append("</Placemark>\r\n");		
	}

	private void initRob() {
		kmlRob.append("<Folder><name>Robots</name>\r\n");
		kmlRob.append("<description>The location of the robots during the game with timeStamp</description>\r\n");
	}

	private void initFru() {
		kmlFru.append("<Folder><name>Fruits</name>\r\n");
		kmlFru.append("<description>The location of the Fruits during the game with timeStamp</description>\r\n");	
	}
	
	private void initKml() {
		kmlOut.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		kmlOut.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\r\n");
		kmlOut.append("  <Document>\r\n");
		kmlOut.append("    <name>Maze of Waze</name>\r\n");
		kmlOut.append(
				"    <description>Presentation of \"Maze of Waze\" game created by Netanel Albert</description>\r\n");
		
		kmlOut.append("    <Style id=\"edgesYellow\">\r\n");
		kmlOut.append("      <LineStyle>\r\n");
		kmlOut.append("        <color>7f00ffff</color>\r\n");
		kmlOut.append("        <width>5</width>\r\n");
		kmlOut.append("      </LineStyle>\r\n");
		kmlOut.append("    </Style>");
		
		kmlOut.append("    <Style id=\"roboStyle\">\r\n");
		kmlOut.append("      <IconStyle>\r\n");
		kmlOut.append("        <Icon>\r\n<href>");
		kmlOut.append("				data/robot.png");
		kmlOut.append("        </href>\r\n</Icon>\r\n");
		kmlOut.append("      </IconStyle>\r\n");
		kmlOut.append("    </Style>");

		kmlOut.append("    <Style id=\"appleStyle\">\r\n");
		kmlOut.append("      <IconStyle>\r\n");
		kmlOut.append("        <Icon>\r\n<href>");
		kmlOut.append("				data/apple.png");
		kmlOut.append("        </href>\r\n</Icon>\r\n");
		kmlOut.append("      </IconStyle>\r\n");
		kmlOut.append("    </Style>");
		
		kmlOut.append("    <Style id=\"bananaStyle\">\r\n");
		kmlOut.append("      <IconStyle>\r\n");
		kmlOut.append("        <Icon>\r\n<href>");
		kmlOut.append("				data/banana.png");
		kmlOut.append("        </href>\r\n</Icon>\r\n");
		kmlOut.append("      </IconStyle>\r\n");
		kmlOut.append("    </Style>");

		
	}

	private void writeGraph() {
		kmlOut.append("	<Folder><name>Graph</name><open>1</open>\r\n");
		kmlOut.append(" <description>The graph of this game</description>\r\n");

		writEdges();

		writeNodes();

		kmlOut.append("</Folder>\r\n");

		kmlOut.append("");
		kmlOut.append("");
		kmlOut.append("");
		kmlOut.append("");

	}

	private void writeNodes() {
		kmlOut.append("		<Folder><name>Vertexes</name>\r\n");
		kmlOut.append("      <description>Draw the game graph Vertexes</description>\r\n");

		for (node_data node : algo.getGraph().getV()) {
			writeNode(node);
		}

		kmlOut.append("	</Folder>\r\n");
	}

	private void writeNode(node_data n) {
		kmlOut.append("<Placemark> \r\n");
		kmlOut.append("      <name>");
		kmlOut.append(n.getKey());
		kmlOut.append("</name>\r\n");
				
		kmlOut.append(" <Polygon> <outerBoundaryIs>  <LinearRing>  \r\n");
		kmlOut.append("  <coordinates>\r\n");
		
		double x = n.getLocation().x();
		double y = n.getLocation().y();
		double w = 0.0002;
		
		writeCoord(x-w, y-w);
		writeCoord(x-w, y+w);
		writeCoord(x+w, y+w);
		writeCoord(x+w, y-w);
		writeCoord(x-w, y-w);
		
		kmlOut.append("  </coordinates>\r\n");
		kmlOut.append(" </LinearRing> </outerBoundaryIs> </Polygon>\r\n");
		kmlOut.append(" <Style> \r\n");
		kmlOut.append("  <PolyStyle>  \r\n");
		kmlOut.append("   <color>#ff000000</color>\r\n");
		kmlOut.append("   <outline>1</outline>\r\n");
		kmlOut.append("  </PolyStyle> \r\n");
		kmlOut.append(" </Style>\r\n");	
		kmlOut.append("</Placemark>\r\n");


	}
	
	private void writeCoord(double x, double y){
		kmlOut.append(x);
		kmlOut.append(",");
		kmlOut.append(y);
		kmlOut.append("\r\n");
	}

	private void writEdges() {
		kmlOut.append("		<Folder><name>Edges</name>\r\n");
		kmlOut.append("      <description>Draw the game graph edges</description>\r\n");

		for (int node : algo.getGraph().getVnums()) {
			for (edge_data edge : algo.getGraph().getE(node)) {
				writEdge(edge);
			}
		}

		kmlOut.append("	</Folder>\r\n");
	}

	private void writEdge(edge_data edge) {
		kmlOut.append("    <Placemark>\r\n");
		kmlOut.append("      <name>");
		kmlOut.append(edge);
		kmlOut.append("</name>\r\n");
		kmlOut.append("      <styleUrl>#edgesYellow</styleUrl>\r\n");

		kmlOut.append("      <LineString>\r\n");
		kmlOut.append("        <coordinates>\r\n");

		kmlOut.append(algo.getGraph().getNode(edge.getSrc()).getLocation());
		kmlOut.append("\r\n");
		kmlOut.append(algo.getGraph().getNode(edge.getDest()).getLocation());
		kmlOut.append("\r\n");

		kmlOut.append("        </coordinates>\r\n");
		kmlOut.append("      </LineString>\r\n");
		kmlOut.append("    </Placemark>\r\n");
	}

	public void closeKml() {
		kmlRob.append("</Folder>\r\n");
		kmlFru.append("</Folder>\r\n");

		kmlOut.append(kmlRob);
		kmlOut.append(kmlFru);
		
		kmlOut.append("  </Document>\r\n");
		kmlOut.append("</kml>");
	}

	public String log() {
		return kmlOut.toString();
	}

	public void save(String file_name) {

		try {
			PrintWriter out = new PrintWriter(file_name);
			out.print(log());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

	}

	public void writeStatus() {
		writeRobots();
		writeFruits();
		time++;
	}
}
