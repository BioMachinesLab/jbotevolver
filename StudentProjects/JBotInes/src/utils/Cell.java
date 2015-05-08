package utils;

public class Cell {

	private int x;
	private int y;
	private boolean visited;
	
	public Cell(int x, int y) {
		super();
		this.x = x;
		this.y = y;
		visited = false;
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

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Cell){
			Cell c = (Cell) obj;
			if(this.x == c.x && this.y == c.y)
				return true;
			else
				return false;
		} else
			return false;
	
	}
	
	@Override
	public String toString() {
		return "("+ x + "," + y + ")";
	}
}
