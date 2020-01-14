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
import gameDataStructure.FruitUpdatingContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsUpdatingContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.DGraph;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

@SuppressWarnings("serial")
public class MyGameGUI extends JFrame implements ActionListener, MenuListener, MouseListener {
	private game_service gameServer;
	private Graph_Algo algo = new Graph_Algo();;
	private ServerInfo serverInfo;
	private FruitUpdatingContainer fruits;
	private RobotsUpdatingContainer robots;

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
	private Image banana;
	private Image apple;
	private Image robot;
	private int graphNum;

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
		
		robots = new RobotsUpdatingContainer(gameServer, serverInfo, algo, EPS);
		fruits = new FruitUpdatingContainer(gameServer, serverInfo);
		repaint();
		String[] modes = { "Manual", "Auto" };
		int mode;
		do {
			mode = JOptionPane.showOptionDialog(null, "Choose game mode", "Game mode", JOptionPane.DEFAULT_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, modes, modes[0]);
		} while (mode == -1);

		if (mode == 0)
			startManualGame();
		else
			startAutoGame();

		autoMoveGame();
		robots.start();
		fruits.start();

	}

	private void startAutoGame() {
		manualGame = false;
		AuotPlayer auto = new AuotPlayer(gameServer, serverInfo, algo, fruits, robots, EPS);
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
				long start = 0, end = 0;
				while (gameServer.isRunning()) {
					start = System.currentTimeMillis();
					gameServer.move();
					try {
						serverInfo.updateServer();
						fruits.updateFruits();
						robots.updateRobots();
						repaint();
						end = System.currentTimeMillis();
						// System.out.println(end-start);
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				GAME_OVER = true;
				repaint();
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
					System.out.println(graphNum);
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

	private void setMenuBar() {
		JMenu file;
		file = new JMenu("File");
		file.addMenuListener(this);

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
		// Double buffering
		BufferedImage bufferedImage = new BufferedImage(WIN_WIDTH, WIN_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bufferedImage.createGraphics();

		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, WIN_WIDTH, WIN_HEIGHT);

		g2d.setFont(new Font("Courier", Font.PLAIN, 20));

		drawGraph(g2d);

		drawFruits(g2d);

		drawRobots(g2d);

		drawStrings(g2d);

		Graphics2D orgGraphic = (Graphics2D) g;
		orgGraphic.drawImage(bufferedImage, null, 0, 0);

	}

	private void drawStrings(Graphics2D g2d) {
		g2d.setColor(Color.BLUE);
		if (serverInfo != null) {
			String info = "";
			info += "Points: " + serverInfo.getGrade();
			if (gameServer.isRunning())
				info += " | Time: " + gameServer.timeToEnd() / 1000;

			g2d.drawString(info, WIN_WIDTH / 4, 50);

			
			g2d.drawString(
					"Level: " + graphNum + " | Fruits: " + serverInfo.getFruits() + " | Robots: " + serverInfo.getRobots(),
					(WIN_WIDTH / 3) * 2, 50);

		}
		if (GAME_OVER) {
			g2d.setFont(new Font("Courier", Font.PLAIN, 50));
			g2d.setColor(Color.RED);
			if (serverInfo != null)
				g2d.drawString("Game Over!", WIN_WIDTH / 2 - 150, 250);
		}

	}

	private void drawRobots(Graphics g) {
		if (robots == null)
			return;

		g.setColor(Color.green);
		for (Robot robot : robots.getRobots()) {
			if (robot != null) {
				int x = scaleX(robot.getPos().x()) - IMAGE_SIZE / 2;
				int y = scaleY(robot.getPos().y()) - IMAGE_SIZE / 2;
				g.drawImage(this.robot, x, y, IMAGE_SIZE, IMAGE_SIZE, null);
				g.drawString("" + robot.getSpeed(), x, y);
			}
		}

		node_data stuck = robots.getStackRobotVertex();
		if (stuck != null) {
			int x = scaleX(stuck.getLocation().x());
			int y = scaleY(stuck.getLocation().y());
			g.setColor(Color.DARK_GRAY);
			g.drawOval(x - NODE_SIZE, y - NODE_SIZE, 2 * NODE_SIZE, 2 * NODE_SIZE);
		}
	}

	private void drawFruits(Graphics g) {
		if (fruits == null)
			return;

		try {
			JSONArray fruits = new JSONArray(gameServer.getFruits());

			for (int i = 0; i < fruits.length(); i++) {
				Fruit f = new Fruit(new JSONObject(fruits.getString(i)).getJSONObject("Fruit"));
				int x = scaleX(f.getPos().x()) - IMAGE_SIZE / 2;
				int y = scaleY(f.getPos().y()) - IMAGE_SIZE / 2;
				if (f.getType() < 0)
					g.drawImage(banana, x, y, IMAGE_SIZE, IMAGE_SIZE, null);
				else
					g.drawImage(apple, x, y, IMAGE_SIZE, IMAGE_SIZE, null);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			banana = ImageIO.read(new File("data/banana.png"));
			apple = ImageIO.read(new File("data/apple.png"));
			robot = ImageIO.read(new File("data/robot.png"));
		} catch (Exception e) {
			e.printStackTrace();
			banana = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = ((BufferedImage) banana).createGraphics(); // not sure on this line, but this seems more
																		// right
			g.setColor(Color.YELLOW);
			g.fillRect(0, 0, 30, 30);

			apple = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			g = ((BufferedImage) apple).createGraphics(); // not sure on this line, but this seems more right
			g.setColor(Color.RED);
			g.fillRect(0, 0, 30, 30);

			robot = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
			g = ((BufferedImage) robot).createGraphics(); // not sure on this line, but this seems more right
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
		node_data stuckRobotNode = robots.getStackRobotVertex();

		if (!manualGame || stuckRobotNode == null)
			return;

		node_data destNode = getNodeByLocation(arg0);
		if (destNode == null)
			return;
		int stuckRobotNum = stuckRobotNode.getKey();
		int newDest = destNode.getKey();

		if (algo.getGraph().getEdge(stuckRobotNum, newDest) == null) {
			JOptionPane.showMessageDialog(null, "Thw dest must be neighbor of the stuck Robot", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		gameServer.chooseNextEdge(robots.getStuckRobotID(), newDest);
		gameServer.move();
		robots.resetStackRobot();

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

	@Override
	public void menuSelected(MenuEvent e) {
		// do nothing
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		repaint(); // to reshow the graph where the menu hide it
	}

	@Override
	public void menuCanceled(MenuEvent e) {
		// do nothing
	}

}
