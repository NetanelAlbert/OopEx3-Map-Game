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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.json.JSONException;
import org.json.JSONObject;

import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataBase.DBHelper;
import dataBase.Log;
import dataBase.MyGamesData;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.DGraph;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

/**
 * This class creating a window with the game.
 * 
 * All the functions of the GUI and the manual game mode are in this class.
 * 
 * @author Netanel Albert
 */
@SuppressWarnings("serial")
public class MyGameGUI extends JFrame implements ActionListener, MouseListener {

	private game_service gameServer;
	private Graph_Algo algo = new Graph_Algo();
	private ServerInfo serverInfo;
	private FruitsContainer fruits;
	private RobotsContainer robots;

	private boolean manualGame = true;

	private final int NODE_SIZE = 10; // need to be even
	private final int ARROW_SIZE = NODE_SIZE - 2;
	private final int IMAGE_SIZE = 20; // need to be even

	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;

	private double EPS = 0.00001;
	private boolean GAME_OVER;
	private Image bananaIMG;
	private Image appleIMG;
	private Image robotIMG;
	private int graphNum;
	private boolean loggedIn = false;

	private BufferedImage graphImage;
	// TODO check if need to recreate when change window size

	KML_Logger logger;

	/**
	 * Empty constructor - initialize the GUI and show the window
	 */
	public MyGameGUI() {
		setTitle("Maze of Waze - The Game");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setSize(1200, 700);

		setMenuBar();
		addMouseListener(this);
		setFruitsImages();

		setVisible(true);

		login();
		initGame();
	}

	private void login() {

		String idStr = JOptionPane.showInputDialog("Welcome to my game \n" + "Pleas please insert your ID", 0);
		int id = 0;
		if (idStr != null) {
			try {
				id = Integer.valueOf(idStr);
				loggedIn = Game_Server.login(id);
				System.out.println("in: " + loggedIn);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error", "You have to type an integer Number",
						JOptionPane.ERROR_MESSAGE);
			}
			if (loggedIn)
				JOptionPane.showMessageDialog(null, "Connect", "You are connecting as " + id,
						JOptionPane.PLAIN_MESSAGE);
			else
				JOptionPane.showMessageDialog(null, "Error", "Connectiom error, pleas try again.",
						JOptionPane.ERROR_MESSAGE);

		}
		
		//if(loggedIn)
			//showStatistics(id);
			

	}
	
	private void showStatistics(int userID) {
		MyGamesData gameData = DBHelper.myInfo(userID);
		StringBuilder sb = new StringBuilder("Statistics for id "+userID +"\n");
		sb.append("Your level is: ");
		sb.append(gameData.getCurrentLevel());
		sb.append(". You played ");
		sb.append(gameData.getPlayedGames());
		sb.append(" games. \n\n");

		
		sb.append("Your best scores: \n");
		for (Log log : gameData.getTopScors().values()) {
			sb.append(log);
			sb.append("\n");
		}
		
		sb.append("\nYour place in levels you reeched: \n");
		for (Log log : gameData.getTopScors().values()) {
			sb.append("Level ");
			sb.append(log.getLevelId());
			sb.append(": ");
			sb.append(DBHelper.placeInLevel(log.getLevelId(), log.getScore()));
			sb.append("\n");
		}
		
		JOptionPane.showMessageDialog(null, sb, "Statistics", JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Do all what needed to start a game. Need to be called from the constructor
	 * and when needed to start a new game (user choose to).
	 */
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

		logger = new KML_Logger(algo.getGraph(), fruits, robots);

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

	/**
	 * Run loop in a new thread that reload the data from server and repaint.
	 */
	private void autoMoveGame() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				long prev = System.currentTimeMillis();
				while (isRunning(gameServer)) {
					gameServer.move();
					try {
						serverInfo.updateServer();
						fruits.updateFruits();
						robots.updateRobots();
						repaint();

						long now = System.currentTimeMillis();
						if (now - prev > 150) {
							logger.writeStatus();
							prev = now;
						}

						Thread.sleep(150);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				GAME_OVER = true;
				repaint();
				logger.closeKml();

				askAndSave();
			}

		};
		new Thread(r).start();
	}

	private void askAndSave() {
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Do you want to save the game log locally?")) {

			// Taken from
			// https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("kml"));

			int result = fileChooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String path = selectedFile.getAbsolutePath();
				if (!path.endsWith(".kml"))
					path += ".kml";
				logger.save(path);
			}
		}
		
		if(loggedIn && serverInfo.getMoves() <= DBHelper.maxMovesAllaowd(graphNum) && serverInfo.getGrade() >= DBHelper.minGradeNeed(graphNum)) {
			if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "You past this level. \nDo you want to send the game log to server?"))
				gameServer.sendKML(logger.log());
		}
	}

	/**
	 * Initialize graph from the server according to the user choice.
	 */
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
					e.printStackTrace();
				}
			}
		}

		algo.init(g);
		refactorMinMaxXY();

		creatGraphIMG();
		repaint();
	}

	/**
	 * Set the min & max x,y for later adjusting of the graph to the window
	 */
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
	}

	/**
	 * @param x the x coordinate to convert
	 * @return the location on the screen that fit that x
	 */
	private int scaleX(double x) {
		if (x > maxX || x < minX)
			refactorMinMaxXY();

		return (int) ((x - minX) / (maxX - minX) * (getWidth() - 50) + 25);
	}

	/**
	 * @param y the y coordinate to convert
	 * @return the location on the screen that fit that y
	 */
	private int scaleY(double y) {
		if (y > maxY || y < minY)
			refactorMinMaxXY();

		return getHeight() - (int) ((y - minY) / (maxY - minY) * (getHeight() - 100) + 30);
	}

	/**
	 * Set the meneBar of this window
	 */
	private void setMenuBar() {
		JMenu file;
		file = new JMenu("File");

		JMenuItem newGraph = new JMenuItem("New");
		newGraph.addActionListener(this);
		newGraph.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.add(newGraph);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);

		setJMenuBar(menuBar);
	}

	@Override
	public void paint(Graphics g) {
		if (graphImage == null)
			return;

		BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(graphImage, null, 0, 0);
		g2d.setFont(new Font("Courier", Font.PLAIN, 20));

		drawFruits(g2d);

		drawRobots(g2d);

		drawStrings(g2d);

		Graphics2D orgGraphic = (Graphics2D) g;
		orgGraphic.drawImage(bufferedImage, null, 0, 0);
		
		//getJMenuBar().updateUI();
		// TODO fix menu hiding
	}

	/**
	 * Write the game status up in the screen
	 */
	private void drawStrings(Graphics g2d) {
		g2d.setColor(Color.BLUE);
		g2d.setFont(new Font("Courier", Font.PLAIN, getWidth() / 40));
		if (serverInfo != null) {
			String info = "";
			info += "Points: " + serverInfo.getGrade();
			if (isRunning(gameServer))
				info += " | Time: " + gameServer.timeToEnd() / 1000;

			g2d.drawString(info, getWidth() / 4, 80);

			g2d.drawString("Level: " + graphNum + " | Fruits: " + serverInfo.getFruits() + " | Robots: "
					+ serverInfo.getRobots(), (getWidth() / 3) * 2, 80);

			if (GAME_OVER) {
				g2d.setFont(new Font("Courier", Font.PLAIN, 50));
				g2d.setColor(Color.RED);
				if (serverInfo != null)
					g2d.drawString("Game Over!", getWidth() / 2 - 150, 250);
			}
		}

	}

	/**
	 * Draw the robots in their current place on the screen
	 */
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

	/**
	 * Draw the fruits in their current place on the screen
	 */
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
		graphImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

		// Double buffering
		Graphics2D g2d = graphImage.createGraphics();

		g2d.setBackground(Color.WHITE);
		super.paint(g2d);

		g2d.setFont(new Font("Courier", Font.PLAIN, 20));

		drawGraph(g2d);

	}

	/**
	 * Draw the graph vertexes and edges in their current place on the screen
	 */
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

	/**
	 * Draw a single edge
	 */
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

	/**
	 * Load the image from files. If failed, create simple shape of them.
	 */
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
	public void validate() {
		super.validate();
		creatGraphIMG();
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
		// do nothing - need just the 'mouseClicked' but has to implement them all
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// do nothing - need just the 'mouseClicked' but has to implement them all
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// do nothing - need just the 'mouseClicked' but has to implement them all
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// do nothing - need just the 'mouseClicked' but has to implement them all
	}

	/**
	 * Need to use this when calling from multiple threads to avoid exceptions
	 */
	public static synchronized boolean isRunning(game_service gameServe) {
		return gameServe.isRunning();
	}
}
