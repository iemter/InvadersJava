package de.javagf;

public enum EnemyType {
	
	NORMAL(10), // just exists 
	SPECIAL(25), // shoots bulets 
	RUSH(15); // rushing forward
	
	final int points; 
	EnemyType(int points) {
		this.points = points; 
	}
	
	public int getPoints() {
		return this.points; 
	}
	
}