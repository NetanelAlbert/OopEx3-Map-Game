package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import grapgDataStructure.DGraph;
import grapgDataStructure.DNode;
import grapgDataStructure.edge_data;
import grapgDataStructure.graph;
import grapgDataStructure.node_data;
import utils.Point3D;

class DGraphTest {
	static private DGraph randGraph;
	static private Point3D p = Point3D.ORIGIN;
	@BeforeAll
	static void setRandomGraph() {
		randGraph = getRandomGraph();
	}
	
	@Test
	void testStringConstructor() {
		String s = randGraph.toString();
		DGraph g = new DGraph(s);
		
		assertEquals(randGraph, g, "The 2 graphs aren't equals");
	}
	
	@Test
	void testConnect() {
		DGraph g = new DGraph();
		g.addNode(new DNode(1,p));
		g.addNode(new DNode(2,p));
		
		g.connect(1, 2, 1.1);
		
		try {
			g.connect(1, 3, 1.1);
			fail("the graph shouldn't allow to connect unexist Nodes (3)");
		} catch (Exception e) {}
		
		try {
			g.connect(3, 1, 1.1);
			fail("the graph shouldn't allow to connect unexist Nodes (3)");
		} catch (Exception e) {}
		
		try {
			g.connect(2, 1, -1);
			fail("the graph shouldn't allow to create edge with a negativ weight");
		} catch (Exception e) {}
	
	}
	
	@Test
	void testAddAndGerNode() {
		graph g = new DGraph();
		g.addNode(new DNode(1,p));
		assertEquals(1, g.getNode(1).getKey());
		
		node_data nd = g.getNode(2);
		assertTrue(nd == null, "Node 2 dosn't exist");
		
		try {
			g.addNode(new DNode(1,p));
			fail("the graph shouldn't allow to insert the same key twice");
		} catch (RuntimeException e) {}
		
	}
	
	
	
	@Test
	void testRemoveNode() {
		graph g = new DGraph();
		g.addNode(new DNode(2,p));
		g.removeNode(2);
		
		node_data nd = g.getNode(2);
		assertTrue(nd == null, "Node 2 dosn't exist");
	}
	
	void testRemoveEdge() {
		graph g = new DGraph();
		g.addNode(new DNode(1,p));
		g.addNode(new DNode(2,p));
		g.connect(1, 2, 1.1);
		g.removeEdge(1, 2);
		
		edge_data e = g.getEdge(1, 2);
		assertTrue(e == null, "Edge (1,2) dosn't exist");
	}
	
	@Test
	void testReversCopy() {
		DGraph g = new DGraph();
		g.addNode(new DNode(1,p));
		g.addNode(new DNode(2,p));		
		g.addNode(new DNode(3,p));
		g.connect(1, 2, 1.1);
		g.connect(2, 3, 1.2);
		g.connect(3, 1, 1.3);
		
		DGraph revers = new DGraph();
		revers.addNode(new DNode(1,p));
		revers.addNode(new DNode(2,p));		
		revers.addNode(new DNode(3,p));
		revers.connect(2, 1, 1.1);
		revers.connect(3, 2, 1.2);
		revers.connect(1, 3, 1.3);
		
		assertEquals(revers, g.getReversCopy(), "Reverse dosn't work good");

	}
	
	public static DGraph getRandomGraph() {
		DGraph g = new DGraph();
		int nodesSize = (int)(Math.random()*5)+5;
		for (int i = 0; i < nodesSize; i++) {
			g.addNode(new DNode(i, p));
		}
		for (int i = 0; i < nodesSize; i++) {
			int edgesSize = (int)(Math.random()*nodesSize);
			for (int j = 0; j < edgesSize; j++) {
				int dest = (int)(Math.random()*nodesSize);
				if(i != dest)
					g.connect(i, dest, 1);
			}
		}
		return g;
	}
	
}
