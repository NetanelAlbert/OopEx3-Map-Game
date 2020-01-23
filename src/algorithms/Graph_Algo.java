package algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import grapgDataStructure.DEdge;
import grapgDataStructure.DGraph;
import grapgDataStructure.DNode;
import grapgDataStructure.edge_data;
import grapgDataStructure.graph;
import grapgDataStructure.node_data;

/**
 * This class holding a graph and make some algorithms on it,
 * Plus adding the option to read / load graph from file
 * 
 * @author Netanel Albert
 *
 */
public class Graph_Algo implements graph_algorithms {
	private static final int NOT_VISITED = 0, VISITED = 1, FINISH = 2;
	private DGraph myGraph;

	public Graph_Algo() {
		this(new DGraph());
	}

	public Graph_Algo(graph g) {
		init(g);
	}

	@Override
	public void init(graph g) {
		if(g instanceof DGraph)
			myGraph = (DGraph) g;
		else
			myGraph = copy(g);
	}

	@Override
	public void init(String file_name) {
		String content;
		try {
			content = new String(Files.readAllBytes(Paths.get(file_name)));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		if (content.length() > 0)
			myGraph = new DGraph(content);
	}

	@Override
	public void save(String file_name) {
		try {
			PrintWriter out = new PrintWriter(file_name);
			out.print(myGraph);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	// Kosaraju’s DFS (two DFS traversals) - O(V+E)
	@Override
	public boolean isConnected() {
		if (myGraph == null || myGraph.nodeSize() > myGraph.edgeSize())
			return false;
		
		if(myGraph.nodeSize() == 0 )
			return true;

		DGraph copy = (DGraph) copy();

		// check if can get from arbitrary node to each node
		for (Iterator<node_data> it = copy.getV().iterator(); it.hasNext();) {
			it.next().setTag(NOT_VISITED);
		}
		DNode arbitrary = (DNode) copy.getV().iterator().next();
		DFS(copy, arbitrary);
		for (Iterator<node_data> it = copy.getV().iterator(); it.hasNext();) {
			node_data node = it.next();
			if (node.getTag() != FINISH) {
				return false;
			}
		}

		// check if can get from each node to same arbitrary node
		DGraph reverse = copy.getReversCopy();
		for (Iterator<node_data> it = reverse.getV().iterator(); it.hasNext();) {
			it.next().setTag(NOT_VISITED);
		}
		arbitrary = (DNode) reverse.getNode(arbitrary.getKey());
		DFS(reverse, arbitrary);
		for (Iterator<node_data> it = reverse.getV().iterator(); it.hasNext();) {
			node_data node = it.next();
			if (node.getTag() != FINISH) {
				return false;
			}
		}

		return true;
	}
	/**
	 * Same as isConnected() but check connection only for the subGraph vertexes
	 * (The path between 2 vertexes in subGrapg can also go through vertex outside the subGraph
	 * @param subGraph - Nodes keys that we want to check connection between
	 * @return true iff there is a path from each vertex to all other vertexes in Subgraph
	 */
	public boolean isConnected(List<Integer> subGraph) {
		if (myGraph == null || myGraph.nodeSize() == 0 || subGraph.size() == 0)
			return false;

		DGraph copy = (DGraph) copy();

		for (Iterator<node_data> it = copy.getV().iterator(); it.hasNext();) {
			it.next().setTag(NOT_VISITED);
		}
		DNode arbitrary = (DNode) copy.getNode(subGraph.get(0));
		if (arbitrary == null)
			throw new RuntimeException("Node dosn't exist (" + subGraph.get(0) + ")");
		DFS(copy, arbitrary);
		// check if can get from arbitrary node to each node
		for (Integer i : subGraph) {
			node_data node = copy.getNode(i);
			if (node == null)
				throw new RuntimeException("Node dosn't exist (" + i + ")");
			if (node.getTag() != FINISH) {
				return false;
			}
		}

		DGraph reverse = copy.getReversCopy();
		for (Iterator<node_data> it = reverse.getV().iterator(); it.hasNext();) {
			it.next().setTag(NOT_VISITED);
		}
		arbitrary = (DNode) reverse.getNode(subGraph.get(0));
		DFS(reverse, arbitrary);
		// check if can get from each node to same arbitrary node
		for (Integer i : subGraph) {
			node_data node = reverse.getNode(i);
			if (node == null)
				throw new RuntimeException("Node dosn't exist (" + i + ")");
			if (node.getTag() != FINISH) {
				return false;
			}
		}

		return true;

	}
	/**
	 * Set tag to VISITED in every Node in g that has path from n to it
	 * https://en.wikipedia.org/wiki/Depth-first_search
	 * @param g - the graph to perform DFS on
	 * @param n - the Node to start from
	 */
	private void DFS(DGraph g, DNode n) {
		n.setTag(VISITED);
		for (Iterator<Integer> it = n.keySet().iterator(); it.hasNext();) {
			DNode neighbor = (DNode) g.getNode(it.next());
			if (neighbor.getTag() == NOT_VISITED)
				DFS(g, neighbor);
		}
		n.setTag(FINISH);
	}

	// Dijkstra's Shortest Path First algorithm - O(E + V*logV)
	@Override
	public double shortestPathDist(int src, int dest) {
		DNode s = (DNode) myGraph.getNode(src);
		DNode d = (DNode) myGraph.getNode(dest);

		if (s == null || d == null) {
			int nullNode = s == null ? src : dest;
			throw new RuntimeException("Node dosn't exist (" + nullNode + ")");
		}
		// Mark all nodes unvisited and set weight 0
		for (Iterator<node_data> it = myGraph.getV().iterator(); it.hasNext();) {
			node_data n = it.next();
			n.setTag(NOT_VISITED);
			n.setWeight(Double.MAX_VALUE);
			((DNode) n).setFather(null);
		}

		s.setWeight(0);
		PriorityBlockingQueue<node_data> notVisited = new PriorityBlockingQueue<node_data>(myGraph.getV());

		while (!notVisited.isEmpty()) {
			node_data current = notVisited.remove();
			if (current.getWeight() == Double.MAX_VALUE || current.getKey() == dest)
				return current.getWeight();

			// change all current unvisited neighbors weight if found shorter path
			for (edge_data e : ((DNode) current).values()) {

				node_data neighbour = myGraph.getNode(e.getDest());
				double newWeight = current.getWeight() + e.getWeight();
				if (neighbour.getTag() == NOT_VISITED && newWeight < neighbour.getWeight()) {
					neighbour.setWeight(newWeight);
					notVisited.remove(neighbour);
					notVisited.add(neighbour);
					((DNode) neighbour).setFather(current);
				}

			}

			current.setTag(VISITED);

		}

		return Double.MAX_VALUE;
	}

	@Override
	public List<node_data> shortestPath(int src, int dest) {
		if (shortestPathDist(src, dest) < Double.MAX_VALUE) {
			ArrayList<node_data> ans = new ArrayList<node_data>();
			DNode cuurent = (DNode) myGraph.getNode(dest);
			do {
				ans.add(0, cuurent);
			} while ((cuurent = cuurent.getFather()) != null);
			return ans;
		}
		return null;
	}

	// complexity ~ n*(E+V*logV) (extrimly worst case)
	@Override
	public List<node_data> TSP(List<Integer> targets) {
		// check if sub graph is connected
		if (targets.isEmpty() || !isConnected(targets))
			return null;

		List<node_data> ans = new ArrayList<node_data>();
		// to allow remove() and removeAll()
		List<Integer> targs = new ArrayList<Integer>(targets);

		int src = targs.get(0);
		if (targets.size() == 1)
			return shortestPath(src, src);

		int dest = targs.get(1);

		while (!targs.isEmpty()) {

			// if the last vertex is the first to come, we don't want it to appear twice
			if (!ans.isEmpty() && ans.get(ans.size() - 1).getKey() == src)
				ans.remove(ans.size() - 1);

			List<node_data> tmp = shortestPath(src, dest);
			// remove from targets list all the vertexes we'v already visit
			targs.removeAll(nodesToInts(tmp));
			ans.addAll(tmp);

			// set the src & dest for the next iteration
			if (!targs.isEmpty()) {
				src = dest;
				dest = targs.get(0);
			}

		}

		return ans;
	}

	/**
	 * 
	 * @return list of the nodes keys
	 */
	private List<Integer> nodesToInts(List<node_data> list) {
		List<Integer> ans = new ArrayList<Integer>();
		for (node_data n : list) {
			ans.add(n.getKey());
		}
		return ans;
	}

	@Override
	public graph copy() {
		return copy(myGraph);
	}
	
	/**
	 * 
	 * @return a copy of g
	 */
	public DGraph copy(graph g) {
		DGraph copy = new DGraph();
		Collection<node_data> nodes = g.getV();
		// copy Nodes
		for (node_data node_data : nodes) {
			copy.addNode(new DNode(node_data));
		}

		// for each Node, copy Edges
		for (node_data orgNode : nodes) {
			for (edge_data edge : g.getE(orgNode.getKey())) {	
				copy.connect(new DEdge(edge));
			}

		}
		return copy;
	}
	
	/** 
	 * @return a pointer to the graph field
	 */
	public DGraph getGraph() {
		return myGraph;
	}

}
