package gameClient;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import grapgDataStructure.DGraph;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;

/**
 * This class propose is to save the live game in KML format file that can be launch from Google earth
 * 
 * @author Netanel Albert
 */
public class KML_Logger {

	private final DGraph graph;
	private final FruitsContainer fruits;
	private final RobotsContainer robots;

	private StringBuilder kmlOut = new StringBuilder();
	private StringBuilder kmlRob = new StringBuilder();
	private StringBuilder kmlFru = new StringBuilder();
	
	private long time = 0;
	
	private String projectPath = System.getProperty("user.dir");
	/**
	 * This constructor is initialize the fields,
	 * and write the graph to the StringBuilder.
	 * 
	 * @param graph - the graph to draw
	 * @param fruits - the fruits to draw
	 * @param robots - the robots to draw
	 */
	public KML_Logger(DGraph graph, FruitsContainer fruits, RobotsContainer robots) {
		this.graph = graph;
		this.fruits = fruits;
		this.robots = robots;

		initKml();
		writeGraph();
		
		initRob();
		initFru();			
		
	}
	
	private void writeRobots() {
		for (Robot rob : robots.getRobots()) {
			writeRobot(rob);
		}
	}
	
	private void writeFruits() {
		for (Fruit fru : fruits.getFruits()) {
			writeFruit(fru);
		}
	}
	
	private void writeFruit(Fruit fru) {
		kmlFru.append("<Placemark>\r\n");
		
		kmlFru.append("<TimeSpan>\r\n<begin>");
		kmlFru.append(time);
		kmlFru.append("</begin>\r\n<end>");
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
		kmlRob.append(time);
		kmlRob.append("</begin>\r\n<end>"); //start
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
		kmlOut.append(projectPath+"/data/robot.png");
		kmlOut.append("        </href>\r\n</Icon>\r\n");
		kmlOut.append("      </IconStyle>\r\n");
		kmlOut.append("    </Style>");

		kmlOut.append("    <Style id=\"appleStyle\">\r\n");
		kmlOut.append("      <IconStyle>\r\n");
		kmlOut.append("        <Icon>\r\n<href>");
		kmlOut.append(projectPath+"/data/apple.png");
		kmlOut.append("        </href>\r\n</Icon>\r\n");
		kmlOut.append("      </IconStyle>\r\n");
		kmlOut.append("    </Style>");
		
		kmlOut.append("    <Style id=\"bananaStyle\">\r\n");
		kmlOut.append("      <IconStyle>\r\n");
		kmlOut.append("        <Icon>\r\n<href>");
		kmlOut.append(projectPath+"/data/banana.png");
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
	}

	private void writeNodes() {
		kmlOut.append("		<Folder><name>Vertexes</name>\r\n");
		kmlOut.append("      <description>Draw the game graph Vertexes</description>\r\n");

		for (node_data node : graph.getV()) {
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

		for (int node : graph.getVnums()) {
			for (edge_data edge : graph.getE(node)) {
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

		kmlOut.append(graph.getNode(edge.getSrc()).getLocation());
		kmlOut.append("\r\n");
		kmlOut.append(graph.getNode(edge.getDest()).getLocation());
		kmlOut.append("\r\n");

		kmlOut.append("        </coordinates>\r\n");
		kmlOut.append("      </LineString>\r\n");
		kmlOut.append("    </Placemark>\r\n");
	}

	/**
	 * Join the 3 StringBuilders together and add the finish of the kml.
	 * need to call it before save(). 
	 */
	public void closeKml() {
		kmlRob.append("</Folder>\r\n");
		kmlFru.append("</Folder>\r\n");

		kmlOut.append(kmlRob);
		kmlOut.append(kmlFru);
		
		kmlOut.append("  </Document>\r\n");
		kmlOut.append("</kml>");
	}

	
	private String log() {
		return kmlOut.toString();
	}

	/**
	 * save the kmlOut to a specific file.
	 * need to call first to closeKml() 
	 * 
	 * @param file_name - the file path
	 */
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

	/**
	 * Write the current fruits and robots to the kml
	 */
	public void writeStatus() {
		writeRobots();
		writeFruits();
		time++;
	}
}
