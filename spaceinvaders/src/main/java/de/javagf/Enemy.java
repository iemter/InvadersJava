
package de.javagf;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

public class Enemy extends Sprite{
	
	EnemyType type; 
	Image enemyBulletImage = new Image("file:images/enemyBullet.png"); 
	Random random; 
	float shootingProbability; 
	ArrayList<Bullet> shotBullets = new ArrayList<Bullet>(); 

	public Enemy(Canvas canvas, float xPos, float yPos, float size, int currentLives, Image icon, EnemyType type) {
		super(canvas, xPos, yPos, size, currentLives, icon);
		this.type = type; 
		
		if (type == EnemyType.RUSH) {
			setMovementDirection(0f, 0.01f); // moves straight down 
		}
		else {
			setMovementDirection(0.005f, 0.008f); 
			// movement from left to right till limit, then down, then from right to left, then down, ...
		}
		random = new Random(); 
	}
	
	public void setXSpeed(float deltaX) {
		this.deltaX = deltaX; 
	}
	
	public void setYSpeed(float deltaY) {
		this.deltaY = deltaY; 
	}
	
	// percentage between 0.0f and 1.0f where 1.0f is 100% of the time (which is a bad setting, trust me) 
	public void setShootingProbability(float percentage) {
		shootingProbability = percentage; 
	}
	
	public EnemyType getType() {
		return type; 
	}

	// rush aliens just move straight down 
	@Override
	public void draw() {
		if (currentLives > 0) {
			var gc = canvas.getGraphicsContext2D(); 
			gc.drawImage(icon, xPos * canvas.getWidth(), yPos * canvas.getHeight(), width * canvas.getWidth(), height * canvas.getWidth());
		
//			for (var bullet: shotBullets) {
//				bullet.draw();
//			}
		}
	}
	
	public void update() {
		if (currentLives > 0) {
			move(); 
			if (type == EnemyType.SPECIAL) {
				// shoot bullet if right enemy type and still alive 
				float value = random.nextFloat(); 
				if (value < shootingProbability) {
					shoot(); 
				}
				
				// filter only "alive" bullets 
				shotBullets = (ArrayList<Bullet>) shotBullets.stream().filter(x -> x.getLives() > 0).collect(Collectors.toList()); 	
			}
		}
		else {
			// System.out.println("enemy dead!"); 
			shotBullets.clear();
			
		}
		
	}
	
	public ArrayList<Bullet> getShotBullets() {
		shotBullets = (ArrayList<Bullet>) shotBullets.stream().filter(x -> x.getLives() > 0).collect(Collectors.toList()); 
		return shotBullets; 
	}
	
	private void shoot() {
		Bullet bullet = new Bullet(canvas, xPos + width / 2.0f - 0.03f / 2.0f, 
				yPos + 0.05f, 0.02f, 1, enemyBulletImage); 
		
		bullet.setMovementDirection(0, 0.03f); 
		bullet.draw(); 
		shotBullets.add(bullet); 
	}
	
	private void move() {
		if (type != EnemyType.RUSH) {
			this.xPos += deltaX; 
			
			if (this.xPos + deltaX + width > 1.0f || this.xPos + deltaX < 0.0f) { 
				// move down 
				deltaX = -deltaX; // switch direction
				yPos += (deltaY + width); // move down a little bit 
			}
			else {
				xPos += deltaX; // move normally (left / right) 
			}
			
			if (this.yPos + height > 1.0f || this.yPos < 0.0f) {
				// cannot move anymore ... 
				// that means death lol
				currentLives = 0; 
			}
		}
		
		else { // rush enemies go down just like bullets 
			this.xPos += deltaX; 
			this.yPos += deltaY; 
			if (this.xPos + width > 1.0f || this.xPos < 0.0f || this.yPos + height > 1.0f || this.yPos < 0.0f) {
				currentLives = 0; 
			}
		}
	}

}
