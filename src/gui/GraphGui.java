 package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import algorithms.Graph_Algo;
import myDataStructure.DGraph;
import myDataStructure.DNode;
import myDataStructure.edge_data;
import myDataStructure.graph;
import myDataStructure.node_data;
import utils.Point3D;

public class GraphGui extends JFrame implements ActionListener, MenuListener, MouseListener {
	//private DGraph graph;
	private Graph_Algo algo;
	private java.util.List<node_data> path;
	private JMenu file;
	private JMenu load;
	private JMenuItem exitEdit;
	private boolean editMode = false;
	private final int NODE_SIZE = 10; // need to be even
	private final int ARROW_SIZE = NODE_SIZE - 2;

	public GraphGui() {
		this(new DGraph());
	}

	public GraphGui(graph g) {
		if(!(g instanceof DGraph))
			throw new RuntimeException("g must be instance of DGraph");
		
		
		this.algo = new Graph_Algo(g);
		setTitle("Maze of Waze");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setSize(700, 500);
		setMenuBar();
		addMouseListener(this);

		setVisible(true);
		
		algo.getGraph().setGUI(this);
		
		JOptionPane.showMessageDialog(null, "You can:\n"
				+"Save, load and edit graphs - from 'File' \n"
				+"Run algorithms on a graph - from 'Algorithms'",
				"Welcome to my graph", JOptionPane.INFORMATION_MESSAGE);

	}

	private void setMenuBar() {
		exitEdit = new JMenuItem("Exit edit");
		exitEdit.addActionListener(this);
		exitEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		
		file = new JMenu("File");
		file.addMenuListener(this);
		
		JMenuItem newGraph = new JMenuItem("New");
		newGraph.addActionListener(this);
		newGraph.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.add(newGraph);

		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(this);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		file.add(save);

		load = new JMenu("Load");
		setLoadMenu();
		file.add(load);
		
		JMenuItem remove = new JMenuItem("Remove");
		remove.addActionListener(this);
		file.add(remove);

		JMenuItem edit = new JMenuItem("Edit");
		edit.addActionListener(this);
		file.add(edit);

		JMenu algorithms = new JMenu("Algorithms");
		algorithms.addMenuListener(this);

		JMenuItem isConnected = new JMenuItem("Is connected?");
		isConnected.addActionListener(this);
		algorithms.add(isConnected);

		JMenuItem dist = new JMenuItem("Distance (a->b)");
		dist.addActionListener(this);
		algorithms.add(dist);

		JMenuItem path = new JMenuItem("Shortest path (a->b)");
		path.addActionListener(this);
		algorithms.add(path);

		JMenuItem tsp = new JMenuItem("TSP");
		tsp.addActionListener(this);
		algorithms.add(tsp);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(file);
		menuBar.add(algorithms);

		setJMenuBar(menuBar);
	}
	/**
	 * Load the saves as items on sunMenu 'Load'
	 */
	private void setLoadMenu() {
		load.removeAll();
		File[] files = new File("saves").listFiles();
		if(files == null) {
			new File("saves").mkdir();
			return;
		}
		for (File f : files) {
			String s = f.toString();
			// cut the "saves\" from start and the ".txt" from end
			JMenuItem loadItem = new JMenuItem(s.substring(6, s.length() - 4));
			loadItem.addActionListener(this);
			load.add(loadItem);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setFont(new Font("Courier", Font.PLAIN, 20));
		for (node_data n : algo.getGraph().getV()) {
			g.setColor(Color.black);
			g.fillOval(n.getLocation().ix() - NODE_SIZE / 2, n.getLocation().iy() - NODE_SIZE / 2, NODE_SIZE,
					NODE_SIZE);
			g.drawString("" + n.getKey(), n.getLocation().ix(), n.getLocation().iy() + 15);

			for (edge_data e : algo.getGraph().getE(n.getKey())) {
				g.setColor(Color.RED);
				drawEdge(g, e);
			}
		}

		if (path != null) { // color the edges saved in path in blue
			for (int i = 0; i < path.size() - 1; i++) {
				DNode n = (DNode) path.get(i);
				int next = path.get(i + 1).getKey();
				g.setColor(Color.BLUE);
				drawEdge(g, n.get(next));
			}
		}

		if (src != null) {
			g.setColor(Color.GREEN);
			g.drawOval(src.getLocation().ix() - NODE_SIZE, src.getLocation().iy() - NODE_SIZE, 2 * NODE_SIZE,
					2 * NODE_SIZE);
		}
	}

	private void drawEdge(Graphics g, edge_data e) {
		node_data src = algo.getGraph().getNode(e.getSrc());
		node_data dest = algo.getGraph().getNode(e.getDest());
		int x1 = src.getLocation().ix();
		int y1 = src.getLocation().iy();
		int x2 = dest.getLocation().ix();
		int y2 = dest.getLocation().iy();

		g.drawLine(x1, y1, x2, y2);
		g.drawString("" + e.getWeight(), (x1 + 4 * x2) / 5, (y1 + 4 * y2) / 5);

		g.setColor(Color.yellow);
		g.fillRect(((x1 + 7 * x2) / 8 - ARROW_SIZE / 2), ((y1 + 7 * y2) / 8 - ARROW_SIZE / 2), ARROW_SIZE, ARROW_SIZE);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		switch (e.getActionCommand()) {
		case "Is connected?":
			String massage;
			if (algo.isConnected()) {
				massage = "The graph is strongly connected";

			} else {
				massage = "The graph isn't strongly connected";
			}
			JOptionPane.showMessageDialog(null, massage, e.getActionCommand(), JOptionPane.PLAIN_MESSAGE);

			break;

		case "Distance (a->b)":
			List<Integer> userNums = get2nodesFromUser();
			if (userNums == null)
				break;

			int from = userNums.get(0);
			int to = userNums.get(1);

			double ans;
			try {
				ans = algo.shortestPathDist(from, to);
			} catch (RuntimeException er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
			String distance = (ans < Double.MAX_VALUE) ? ans + "" : "Infinity";
			JOptionPane.showMessageDialog(null, "The distance from " + from + " to " + to + " is: " + distance
					,"Distance (" + from + "->" + to + ")", JOptionPane.PLAIN_MESSAGE);

			break;

		case "Shortest path (a->b)":

			userNums = get2nodesFromUser();
			if (userNums == null)
				break;

			from = userNums.get(0);
			to = userNums.get(1);

			try {
				path = algo.shortestPath(from, to);
			} catch (RuntimeException er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
			String pathDesc = "There is no path from " + from + " to " + to;
			if (path != null)
				pathDesc = pathToString();
			
			repaint();
			JOptionPane.showMessageDialog(null, pathDesc, "shortest path (" + from + "->" + to + ")", JOptionPane.PLAIN_MESSAGE);
			
			path = null;
			repaint();

			break;

		case "TSP":

			String allNodes = algo.getGraph().getVnums().toString();

			userNums = getNodesFromUser(allNodes.substring(1, allNodes.length() - 1));
			if (userNums == null)
				break;

			try {
				path = algo.TSP(userNums);
			} catch (RuntimeException er) {
				JOptionPane.showMessageDialog(null, er.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
			if (path != null) {
				repaint();
				JOptionPane.showMessageDialog(null, pathToString(), "TPS " + userNums,
						JOptionPane.PLAIN_MESSAGE);
				path = null;
				repaint();
			} else
				JOptionPane.showMessageDialog(null, "The Algorithm can't finde a path","TPS " + userNums,
					JOptionPane.ERROR_MESSAGE);
			

			break;
			
		case "New":
			new GraphGui();
			
			break;

		case "Save":
			String fileName = JOptionPane.showInputDialog(null, "Insert file name", "Graph");
			if(fileName == null)
				return;
			if (fileName.length() > 0 && !isButton(fileName)) {		
				algo.save("saves\\" + fileName + ".txt");
				JMenuItem loadItem = new JMenuItem(fileName);
				loadItem.addActionListener(this);
				load.add(loadItem);
			} else if (fileName.length() == 0)
				JOptionPane.showMessageDialog(null, "Can't save with an empty name", "Error",
					JOptionPane.ERROR_MESSAGE);
			else
				JOptionPane.showMessageDialog(null, "Can't save with a name that used in the menu", "Error",
						JOptionPane.ERROR_MESSAGE);
			
			break;
			
		case "Remove":
			String fileToRemove = JOptionPane.showInputDialog(null, "Enter file name to remove");
			if(fileToRemove != null) {
				try {
					Files.deleteIfExists(Paths.get("saves\\" + fileToRemove + ".txt"));
					setLoadMenu();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Can't delete file '"+fileToRemove+"'", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			
			break;

		case "Edit":
			editMode = true;
			file.add(exitEdit);
			JOptionPane.showMessageDialog(null,
					"Click empty place to add vertex.\n" 
							+ "Click on 2 vertexes to add Edge from the 1st to the 2nd.\n"+
							"Ctrl+Z to exit Edit mode",
					"Edit mode", JOptionPane.INFORMATION_MESSAGE);
			break;

		case "Exit edit":
			editMode = false;
			file.remove(exitEdit);
			src = null;
			repaint();
			break;

		default: // this is a file name we want to load
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null,
					"If you load without saving you'll lose the currnt graph" + "\nDo you want to load?")) {
				try {
					algo.getGraph().setGUI(null);
					algo.init("saves\\" + e.getActionCommand() + ".txt");					
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null,"Can't load file '"+e.getActionCommand()+"'",
							"Load Error", JOptionPane.ERROR_MESSAGE);
				}
				algo.getGraph().setGUI(this);
				repaint();
			}
			break;
		}

	}
	private static String[] buttons = {
			"Is connected?",
			"Distance (a->b)",
			"Shortest path (a->b)",
			"TSP",
			"New",
			"Save",
			"Remove",
			"Edit",
			"Exit edit"};
	
	private boolean isButton(String s) {
		for (String string : buttons) {
			if(s.equals(string))
				return true;
		}
		return false;
	}

	private List<Integer> get2nodesFromUser() {
		List<Integer> userNums = getNodesFromUser("a,b");
		if (userNums == null)
			return null;
		if (userNums.size() != 2) {
			JOptionPane.showMessageDialog(null, "Path is define for 2 nodes only", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return userNums;
	}

	private List<Integer> getNodesFromUser(String defValue) {
		String userAns = JOptionPane.showInputDialog(null, "Insert Nodes separate with ','", defValue);
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
				userNums.add(Integer.parseInt(s));
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
	 * @return the path List [a,b,c....] as "a->b->c....."
	 */
	private String pathToString() {
		if (path == null) {
			return "No path";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < path.size(); i++) {
			sb.append(path.get(i).getKey());
			if (i != path.size() - 1)
				sb.append("->");
		}
		return sb.toString();
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
	/**
	 * 
	 * @param e - the event with the information about the user click
	 * @return the node of this graph that set to the same location, if exist.
	 */
	private node_data getNodeByLocation(MouseEvent e) {
		for (node_data n : algo.getGraph().getV()) {
			Point3D p = n.getLocation();
			if (Math.abs(p.x() - e.getX()) <= NODE_SIZE && Math.abs(p.y() - e.getY()) <= NODE_SIZE)
				return n;
		}
		return null;
	}

	private node_data src;
	// add vertexes and Edges
	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (!editMode)
			return;
		node_data n = getNodeByLocation(arg0);

		boolean newN = (n == null);
		if (newN) {
			n = new DNode(algo.getGraph().nodeSize() + 1, new Point3D(arg0.getX(), arg0.getY()));
			algo.getGraph().addNode(n);
		}
		if (src != null) {

			String userAns = JOptionPane.showInputDialog(null, "Do you want to add Edge from " + src.getKey() + " to "
					+ n.getKey() + "?\n" + "Enter positiv real weight");
			if (userAns == null)
				return;
			try {
				double w = Double.parseDouble(userAns);
				algo.getGraph().connect(src.getKey(), n.getKey(), w);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "'" + userAns + "' is not a real number", "Error",
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				src = null;
			}
		} else if (!newN) {
			src = n;
		}
		repaint();
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
