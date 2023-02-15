
package de.javagf;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class Bullet extends Sprite {


	public Bullet(Canvas canvas, float xPos, float yPos, float size, int currentLives,
			Image icon) {
		super(canvas, xPos, yPos, size, currentLives, icon);
		
	}
	
	
	public void update() {
		if (currentLives > 0) {
			move(); 
		}
	}
	
	// bullet can only move until canvas boundary is reached
	private void move() {
		this.xPos += deltaX; 
		this.yPos += deltaY; 
		if (this.xPos + width > 1.0f || this.xPos < 0.0f || this.yPos + height > 1.0f || this.yPos < 0.0f) {
			currentLives = 0; 
		}
	}
	
}
