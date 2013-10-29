package edu.uah.rsesc.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class Main {
	public static void main(String[] args) {
		final int numberOfShapes = 20;
		final int numberOfConnections = 10;
		final int numberOfSamples = 10000;		
		
		// Create shapes
		final Random rand = new Random();
		//final Random rand = new Random(42); // Switch back to have different sizes on each run
		List<Shape> shapes = new ArrayList<Shape>(numberOfShapes);
		for(int i = 0; i < numberOfShapes; i++) {
			shapes.add(new Shape("S" + i, 0, 0, 30 + rand.nextInt(80), 30 + rand.nextInt(80)));
		}
		
		// Create connections between random components
		List<Connection> connections = new ArrayList<Connection>(numberOfConnections);
		for(int i = 0; i < numberOfConnections; i++) {
			boolean unique;
			Shape srcShape, dstShape;
			do {				
				// Determine the src and destination index
				final int srcShapeIdx = rand.nextInt(numberOfShapes);
				int dstShapeIdx;
				do {
					dstShapeIdx = rand.nextInt(numberOfShapes);
				} while(srcShapeIdx == dstShapeIdx);
				
				// Get the shapes
				srcShape = shapes.get(srcShapeIdx);
				dstShape = shapes.get(dstShapeIdx);
				
				// Check if a connections with those values or opposite values already exists
				unique = true;
				for(final Connection c : connections) {
					if((srcShape == c.getSource() && dstShape == c.getDestination()) || 
							(dstShape == c.getSource() && srcShape == c.getDestination())) {
						unique = false;
						break;
					}
				}				
			} while(!unique);
			
			// Create the connection
			connections.add(new Connection("C" + i, srcShape, dstShape));
		}

		// Layout the shapes
		final MonteCarloLayout layoutAlg = new MonteCarloLayout(numberOfSamples, 20, 150);
		layoutAlg.set_factorweight(20, 20);
		layoutAlg.layout(shapes, connections);
	
		show(shapes, connections);		
	}
	
	final static void show(final List<Shape> shapes, final List<Connection> connections) {
		// Create the objects for JGraph
		final mxGraph graph = new mxGraph();
		final Object parent = graph.getDefaultParent();
		
		graph.getModel().beginUpdate();
		try
		{
			final Map<Shape, Object> shapeToVertexMap = new HashMap<Shape, Object>();
			for(final Shape shape : shapes) {
				shapeToVertexMap.put(shape, graph.insertVertex(parent, null, shape.getLabel(), shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight()));
			}
			
			for(final Connection c : connections) {
				graph.insertEdge(parent, null, c.getLabel(), shapeToVertexMap.get(c.getSource()), shapeToVertexMap.get(c.getDestination()));
			}	
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		final mxGraphComponent graphComponent = new mxGraphComponent(graph);
		graphComponent.setEnabled(false);;
		
		// Create the frame
		final JFrame frame = new JFrame();
		frame.getContentPane().add(graphComponent);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 800);
		frame.setVisible(true);
	}

}
