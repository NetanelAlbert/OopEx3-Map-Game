package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import algorithms.Graph_Algo;
import grapgDataStructure.DGraph;
import grapgDataStructure.DNode;
import grapgDataStructure.node_data;
import utils.Point3D;

class GrapAlgoTest {
	private static DGraph randGraph;
	private static Graph_Algo alg;
	private static Point3D p = Point3D.ORIGIN;

	
	@BeforeAll
	static void setRandomGraph() {
		randGraph = DGraphTest.getRandomGraph();
		alg = new Graph_Algo();
		alg.init(randGraph);
	}
	@Test
	void testSaveAndInit() {
		alg.save("Test");
		Graph_Algo g = new Graph_Algo();
		g.init("Test");
		assertEquals(alg.getGraph(), g.getGraph());
	}
	
	@Test
	void testCopy() {
		DGraph g = DGraphTest.getRandomGraph();
		DGraph copy =(DGraph) new Graph_Algo().copy(g);
		assertEquals(g, copy);
	}

	@Test
	void testIsConnected() {
		DGraph g = new DGraph();
		alg.init(g);
		assertTrue(alg.isConnected());
		
		g.addNode(new DNode(1,p));
		g.addNode(new DNode(2,p));		
		g.addNode(new DNode(3,p));
		g.connect(1, 2, 1.1);
		g.connect(2, 3, 1.2);
		alg.init(g);
		assertFalse(alg.isConnected());
		
		g.connect(3, 1, 1.3);
		
		alg.init(g);
		assertTrue(alg.isConnected());
	}
	
	@Test
	void testShortestPathAndDist() {
		DGraph g = new DGraph();
		g.addNode(new DNode(1,p));
		g.addNode(new DNode(2,p));		
		g.addNode(new DNode(3,p));
		g.addNode(new DNode(4,p));
		g.addNode(new DNode(5,p));
		
		g.connect(1, 2, 1.1);
		g.connect(2, 3, 1.1);
		g.connect(3, 4, 1.1);
		g.connect(4, 5, 1.1);
		
		g.connect(1, 5, 10);
		
		alg.init(g);
		assertEquals(4.4, alg.shortestPathDist(1, 5));
		assertEquals(5, alg.shortestPath(1, 5).size());
	}
	
	@Test
	void testTSP() {
		DGraph g = new DGraph();
		List<node_data> exp = new ArrayList<node_data>();
		for (int i = 8; i < 10; i++) {
			node_data n = new DNode(i,p);
			g.addNode(n);
			exp.add(n);
		}
		for (int i = 0; i < 8; i++) {
			node_data n = new DNode(i,p);
			g.addNode(n);
			exp.add(n);
		}
		for (int i = 0; i < 10; i++) {
			g.connect(i, (i+1)%10, 1.1);
		}
		List<Integer> tar = Arrays.asList(8,3,7,9,1);
		alg.init(g);
		
		assertEquals(exp, alg.TSP(tar));
		
	}
	
	@Test
	void TSPRunTime() {
		final int size = 1000;
		DGraph g = new DGraph();
		List<Integer> targets = new ArrayList<Integer>();
		Point3D p = Point3D.ORIGIN;
		for (int i = 0; i < size; i++) {
			g.addNode(new DNode(i, p));
			if(Math.random()*3 < 1) // 1/3
				targets.add(i);
		}
		g.connect(size-1, 0, 50);
		for (int i = 0; i < size-1; i++) {
			g.connect(i, i+1, 50);
			
			for (int j = 0; j < Math.random()*size/5; j++) {
				int dest = (int)(Math.random()*size);
				if(dest != i)
					g.connect(i, dest, (int)(Math.random()*30+1));
			}
		}
		Date start = new Date();
		new Graph_Algo(g).TSP(targets);
		Date end = new Date();
		long time = (end.getTime()-start.getTime());
		if(time > 3000)
			fail("TPS shouldn't take mor then 3 seconds on a normal computer");
	}
	
	
}
