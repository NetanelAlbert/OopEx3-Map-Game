package myDataStructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import gui.GraphGui;

public class DGraph implements graph{
	private HashMap<Integer, node_data> nodes = new HashMap<Integer, node_data>();
	private int edges = 0;	
	private GraphGui gui;
	
	public DGraph() {}
	/**
	 * Construct from a String. Using for load graph from a text file
	 * @param s - String in toString() format
	 */
	public DGraph(String s) {
		String[] parts = s.split(":\n");
		this.edges = Integer.parseInt(parts[0]);
		String[] edges = parts[1].split("\n");
		for (String string : edges) {
			DNode n = new DNode(string);
			nodes.put(n.getKey(), n);
		}
	}
	

	@Override
	public node_data getNode(int key) {
		return nodes.get(key);
	}

	@Override
	public edge_data getEdge(int src, int dest) {
		DNode n = (DNode) nodes.get(src); 
		return  n != null ? n.get(dest) : null;
	}

	@Override
	public void addNode(node_data n) {
		if(nodes.containsKey(n.getKey()))
			throw new RuntimeException("The graph already contains Node with key "+n.getKey());
		
		nodes.put(n.getKey(), n);
		refreshGUI();
	}

	@Override
	public void connect(int src, int dest, double w) {
		DEdge e = new DEdge(src, dest, w);
		connect(e);
	}
	/**
	 * @param e will be the actual edge without a copy
	 */
	public void connect(DEdge e) {
		DNode n = (DNode) nodes.get(e.getSrc());
		if(n != null && nodes.get(e.getDest()) != null) {
			if(!n.containsKey(e.getDest()))
				edges++;
			
				
			n.put(e.getDest(), e);
			refreshGUI();
		} else {
			throw new RuntimeException("Cant connect unexist vertics ("
					+e.getSrc()+","+e.getDest()+"). The nodes are: "+getVnums());
		}
	}

	@Override
	public Collection<node_data> getV() {
		return nodes.values();
	}
	
	/**
	 * @return set of the vertexes numbers
	 */
	public Collection<Integer> getVnums() {
		return nodes.keySet();
	}
	
	@Override
	public Collection<edge_data> getE(int node_id) {
		DNode n = (DNode) nodes.get(node_id);
		return n != null ? n.values() : null;
	}
	
	@Override
	public node_data removeNode(int key) {
		node_data del = nodes.remove(key);
		if(del != null) {
			edges -= ((DNode)del).size();
			
			for (Iterator<Integer> it = nodes.keySet().iterator(); it.hasNext();) {
				removeEdge(it.next(), key);
			}
			refreshGUI();
		}
		return del;
	}

	@Override
	public edge_data removeEdge(int src, int dest) {
		DNode sr = (DNode) nodes.get(src);
		edge_data e = sr  != null ? sr.remove(dest) : null;
		if(e != null) {
			edges--;
			refreshGUI();
		}
		return e;
	}
	
	/**
	 * @return - new DGraph with the same node but
	 * 			 all the edges are in the opposite direction
	 */
	public DGraph getReversCopy() {
		DGraph copy = new DGraph();
		// copy Nodes
		for (Iterator<node_data> iterator = getV().iterator(); iterator.hasNext();) {
			DNode n = new DNode((DNode)iterator.next());
			n.clear();
			copy.addNode(n);
		}
				
		for (Iterator<node_data> itNodes = getV().iterator(); itNodes.hasNext();) {
			DNode n = (DNode)itNodes.next();
			for (Iterator<edge_data> itEdges = getE(n.getKey()).iterator(); itEdges.hasNext();) {
				DEdge e = (DEdge) itEdges.next();
				copy.connect(e.getReversEdge());
			}
		}
		return copy;
	}

	@Override
	public int nodeSize() {
		return nodes.size();
	}

	@Override
	public int edgeSize() {
		return edges;
	}

	@Override
	public int getMC() {
		return 0;
	}
	
	private void refreshGUI() {
		if(gui != null)
			gui.repaint();
	}
	
	public void setGUI(GraphGui gui) {
		this.gui = gui;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof DGraph))
			return false;
		DGraph dGraph = (DGraph) obj;
		return this.edges == dGraph.edges && this.nodes.equals(dGraph.nodes);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(edges);
		sb.append(":\n");
		for (Iterator<node_data> it = nodes.values().iterator(); it.hasNext();) {
			sb.append(it.next() + "\n");
		}
		if(nodes.size() > 0)
			sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}

}
