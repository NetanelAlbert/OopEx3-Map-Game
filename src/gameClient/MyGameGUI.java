package gameClient;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.DGraph;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

@SuppressWarnings("serial")
public class MyGameGUI extends JFrame implements ActionListener, MouseListener {
	public static void main(String[] args) {
		new MyGameGUI();
	}
	
	
	private game_service gameServer;
	private Graph_Algo algo = new Graph_Algo();;
	private ServerInfo serverInfo;
	private FruitsContainer fruits;
	private RobotsContainer robots;

	private boolean manualGame = true;

	private final int WIN_WIDTH = 1200;
	private final int WIN_HEIGHT = 700;
	private final int NODE_SIZE = 10; // need to be even
	private final int ARROW_SIZE = NODE_SIZE - 2;
	private final int IMAGE_SIZE = 20; // need to be even

	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;

	private double EPS;
	private boolean GAME_OVER;
	private Image bananaIMG;
	private Image appleIMG;
	private Image robotIMG;
	private int graphNum;
	
	KML_Logger logger;

	public MyGameGUI() {
		setTitle("Maze of Waze - The Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setSize(WIN_WIDTH, WIN_HEIGHT);
		setMenuBar();
		addMouseListener(this);
		setFruitsImages();

		setVisible(true);

		initGame();
	}

	private void initGame() {
		initGraph();
		GAME_OVER = false;
		try {
			serverInfo = new ServerInfo(gameServer);
		} catch (JSONException e) {
			JOptionPane.showMessageDialog(null, "Server informetion is wrong. exiting game.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		robots = new RobotsContainer(gameServer, serverInfo);
		fruits = new FruitsContainer(gameServer, serverInfo);
		repaint();
		
		////// kml

		logger = new KML_Logger(algo, fruits, robots);
		

		////// kml
		
		String[] modes = { "Manual", "Auto" };
		int mode;
		do {
			mode = JOptionPane.showOptionDialog(null, "Choose game mode", "Game mode", JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, modes, modes[1]);
		} while (mode == -1);

		if (mode == 0)
			startManualGame();
		else
			startAutoGame();

		autoMoveGame();
	}

	private void startAutoGame() {
		manualGame = false;
		AutoPlayer2 auto = new AutoPlayer2(gameServer, serverInfo, algo, fruits, robots, EPS);
		gameServer.startGame();
		auto.start();
	}

	private void startManualGame() {
		manualGame = true;
		List<Integer> robotsPos;
		do {
			robotsPos = get_i_nodesFromUser(serverInfo.getRobots());
		} while (robotsPos == null);

		for (Integer i : robotsPos) {
			gameServer.addRobot(i);
		}
		JOptionPane.showMessageDialog(null, "When robots stops, press neighbor node to move.");

		gameServer.startGame();

	}

	private void autoMoveGame() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				long start = 0, end = 0, max = 0, count = 0, sum = 0, current;
				while (gameServer.isRunning()) {
					start = System.currentTimeMillis();
					gameServer.move();
					try {
						serverInfo.updateServer();
						fruits.updateFruits();
						robots.updateRobots();
						repaint();
						
						logger.writeStatus();
						
						end = System.currentTimeMillis();
						current = end - start;
						// System.out.println(current);
						if (current > max)
							max = current;
						count++;
						sum += current;
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				GAME_OVER = true;
				repaint();
				logger.closeKml();
				logger.save("gameGui.kml");
				
				System.out.println("max delay: " + max);
				System.out.println("average: " + sum / count);
			}
		};
		new Thread(r).start();
	}

	private void initGraph() {
		DGraph g = null;

		while (g == null) {
			String scenarioStr = JOptionPane
					.showInputDialog("Welcome to my game \n" + "Pleas choose scenario (number between 0 to 23)", 0);
			if (scenarioStr != null) {
				scenarioStr = scenarioStr.replaceAll(" ", "");
				try {
					graphNum = Integer.parseInt(scenarioStr);
					if (graphNum < 0 || 23 < graphNum)
						continue;

					gameServer = Game_Server.getServer(graphNum); // you have [0,23] games
					String graphStr = gameServer.getGraph();
					g = new DGraph(new JSONObject(graphStr));
				} catch (Exception e) {
					System.out.println("MyGameGUI initGame() ERROR:");
					e.printStackTrace();
				}
			}
		}

		algo.init(g);
		refactorMinMaxXY();
		
		creatGraphIMG();
		repaint();		
	}

	private void refactorMinMaxXY() {
		if (algo == null || algo.getGraph() == null)
			return;

		for (node_data node : algo.getGraph().getV()) {
			if (node.getLocation().x() < minX)
				minX = node.getLocation().x();

			if (node.getLocation().x() > maxX)
				maxX = node.getLocation().x();

			if (node.getLocation().y() < minY)
				minY = node.getLocation().y();

			if (node.getLocation().y() > maxY)
				maxY = node.getLocation().y();
		}

		EPS = (maxX - minX) / WIN_WIDTH;

	}

	private int scaleX(double x) {
		if (x > maxX || x < minX)
			refactorMinMaxXY();

		return (int) ((x - minX) / (maxX - minX) * (WIN_WIDTH - 50) + 25);
	}

	private int scaleY(double y) {
		if (y > maxY || y < minY)
			refactorMinMaxXY();

		return WIN_HEIGHT - (int) ((y - minY) / (maxY - minY) * (WIN_HEIGHT - 100) + 30);
	}

	JMenuBar menuBar;

	private void setMenuBar() {
		JMenu file;
		file = new JMenu("File");

		JMenuItem newGraph = new JMenuItem("New");
		newGraph.addActionListener(this);
		newGraph.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.add(newGraph);

		menuBar = new JMenuBar();
		menuBar.add(file);

		setJMenuBar(menuBar);
	}

	private BufferedImage graphImage;
	// TODO check if need to recreate when change window size

	@Override
	public void paint(Graphics g) {
		if(graphImage == null)
			return;
		
		BufferedImage bufferedImage = new BufferedImage(WIN_WIDTH, WIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(graphImage, null, 0, 0);
		g2d.setFont(new Font("Courier", Font.PLAIN, 20));
		
		drawFruits(g2d);

		drawRobots(g2d);

		drawStrings(g2d);

		Graphics2D orgGraphic = (Graphics2D) g;
		orgGraphic.drawImage(bufferedImage, null, 0, 0);
	}

	private void drawStrings(Graphics g2d) {
		g2d.setColor(Color.BLUE);
		g2d.setFont(new Font("Courier", Font.PLAIN, 30));
		if (serverInfo != null) {
			String info = "";
			info += "Points: " + serverInfo.getGrade();
			if (gameServer.isRunning())
				info += " | Time: " + gameServer.timeToEnd() / 1000;

			g2d.drawString(info, WIN_WIDTH / 4, 50);

			g2d.drawString("Level: " + graphNum + " | Fruits: " + serverInfo.getFruits() + " | Robots: "
					+ serverInfo.getRobots(), (WIN_WIDTH / 3) * 2, 50);

			if (GAME_OVER) {
				g2d.setFont(new Font("Courier", Font.PLAIN, 50));
				g2d.setColor(Color.RED);
				if (serverInfo != null)
					g2d.drawString("Game Over!", WIN_WIDTH / 2 - 150, 250);
			}
		}

	}

	private void drawRobots(Graphics g) {
		if (robots == null)
			return;

		for (Robot robot : robots.getRobots()) {
			if (robot != null) {
				g.setColor(Color.green);
				int x = scaleX(robot.getPos().x()) - IMAGE_SIZE / 2;
				int y = scaleY(robot.getPos().y()) - IMAGE_SIZE / 2;
				g.drawImage(this.robotIMG, x, y, IMAGE_SIZE, IMAGE_SIZE, null);
				g.drawString("" + robot.getSpeed(), x, y);

				if (robot.getDest() == -1) {
					g.setColor(Color.DARK_GRAY);
					g.drawOval(x - IMAGE_SIZE, y - IMAGE_SIZE, 3 * IMAGE_SIZE, 3 * IMAGE_SIZE);
				}
			}
		}
	}

	private void drawFruits(Graphics g) {
		if (fruits == null)
			return;

		g.setColor(Color.blue);
		for (Fruit f : fruits.getFruits()) {
			int x = scaleX(f.getPos().x()) - IMAGE_SIZE / 2;
			int y = scaleY(f.getPos().y()) - IMAGE_SIZE / 2;
			if (f.getType() < 0)
				g.drawImage(bananaIMG, x, y, IMAGE_SIZE, IMAGE_SIZE, null);
			else
				g.drawImage(appleIMG, x, y, IMAGE_SIZE, IMAGE_SIZE, null);

			g.drawString(f.getValue() + "", x, y);
		}
	}

	private void creatGraphIMG() {
		graphImage = new BufferedImage(WIN_WIDTH, WIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);

		// Double buffering
		Graphics2D g2d = graphImage.createGraphics();

		g2d.setBackground(Color.WHITE);
		super.paint(g2d);

		g2d.setFont(new Font("Courier", Font.PLAIN, 20));

		drawGraph(g2d);

	}

	private void drawGraph(Graphics2D g2d) {
		if (algo == null || algo.getGraph() == null)
			return;

		for (node_data n : algo.getGraph().getV()) {
			int x = scaleX(n.getLocation().x());
			int y = scaleY(n.getLocation().y());
			g2d.setColor(Color.black);
			g2d.fillOval(x - NODE_SIZE / 2, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
			g2d.drawString("" + n.getKey(), x, y + 15);

			for (edge_data e : algo.getGraph().getE(n.getKey())) {
				drawEdge(g2d, e);
			}
		}
	}

	private void drawEdge(Graphics g, edge_data e) {
		node_data src = algo.getGraph().getNode(e.getSrc());
		node_data dest = algo.getGraph().getNode(e.getDest());
		int x1 = scaleX(src.getLocation().x());
		int y1 = scaleY(src.getLocation().y());
		int x2 = scaleX(dest.getLocation().x());
		int y2 = scaleY(dest.getLocation().y());

		g.setColor(Color.DARK_GRAY);
		g.drawLine(x1, y1, x2, y2);
		double w = (int) (e.getWeight() * 10);
		w /= 10;
		g.drawString("" + w, (x1 + 4 * x2) / 5, (y1 + 4 * y2) / 5);

		g.setColor(Color.yellow);
		g.fillRect(((x1 + 7 * x2) / 8 - ARROW_SIZE / 2), ((y1 + 7 * y2) / 8 - ARROW_SIZE / 2), ARROW_SIZE, ARROW_SIZE);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {

		case "New":
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
					"Are you sure you want to exit this game and start a new one??")) {

				gameServer.stopGame();
				initGame();

			}
		}

	}

	private void setFruitsImages() {
		try {
			bananaIMG = ImageIO.read(new File("data/banana.png"));
			appleIMG = ImageIO.read(new File("data/apple.png"));
			robotIMG = ImageIO.read(new File("data/robot.png"));
		} catch (Exception e) {
			e.printStackTrace();
			bananaIMG = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = ((BufferedImage) bananaIMG).createGraphics(); // not sure on this line, but this seems more
																		// right
			g.setColor(Color.YELLOW);
			g.fillRect(0, 0, 30, 30);

			appleIMG = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			g = ((BufferedImage) appleIMG).createGraphics(); // not sure on this line, but this seems more right
			g.setColor(Color.RED);
			g.fillRect(0, 0, 30, 30);

			robotIMG = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			g = ((BufferedImage) robotIMG).createGraphics(); // not sure on this line, but this seems more right
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, 30, 30);
			g.setColor(Color.GREEN);
			g.fillOval(0, 0, 30, 30);
			;
		}
	}

	private List<Integer> get_i_nodesFromUser(int i) {
		List<Integer> userNums = getNodesFromUser("", i);
		if (userNums == null)
			return null;
		if (userNums.size() != i) {
			JOptionPane.showMessageDialog(null, "You need to choose exacly " + i + " vertexes", "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return userNums;
	}

	private List<Integer> getNodesFromUser(String defValue, int num) {
		String userAns = JOptionPane.showInputDialog(null, "Insert " + num + " Nodes separate with ','", defValue);
		if (userAns == null)
			return null;

		if (userAns.length() == 0) {
			JOptionPane.showMessageDialog(null, "You must insert numbers", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		userAns = userAns.replaceAll(" ", "");
		String[] userAnsSplit = userAns.split(",");
		List<Integer> userNums = new ArrayList<Integer>();
		for (String s : userAnsSplit) {
			try {
				int node = Integer.parseInt(s);
				if (!algo.getGraph().getVnums().contains(node)) {
					JOptionPane.showMessageDialog(null, node + " isn't a valid Vertex", "Error",
							JOptionPane.ERROR_MESSAGE);
					return null;
				}

				userNums.add(node);
			} catch (NumberFormatException er) {
				JOptionPane.showMessageDialog(null, "Wrong format. '" + s + "' isn't an Integer", "Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return userNums;
	}

	/**
	 * 
	 * @param e - the event with the information about the user click
	 * @return the node of this graph that set to the same location, if exist.
	 */
	private node_data getNodeByLocation(MouseEvent e) {
		for (node_data n : algo.getGraph().getV()) {
			Point3D p = n.getLocation();
			if (Math.abs(scaleX(p.x()) - e.getX()) <= NODE_SIZE && Math.abs(scaleY(p.y()) - e.getY()) <= NODE_SIZE)
				return n;
		}
		return null;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		if (!manualGame)
			return;

		node_data destNode = getNodeByLocation(arg0);
		if (destNode == null)
			return;

		for (Robot rob : robots.getRobots()) {
			if (rob.getDest() != -1)
				continue;

			edge_data e = algo.getGraph().getEdge(rob.getSrc(), destNode.getKey());

			if (e != null) {
				gameServer.chooseNextEdge(rob.getId(), destNode.getKey());
				return;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// do nothing
	}

}
