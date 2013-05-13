package br.edu.utfpr.cm.scienceevol;

public class Dupla<X, Y> {
	private X x;
	private Y y;

	public Dupla(X x, Y y) {
		put(x,y);
	}
	
	public void put(X x, Y y){
		this.x = x;
		this.y = y;
	}
	
	public X getX(){
		return this.x;
	}

	public Y getY(){
		return this.y;
	}
	
}
