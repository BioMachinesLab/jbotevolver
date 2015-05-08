package utils;

import java.io.Serializable;

public class Edge implements Serializable{
	private Vertex v1;
	private Vertex v2;

	public Edge(Vertex v1, Vertex v2){
		this.v1 = v1;
		this.v2 = v2;
	}

	public Vertex getV1() {
		return v1;
	}

	public Vertex getV2() {
		return v2;
	}

	public Vertex getNeighbour(Vertex v) {
		if(v1.equals(v))
			return v2;
		else
			return v1;

	}

	public boolean contains(Vertex v) {
		return (this.v1.equals(v) || this.v2.equals(v));
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() == Edge.class){
			Edge edge = (Edge) obj;
			return (edge.v1.equals(this.v1) && edge.v2.equals(this.v2));
		}
		return false;
	}
}
