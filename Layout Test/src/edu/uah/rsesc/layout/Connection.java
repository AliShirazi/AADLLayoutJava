package edu.uah.rsesc.layout;

public class Connection {
	private final String label;
	private final Shape src;
	private final Shape dest;
	
	public Connection(final String label, final Shape src, final Shape dest) {
		this.label = label;
		this.src = src;
		this.dest = dest;
	}

	public String getLabel() {
		return label;
	}
	
	public Shape getSource() {
		return src;
	}

	public Shape getDestination() {
		return dest;
	}
}
