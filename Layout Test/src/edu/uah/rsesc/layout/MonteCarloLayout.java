package edu.uah.rsesc.layout;

import java.util.List;
import java.util.Random;

public class MonteCarloLayout 
{
	

	final int numberOfSamples; 
	private int shape_padding;
	private double target_line_length;
	private int intersection_OfShapes;
	private int intersection_OfConnections;
	int height_average = 0;
	int width_average = 0;
	double[] midpoint_Results = new double[2];
	public MonteCarloLayout(int numberOfSamples, int shape_padding, double target_connection_length) 
	{
		this.numberOfSamples = numberOfSamples;
		this.shape_padding = shape_padding;
		this.target_line_length = target_connection_length;
	}
	
	
	public void set_factorweight(int intersection_OfShapes, int intersection_OfConnections)
	{
		this.intersection_OfShapes = intersection_OfShapes;
		this.intersection_OfConnections = intersection_OfConnections;
	}
	
	public void layout(final List<Shape> shapes, final List<Connection> connections)
	{
		determine_diagramarea(shapes);
	
		final Random seedGenerator = new Random();
		int bestScore = Integer.MAX_VALUE;
		long bestSeed = 0;
		for(int i = 0; i < numberOfSamples; i++) {
			final long seed = seedGenerator.nextLong();
			final int score = layout(shapes, connections, new Random(seed));
			
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
		//layout(shapes, connections, new Random(3757796993979502431L));
		//System.out.println(bestSeed);
		//System.out.println(determinelineintersection());
		System.out.print(Integer.toString(bestScore));
	}
	
	/**
	 * Layouts the shapes, returns the score. Lower is better
	 * @param rand
	 * @return
	 */
	private int layout(final List<Shape> shapes, final List<Connection> connections, final Random rand) {
		/*
		 * Potential factors in score calculation:
		 * 	Length of lines
		 * 	Intersection between lines
		 * 	Size of diagram
		 *  Spacing between objects - too little is bad. Too much is bad as well
		 * Each factor should have a constant weight to allow fine tuning the algorithm.
		 */
		
		// Random X and Y for each Shape, within the range of calculated ideal_width and ideal_height
		for(final Shape shape : shapes) 
		{
			shape.setX(rand.nextInt((int) (width_average)));
			shape.setY(rand.nextInt((int) (height_average)));
		}

		int score = 0;
		score+=determine_shapeintersection(shapes, rand);
		score+=determine_lineintersection(connections);
		score+=determine_ideal_line_length(connections);
		return score;
	}
	
	int determine_shapeintersection(List <Shape> shapes, Random rand)
	{
		
		int score = 0;

		
		//Variables that will store the parameters of the shape in order to determine
		//if they are intersecting in the produced layout.
		
		int shape1_startx, shape1_endx, shape1_starty, shape1_endy;
		int shape2_startx, shape2_endx, shape2_starty, shape2_endy;
		
		/*
		* The following loop will run through the entire list of shapes to determine if they
		* are intersecting. It is built with a nested loop structure to prevent
		* repeated shapes to be compared. If there was a sample of 5 shapes, the loop's comparison
		* will resemble the following: 0->1, 0->2, 0->3, 0->4, 1->2, 1->3, 1->4, 2->3, 2->4, 3->4
		*/

		for(int outer_loopcounter=0; outer_loopcounter<(shapes.size()-1); outer_loopcounter++)
		{
			//Calculate the first comparison shape's parameters using the provided data
			//in the shape collection passed in. The padding specified in the constructor
			//call will be used to adjust the parameters.
			
			shape1_startx = shapes.get(outer_loopcounter).getX() - shape_padding;
			shape1_endx = shape1_startx + shapes.get(outer_loopcounter).getWidth() + shape_padding;
			shape1_starty = shapes.get(outer_loopcounter).getY() - shape_padding;
			shape1_endy= shape1_starty + (shapes.get(outer_loopcounter).getHeight()) + shape_padding;
			
			
			for(int inner_loopcounter = outer_loopcounter+1; inner_loopcounter<shapes.size(); inner_loopcounter++)
			{
				//Calculate the second comparison shape's parameters, same format as the first shape comparison.
				shape2_startx = shapes.get(inner_loopcounter).getX() - shape_padding;
				shape2_endx = shape2_startx + shapes.get(inner_loopcounter).getWidth() + shape_padding;
				shape2_starty =shapes.get(inner_loopcounter).getY() - shape_padding;
				shape2_endy= (shapes.get(inner_loopcounter).getY()) + (shapes.get(inner_loopcounter).getHeight()) + shape_padding;
				
				//The initial comparison is to determine if the x-axis range of the two shapes result in a possible
				//intersection. If this is true, the inner comparison then uses the y-axis range of the two shapes 
				//to do a final determination of an intersection. The weight factor of this occuring is in then
				//added to the total score of the layout.
				
				if(shape2_startx <= shape1_startx && shape2_endx > shape1_startx)
				{
					if(shape2_starty >= shape1_starty && shape2_starty < shape1_endy)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty < shape1_starty && shape2_endy > shape1_starty)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty == shape1_starty && shape2_endy == shape1_endy)
					{
						score += intersection_OfShapes;
					}
				}
				
				else if(shape2_startx >= shape1_startx && shape2_startx < shape1_endx)
				{
					if(shape2_starty >= shape1_starty && shape2_starty < shape1_endy)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty < shape1_starty && shape2_endy > shape1_starty)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty == shape1_starty && shape2_endy == shape1_endy)
					{
						score += intersection_OfShapes;
					}
					
				}
				
				else if(shape1_startx == shape2_startx && shape1_endx == shape2_endx)
				{
					if(shape2_starty >=  shape1_starty && shape2_starty < shape1_endy)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty < shape1_starty && shape2_endy > shape1_starty)
					{
						score += intersection_OfShapes;
					}
					
					else if(shape2_starty == shape1_starty && shape2_endy == shape1_endy)
					{
						score += intersection_OfShapes;
					}
					
				}
							
			}
		}
		
		return score;
	}

	
	int determine_lineintersection(List<Connection> connections)
	{
		/*The following will determine how many intersections are present between the Connections(excluding common endpoint/start points)
		and then adding the appropriate weight factor to the score. The algorithm sets up the line form AX + BY = C for each connection
		and then uses the determinant between the two compared lines to calculate the intersection.		
		*/
		
		//Initialization of variables used in the loop structure below.
		
		double determinant = 0, intersection_X = 0, intersection_Y = 0;
		double line1_A = 0, line2_A = 0, line1_B = 0, line2_B = 0, line1_C =0, line2_C = 0;
		double connection1_startx = 0, connection1_starty = 0, connection1_endx = 0, connection1_endy = 0;
		double connection2_startx = 0, connection2_starty = 0, connection2_endx = 0, connection2_endy = 0;
		int score = 0;
		/*
		* The following loop will run through the entire list of Connections to determine if they
		* are intersecting. It is built with a nested loop structure to prevent
		* repeated Connections to be compared. If there was a sample of 5 Connections, the loop's comparison
		* will resemble the following: 0->1, 0->2, 0->3, 0->4, 1->2, 1->3, 1->4, 2->3, 2->4, 3->4
		*/
		
		for(int outer_loopcounter=0; outer_loopcounter<(connections.size()-1); outer_loopcounter++)
		{
			
			//The center coordinates of each the two Shape objects related to a Connection are calculated. This
			//is needed because a Connection always starts and ends at a center point of a Shape
			
			//Determine the starting coordinates of the 1st Connection being compared.
			midpointcalculation(connections.get(outer_loopcounter).getSource());
			//Set the results to the appropriate variables.
			connection1_startx = midpoint_Results[0];
			connection1_starty = midpoint_Results[1];
			//Determine the ending coordinates of the 1st Connection being compared.
			midpointcalculation(connections.get(outer_loopcounter).getDestination());
			//Set the results to the appropriate variables
			connection1_endx = midpoint_Results[0];
			connection1_endy = midpoint_Results[1];
			//Construct the line equation of the 1st Connection in the form of AX + BY = C
			line1_A = connection1_endy - connection1_starty;
			line1_B = connection1_startx - connection1_endx;
			line1_C = (line1_A * connection1_startx) + (line1_B * connection1_starty);
			
			for(int inner_loopcounter = outer_loopcounter+1; inner_loopcounter<connections.size(); inner_loopcounter++)
			{
				//Same process as before to determine mid-point coordinates, except it is now for the 2nd Connection being compared.
				midpointcalculation(connections.get(inner_loopcounter).getSource());
				connection2_startx = midpoint_Results[0];
				connection2_starty = midpoint_Results[1];
				
				midpointcalculation(connections.get(inner_loopcounter).getDestination());
				connection2_endx = midpoint_Results[0];
				connection2_endy = midpoint_Results[1];
				
				//Construct the line equation of the 2nd Connection in the form of AX + BY = C;
				line2_A = connection2_endy - connection2_starty;
				line2_B = connection2_startx - connection2_endx;
				line2_C = (line2_A * connection2_startx) + (line2_B * connection2_starty);
				
				//Calculate the determinant of the two Connections.
				determinant = (line1_A*line2_B) - (line2_A*line1_B);
				
				//If the determinant is not zero, this means the lines are not parallel and therefore intersecting.
				if(determinant != 0)
				{
					//Determine the coordinates of the intersection. This is the intersection of the two line equations, NOT necessarily what's on the screen. 
					//A check is used to later to ensure that the intersection is visible.
					intersection_X = (line2_B*line1_C - line1_B * line2_C)/determinant;
					intersection_Y = (line1_A*line2_C - line2_A * line1_C)/determinant;
					
					//Confirm that the intersection point is not the start/end of a Connection
					if((intersection_X != connection1_startx && intersection_Y != connection1_starty) && (intersection_X !=  connection1_endx && intersection_Y != connection1_endy)
						&& (intersection_X != connection2_startx && intersection_Y != connection2_starty) && (intersection_X !=  connection2_endx && intersection_Y != connection2_endy))
					{
						//Confirm that the intersection point is within the range of the two compared Connections
						if((Math.min(connection1_startx,connection1_endx)<= intersection_X && intersection_X <=  Math.max(connection1_startx, connection1_endx)) && (Math.min(connection2_startx,connection2_endx)<= intersection_X && intersection_X <=  Math.max(connection2_startx, connection2_endx)) 
							&& (Math.min(connection1_starty, connection1_endy) <= intersection_Y && intersection_Y <= Math.max(connection1_starty, connection1_endy)) && (Math.min(connection2_starty, connection2_endy) <= intersection_Y && intersection_Y<= Math.max(connection2_starty, connection2_endy)))
						{
							//Add the weight factor for an intersection to the score
							score+=intersection_OfConnections;
						}
					}
					
				}
				
			}
		
		}
		return score;
	}
	
	
	void midpointcalculation(Shape shape1) 
	{
		//Determine the center of the Shape using the midpoint between two opposite corners of the Shape.
		int x1 = shape1.getX();
		int x2 = x1 + shape1.getWidth();
		int y1 = shape1.getY();
		int y2 = y1 + shape1.getHeight();
		
		double midpointx = (x1 + x2)/2;
		double midpointy = (y1 + y2)/2;
		
		//Store the results in a global private array that can be accessed by one of the functions
		midpoint_Results[0] = midpointx;
		midpoint_Results[1] = midpointy;
		
		
	}
	
	void determine_diagramarea(List <Shape> shapes)
	{
		int min_height=shapes.get(0).getHeight() + shape_padding;
		int max_height=min_height;
		int min_width=shapes.get(0).getWidth() + shape_padding;
		int max_width=min_width;
		int current_height;
		int current_width;
		int scaling = (int)(Math.sqrt(shapes.size()) * 2.5);
		for(int shape_counter =1; shape_counter<shapes.size(); shape_counter++)
		{
			current_height = shapes.get(shape_counter).getHeight() + shape_padding;
			current_width = shapes.get(shape_counter).getWidth() + shape_padding;
			if(current_height > max_height)
			{
				max_height = current_height;
			}
			
			else if(current_height < min_height)
			{
				min_height = current_height;
			}
			
			if(current_width > max_width)
			{
				max_width = current_width;
			}
			
			else if(current_width < min_width)
			{
				min_width = current_width;
			}
			
		}
		
		height_average = ((min_height + max_height)/2) * scaling;
		width_average = ((min_width + max_width)/2) *scaling;
		
	
	}
	
	int determine_ideal_line_length(final List<Connection> connections)
	{
		double average_length=0;
		int score=0;
		for(Connection connection: connections)
		{
			average_length+= Math.sqrt(Math.pow((connection.getDestination().getX() - connection.getSource().getX()), 2) + Math.pow((connection.getDestination().getY() - connection.getSource().getY()), 2));
		}
		average_length = average_length/connections.size();
		
		if(average_length != target_line_length)
		{
			score = (int)(Math.abs(target_line_length - average_length));
			System.out.println(score);
			return score;
		}
		
		return score;
		
	}
	
	
}
