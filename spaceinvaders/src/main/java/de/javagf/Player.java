
package de.javagf;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class Player extends Sprite {
	
	public int currentScore = 0; 
	Image bulletImage = new Image("file:images/bullet.png"); 
	
	private float movementSpeedY = 0.0f;
	private float movementSpeedX = 0.02f;
	
	public Player(Canvas canvas, float xPos, float yPos, float size, int currentLives, Image icon) {
		super(canvas, xPos, yPos, size, currentLives, icon);
		currentScore = 0; 
	}
	
	public int getCurrentScore() {
		return currentScore; 
	}
	
	public void incrementScore(int value) {
		currentScore += value; 
	}
	
	public void update() {
		move(); 
	}
	
	public void setMovementDirectionWithKeys(String code) {
		if (code.equals("A")) {
			setMovementDirection(-movementSpeedX, movementSpeedY); 
		}
		if (code.equals("D")) {
			setMovementDirection(movementSpeedX, movementSpeedY); 
		}
		
		// movement stops 
		if (code.equals("S")) {
			setMovementDirection(0.0f, 0.0f); 
		}
	}
	
	public Bullet shoot() {
		Bullet bullet = new Bullet(canvas, xPos + width / 2.0f - 0.03f / 2.0f, 
				yPos + 0.0f, 0.03f, 1, bulletImage); 
		
		bullet.setMovementDirection(0, -0.05f); 
		bullet.draw(); 
		return bullet; 
	}
	
	private void move() {

		this.xPos += deltaX; 
		if (this.xPos + width > 1.0f) {
			this.xPos = 1.0f - width; 
		}
		else if (this.xPos < 0.0f) {
			this.xPos = 0.0f; 
		}
		this.yPos += deltaY; 
		if (this.yPos + height > 1.0f) {
			this.yPos = 1.0f - height; 
		}
		else if (this.yPos < 0.0f) {
			this.yPos = 0.0f; 
		}
	}

}
