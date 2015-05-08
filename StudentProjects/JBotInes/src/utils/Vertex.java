package utils;

import java.io.Serializable;
import java.util.LinkedList;


public class Vertex implements Serializable{

	private int value;
	private boolean closed;
	private boolean hasEdges;
	private LinkedList<Edge> edges = new LinkedList<Edge>();

	public Vertex(int value) {
		this.value = value;
		closed = false;
		hasEdges = false;
	}

	public LinkedList<Edge> getEdges() {
		return edges;
	}

	public int getValue() {
		return value;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		if(!closed)
			this.closed = closed;
	}

	public void setEdges(LinkedList<Edge> edges) {
		this.edges = edges;
		if(edges.isEmpty())
			this.hasEdges = false;
		else
			this.hasEdges = true;
	}

	public boolean hasEdges() {
		return this.hasEdges;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() == Vertex.class){
			Vertex v = (Vertex) obj;
			return (this.value == v.value);
		}
		return false;
	}
}
