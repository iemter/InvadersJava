package de.javagf;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class Sprite {

	float width; 
	float height; 
	Canvas canvas;  
	Image icon; 
	float xPos; 
	float yPos; 
	// boolean shouldMove; 
	float deltaX; 
	float deltaY; 
	int currentLives; 
	String imagePath; 
	// this has UI Elements and contains reference to SpriteLogic 
	// because we want to resize everything here is set relative!
	// values between 0 and 1 relative to canvas size ... 
	public Sprite(Canvas canvas, float xPos, float yPos, float size, int currentLives) {
		this.canvas = canvas; 
		this.width = size; 
		this.height = size; 
		this.xPos = xPos; 
		this.yPos = yPos; 
		this.currentLives = currentLives; 
		
	}
	public Sprite(Canvas canvas,float xPos, float yPos, float size, int currentLives, Image icon) {
		this(canvas, xPos, yPos, size, currentLives); 
		this.icon = icon; 
		
	}
	
	public void setMovementDirection(float deltaX, float deltaY) {
		this.deltaX = deltaX; 
		this.deltaY = deltaY; 
	}
	
	public void draw() {
		// try reloading image 
		var gc = canvas.getGraphicsContext2D(); 
		gc.drawImage(icon, xPos * canvas.getWidth(), yPos * canvas.getHeight(), width * canvas.getWidth(), height * canvas.getWidth());
		
	}
	
	public float getXPos() {
		return xPos; 
	}
	
	public float getYPos() {
		return yPos; 
	}
	
	public float getWidth() {
		return width; 
	}
	public float getHeight() {
		return height; 
	}

	public int getLives() {
		return currentLives; 
	}
	
	public void setLives(int currentLives) {
		this.currentLives = currentLives; 
	}
	
	public Rectangle2D getBoundary() {
        return new Rectangle2D(xPos * canvas.getWidth(), yPos * canvas.getHeight(), width * canvas.getWidth() * 0.8, height * canvas.getWidth() * 0.8);
    }
	
	public boolean intersects(Sprite s) {
        return s.getBoundary().intersects(this.getBoundary());
    }
	
	public boolean checkCollision(Sprite s) {
		// need to check canvas height as if I don't sometimes stuff collides during the building of the canvas 
		if (intersects(s) && canvas.getHeight() > 0 && canvas.getWidth() > 0) {
			s.currentLives -= 1; 
			currentLives -= 1; 
			// System.out.println("collision detected"); 
			return true; 
		}
		return false; 
	}

}
