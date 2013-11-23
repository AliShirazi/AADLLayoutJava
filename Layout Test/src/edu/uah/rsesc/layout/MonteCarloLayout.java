package edu.uah.rsesc.layout;

import java.util.List;
import java.util.Random;

public class MonteCarloLayout 
{
	//Class variables used throughout functions to determine ideal layout.
	private int numberOfSamples = 0; 
	private int shapePadding=0;
	private double targetConnectionLength=0;
	private int intersectionOfShapes=0;
	private int intersectionOfConnections=0;
	private int targetConnectionLengthFactor=0;
	private int maxHeight=0;
	private int maxWidth=0;
	private int diagramHeight = 0;
	private int diagramWidth = 0;
	private double[] midpointResults = new double[2];
	
	public MonteCarloLayout(int numberOfSamples, int shapePadding, double targetConnectionLength) 
	{
		this.numberOfSamples = numberOfSamples;
		this.shapePadding = shapePadding;
		this.targetConnectionLength = targetConnectionLength;
	}
	
	//Functions that set the weighted value for each factor that is calculated against the normalized results for each
	//category.
	public void setShapeIntersectionWeight(int intersectionOfShapes)
	{
		this.intersectionOfShapes = intersectionOfShapes;
	}
	public void setConnectionIntersectionWeight(int intersectionOfConnections)
	{
		this.intersectionOfConnections = intersectionOfConnections;
	}
	public void setTargetConnectionLengthFactor(int targetConnectionLengthFactor)
	{
		this.targetConnectionLengthFactor = targetConnectionLengthFactor;
	}
	
	public void layout(final List<Shape> shapes, final List<Connection> connections)
	{
		determineDiagramArea(shapes);		
		final Random seedGenerator = new Random();
		double bestScore = Integer.MAX_VALUE;
		long bestSeed = 0;
		for(int i = 0; i < numberOfSamples; i++) 
		{
			final long seed = seedGenerator.nextLong();
			final double score = layout(shapes, connections, new Random(seed));
			
			// Store the seed if it beat the last best score
			if(score < bestScore) 
			{
				bestScore = score;
				//the_best_score = score;
				bestSeed = seed;
			}
		}
		// Relayout using the best seed
		layout(shapes, connections, new Random(bestSeed));
	}
	
	/**
	 * Layouts the shapes, returns the score. Lower is better
	 * @param rand
	 * @return
	 */
	private double layout(final List<Shape> shapes, final List<Connection> connections, final Random rand) 
	{	
		//Total score for calculated layout.
		double score = 0; 
		
		// Assign X and Y for each Shape depending on PositionMode value 
		for(final Shape shape : shapes) 
		{
			switch(shape.getPositionMode())
			{
			case SNAP_LEFT:
				//Snap shape to left-side of diagram
				shape.setX(0);
				shape.setY(rand.nextInt((diagramHeight)));
				break;
			case SNAP_RIGHT:
				//Snap shape to right-side of diagram
				shape.setX(diagramWidth);
				shape.setY(rand.nextInt((diagramHeight)));
				break;
			case LOCKED:
				//Do Nothing
				break;
			case FREE:
				//Set X and Y within the range of calculated diagramWidth and diagramHeight
				shape.setX(rand.nextInt(diagramWidth));
				shape.setY(rand.nextInt(diagramHeight));
				break;
			}
		}	
		
		//The following factor functions are called and their individual scores are added to the overall score.
		score+=determineShapeIntersection(shapes, rand);
		score+=determineConnectionIntersection(connections);
		score+=determineTargetConnectionLengthDifference(connections);
		return score;
	}
	
	//Determines the number of Shape intersections in diagram
	private double determineShapeIntersection(List <Shape> shapes, Random rand)
	{
		//score will hold the normalized value * weight factor for Shape intersections.
		double score = 0;
		//totalShapeIntersections keeps count of the total Shape intersections that have been found
		//in the diagram
		double totalShapeIntersections=0;

		//Variables that will store the parameters of the shape in order to determine
		//if they are intersecting in the produced layout.
		int shape1StartX, shape1EndX, shape1StartY, shape1EndY;
		int shape2StartX, shape2EndX, shape2StartY, shape2EndY;
		
		/*
		* The following loop will run through the entire list of shapes to determine if they
		* are intersecting. It is built with a nested loop structure to prevent
		* repeated shapes to be compared. If there was a sample of 5 shapes, the loop's comparison
		* will resemble the following: 0->1, 0->2, 0->3, 0->4, 1->2, 1->3, 1->4, 2->3, 2->4, 3->4
		*/

		for(int outerLoopCounter=0; outerLoopCounter<(shapes.size()-1); outerLoopCounter++)
		{
			//Calculate the first comparison shape's parameters using the provided data
			//in the shape collection passed in. The padding specified in the constructor
			//call will be used to adjust the parameters.
			
			shape1StartX = shapes.get(outerLoopCounter).getX() - shapePadding;
			shape1EndX = shape1StartX + shapes.get(outerLoopCounter).getWidth() + shapePadding;
			shape1StartY = shapes.get(outerLoopCounter).getY() - shapePadding;
			shape1EndY= shape1StartY + (shapes.get(outerLoopCounter).getHeight()) + shapePadding;			
			for(int innerLoopCounter = outerLoopCounter+1; innerLoopCounter<shapes.size(); innerLoopCounter++)
			{
				//Calculate the second comparison shape's parameters, same format as the first shape comparison.
				shape2StartX = shapes.get(innerLoopCounter).getX() - shapePadding;
				shape2EndX = shape2StartX + shapes.get(innerLoopCounter).getWidth() + shapePadding;
				shape2StartY =shapes.get(innerLoopCounter).getY() - shapePadding;
				shape2EndY= (shapes.get(innerLoopCounter).getY()) + (shapes.get(innerLoopCounter).getHeight()) + shapePadding;
				
				//The initial comparison is to determine if the x-axis range of the two shapes result in a possible
				//intersection. If this is true, the inner comparison then uses the y-axis range of the two shapes 
				//to do a final determination of an intersection. The occurrence of the intersection is then 
				//added to the total instances found of shape intersections. This is later used to normalize 
				//the finding and apply its user-specified weight factor.
				
				if(shape2StartX <= shape1StartX && shape2EndX > shape1StartX)
				{
					if(shape2StartY >= shape1StartY && shape2StartY < shape1EndY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY < shape1StartY && shape2EndY > shape1StartY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY == shape1StartY && shape2EndY == shape1EndY)
					{
						totalShapeIntersections++;
					}
				}
				else if(shape2StartX >= shape1StartX && shape2StartX < shape1EndX)
				{
					if(shape2StartY >= shape1StartY && shape2StartY < shape1EndY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY < shape1StartY && shape2EndY > shape1StartY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY == shape1StartY && shape2EndY == shape1EndY)
					{
						totalShapeIntersections++;
					}	
				}
				else if(shape1StartX == shape2StartX && shape1EndX == shape2EndX)
				{
					if(shape2StartY >=  shape1StartY && shape2StartY < shape1EndY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY < shape1StartY && shape2EndY > shape1StartY)
					{
						totalShapeIntersections++;
					}
					else if(shape2StartY == shape1StartY && shape2EndY == shape1EndY)
					{
						totalShapeIntersections++;
					}
				}		
			}
		}
		
		//Normalize the total number of Shape intersections found by dividing it by the total UNIQUE Shape intersections that could have occurred.
		double normalizedValue = (totalShapeIntersections/determineMaxCollectionFactor(shapes));
		//Multiply the normalized value by the user-set weight for Shape intersections.
		score = normalizedValue * intersectionOfShapes;
		//Return score to calling function.
		return score;
	}

	//Determine total number of Connection intersections in diagram 
	private double determineConnectionIntersection(List<Connection> connections)
	{
		/*The following will determine how many intersections are present between the Connections(excluding common endpoint/start points)
		and then adding the appropriate weight factor to the score. The algorithm sets up the line form AX + BY = C for each connection
		and then uses the determinant between the two compared lines to calculate the intersection.		
		*/
		
		//Initialization of variables used in the loop structure below.
		double totalConnectionIntersections=0;
		double determinant = 0, intersectionX = 0, intersectionY = 0;
		double line1A = 0, line2A = 0, line1B = 0, line2B = 0, line1C =0, line2C = 0;
		double connection1StartX = 0, connection1StartY = 0, connection1EndX = 0, connection1EndY = 0;
		double connection2StartX = 0, connection2StartY = 0, connection2EndX = 0, connection2EndY = 0;
		//score will hold the normalized value * weight factor for Connection intersections.
		double score = 0;
		
		/*
		* The following loop will run through the entire list of Connections to determine if they
		* are intersecting. It is built with a nested loop structure to prevent
		* repeated Connections to be compared. If there was a sample of 5 Connections, the loop's comparison
		* will resemble the following: 0->1, 0->2, 0->3, 0->4, 1->2, 1->3, 1->4, 2->3, 2->4, 3->4
		*/
		for(int outerLoopCounter=0; outerLoopCounter<(connections.size()-1); outerLoopCounter++)
		{
			//The center coordinates of each the two Shape objects related to a Connection are calculated. This
			//is needed because a Connection always starts and ends at a center point of a Shape.
			
			//Determine the starting coordinates of the 1st Connection being compared.
			midpointCalculation(connections.get(outerLoopCounter).getSource());
			//Set the results to the appropriate variables.
			connection1StartX = midpointResults[0];
			connection1StartY = midpointResults[1];
			//Determine the ending coordinates of the 1st Connection being compared.
			midpointCalculation(connections.get(outerLoopCounter).getDestination());
			//Set the results to the appropriate variables
			connection1EndX = midpointResults[0];
			connection1EndY = midpointResults[1];
			//Construct the line equation of the 1st Connection in the form of AX + BY = C
			line1A = connection1EndY - connection1StartY;
			line1B = connection1StartX - connection1EndX;
			line1C = (line1A * connection1StartX) + (line1B * connection1StartY);
			
			for(int innerLoopCounter = outerLoopCounter+1; innerLoopCounter<connections.size(); innerLoopCounter++)
			{
				//Same process as before to determine mid-point coordinates, except it is now for the 2nd Connection being compared.
				midpointCalculation(connections.get(innerLoopCounter).getSource());
				connection2StartX = midpointResults[0];
				connection2StartY = midpointResults[1];
				
				midpointCalculation(connections.get(innerLoopCounter).getDestination());
				connection2EndX = midpointResults[0];
				connection2EndY = midpointResults[1];
				
				//Construct the line equation of the 2nd Connection in the form of AX + BY = C;
				line2A = connection2EndY - connection2StartY;
				line2B = connection2StartX - connection2EndX;
				line2C = (line2A * connection2StartX) + (line2B * connection2StartY);
				
				//Calculate the determinant of the two Connections.
				determinant = (line1A*line2B) - (line2A*line1B);
				
				//If the determinant is not zero, this means the lines are not parallel and therefore intersecting.
				if(determinant != 0)
				{
					//Determine the coordinates of the intersection. This is the intersection of the two line equations, NOT necessarily what's on the screen. 
					//A check is used to later to ensure that the intersection is visible.
					intersectionX = (line2B*line1C - line1B * line2C)/determinant;
					intersectionY = (line1A*line2C - line2A * line1C)/determinant;
					
					//Confirm that the intersection point is not the start/end of a Connection
					if((intersectionX != connection1StartX && intersectionY != connection1StartY) && (intersectionX !=  connection1EndX && intersectionY != connection1EndY)
						&& (intersectionX != connection2StartX && intersectionY != connection2StartY) && (intersectionX !=  connection2EndX && intersectionY != connection2EndY))
					{
						//Confirm that the intersection point is within the range of the two compared Connections
						if((Math.min(connection1StartX,connection1EndX)<= intersectionX && intersectionX <=  Math.max(connection1StartX, connection1EndX)) && (Math.min(connection2StartX,connection2EndX)<= intersectionX && intersectionX <=  Math.max(connection2StartX, connection2EndX)) 
							&& (Math.min(connection1StartY, connection1EndY) <= intersectionY && intersectionY <= Math.max(connection1StartY, connection1EndY)) && (Math.min(connection2StartY, connection2EndY) <= intersectionY && intersectionY<= Math.max(connection2StartY, connection2EndY)))
						{
							//Add the occurrence of a Connection intersection to the total found.
							totalConnectionIntersections++;
						}
					}	
				}	
			}		
		}
		
		//Normalize the total number of Connection intersections found by dividing it by the total UNIQUE Connection intersections that could have occurred.
		double normalizedValue = totalConnectionIntersections/determineMaxCollectionFactor(connections);
		//Multiply the normalized value by the user-set weight for Connection intersections.
		score = normalizedValue * intersectionOfConnections;
		//Return score to calling function.
		return score;
	}
	
	private void midpointCalculation(Shape shape1) 
	{
		//Determine the center of the Shape using the midpoint between two opposite corners of the Shape.
		int x1 = shape1.getX();
		int x2 = x1 + shape1.getWidth();
		int y1 = shape1.getY();
		int y2 = y1 + shape1.getHeight();
		
		double midpointX = (x1 + x2)/2;
		double midpointY = (y1 + y2)/2;
		
		//Store the results in a global private array that can be accessed by one of the functions
		midpointResults[0] = midpointX;
		midpointResults[1] = midpointY;
	}
	
	//Determine most efficient width and height parameters for total diagram.
	private void determineDiagramArea(List <Shape> shapes)
	{
		//The first step is to determine the min/max height and width in the shapes List that was passed in.
		//Initially the min/max height and width are set to the first Shape in the List.
		int minHeight=shapes.get(0).getHeight() + shapePadding;
		maxHeight=minHeight;
		int minWidth=shapes.get(0).getWidth() + shapePadding;
		maxWidth=minWidth;
		
		//The variables used in the loop to determine the current Shape's Height and Width
		int currentHeight;
		int currentWidth;
		
		//There is a level of scaling for the diagram's final width and height parameters. This is needed
		//to ensure the diagram does not grow inefficiently(i.e an addition of two shapes should not exponentially
		//increase the diagram area)
		int scaling = (int)(Math.sqrt(shapes.size()) * 2.5);
		
		//The following loop goes through each Shape in the List and determines if its height or
		//width are the new minimum or maximum.
		for(int shapeCounter =1; shapeCounter<shapes.size(); shapeCounter++)
		{
			currentHeight = shapes.get(shapeCounter).getHeight() + shapePadding;
			currentWidth = shapes.get(shapeCounter).getWidth() + shapePadding;
			if(currentHeight > maxHeight)
			{
				maxHeight = currentHeight;
			}
			else if(currentHeight < minHeight)
			{
				minHeight = currentHeight;
			}
			if(currentWidth > maxWidth)
			{
				maxWidth = currentWidth;
			}
			else if(currentWidth < minWidth)
			{
				minWidth = currentWidth;
			}
		}
		
		//Determine the final diagram width and height by calculating the mid value of the min/max height and width.
		//It is then multiplied by the scaling factor specified earlier.
		diagramHeight = ((minHeight + maxHeight)/2) * scaling;
		diagramWidth = ((minWidth + maxWidth)/2) *scaling;
	}
	
	//Determines the max number of UNIQUE occurrences for any factor that has a Collection.
	//The method used is summation to calculate this.
	<T> double  determineMaxCollectionFactor(List<T> theCollection)
	{
		double potential=0;
		for(int collectionCounter = theCollection.size()-1; collectionCounter>0; collectionCounter--)
		{
			potential+= collectionCounter;
		}
		return potential;
	}
	
	//Determines the average difference of all the Connections from the target Connection Length originally passed in
	//the constructor.
	private double determineTargetConnectionLengthDifference(final List<Connection> connections)
	{
		//Initialization of variables used to calculate average difference of Connections.
		double totalLineDifference=0;
		double idealMaxLineDifference;
		double lineLength;
		double diagonalLengthOfDiagram;
		double averageDifference;
		double normalizedValue;
		
		//score will hold the normalized value * weight factor for the Connection Length difference.
		double score=0;
		
		//Determine the total Connection Length difference of all the Connections from the target Connection length.
		for(Connection connection: connections)
		{
			lineLength = Math.sqrt(Math.pow((connection.getDestination().getX() - connection.getSource().getX()), 2) + Math.pow((connection.getDestination().getY() - connection.getSource().getY()), 2));
		    totalLineDifference += Math.abs(targetConnectionLength - lineLength);
		}
		
		//Calculate the average Connection Length difference.
		averageDifference = totalLineDifference/connections.size();
		//Determine the longest Connection Length possible for diagram(the diagonal of the diagram)
		diagonalLengthOfDiagram = Math.sqrt(Math.pow(maxWidth, 2) + Math.pow(maxHeight, 2));
		//Calculate the maximum Connection Length difference average using the diagonalLengthOfDiagram value.
		idealMaxLineDifference = diagonalLengthOfDiagram - targetConnectionLength;
		//Normalize the averageDifference calculated by dividing by idealMaxLineDifference.
		normalizedValue = averageDifference/idealMaxLineDifference;
		//Multiply the normalized value by the weight for the targetConnectionLength factor.
		score = normalizedValue * targetConnectionLengthFactor;
		//Return score to the calling function.
		return score;
	}
}
