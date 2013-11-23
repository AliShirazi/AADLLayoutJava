package edu.uah.rsesc.layout;

public class Shape {
	private String label;
	private int x;
	private int y;
	private int width;
	private int height;
	private PositionMode positionMode;
	
	public enum PositionMode 
	{
		SNAP_LEFT, SNAP_RIGHT, LOCKED, FREE
	}
	
	public Shape(final String label, final int x, final int y, final int width, final int height, final PositionMode positionMode) 
	{
		this.label = label;
		this.setX(x);
		this.setY(y);
		this.width = width;
		this.height = height;
		this.positionMode = positionMode;
	}

	public PositionMode getPositionMode()
	{
		return positionMode;
	}
	public String getLabel() {
		return label;
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
