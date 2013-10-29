package edu.uah.rsesc.layout;

public class Shape {
	private String label;
	private int x;
	private int y;
	private int width;
	private int height;
	
	public Shape(final String label, final int x, final int y, final int width, final int height) {
		this.label = label;
		this.setX(x);
		this.setY(y);
		this.width = width;
		this.height = height;
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
