package de.javagf;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.beans.property.*;

/*
 * This class contains all general game logic components i.e. spawning of enemies, etc 
 */
public class GameLogic {

	// Icon stuff
	// save loaded images instead of strings because that means loding less images
	// but instead reusing the ones I need more than once
	// might be easier on ram?
	Image[] normalEnemyIcons = new Image[] { new Image("file:images/alien1.png"), new Image("file:images/alien2.png"),
			new Image("file:images/alien3.png"), new Image("file:images/alien4.png"),
			new Image("file:images/alien5.png"), new Image("file:images/alien6.png"),
			new Image("file:images/alien7.png") };
	Image[] specialEnemyIcons = new Image[] { new Image("file:images/alien31.png"),
			new Image("file:images/alien32.png"), new Image("file:images/alien33.png"),
			new Image("file:images/alien34.png") };
	Image[] rushEnemyIcons = new Image[] { new Image("file:images/alien21.png"), new Image("file:images/alien22.png"),
			new Image("file:images/alien23.png") };

	Image backgroundImage = new Image("file:images/background.png");
	Image livesImage = new Image("file:images/PlayerLive.png");
	Image playerImage = new Image("file:images/Player.png");

	// Sound
	private MediaPlayer playerMediaPlayer;
	private MediaPlayer enemyMediaPlayer;

	// sprite management
	Player player;

	Bullet playerBullet;
	ArrayList<Bullet> enemyBullets = new ArrayList<Bullet>();
	ArrayList<Enemy> enemies = new ArrayList<Enemy>();

	// Canvas
	Canvas canvas;

	// logic variables (for example modifyable with settings
	int numberOfEnemiesPerSpawn = 10;
	long enemyRespawnInterval = 20; // in seconds
	int initialPlayerLives = 5;

	float rushEnemyProbability = 0.1f;
	float specialEnemyProbability = 0.2f;
	float enemyShootPropability = 0.003f;
	// rest is normal enemies

	// variables for game Pause
	private boolean gamePaused = false;
	public BooleanProperty gameOverProperty = new SimpleBooleanProperty(false);

	// other variables needed for calculation and such shit
	int currentlySpawnedEnemies = 0;
	float relativeEnemySize = 0.05f;
	Random random;

	// stuff I only need once even when restarting
	EventHandler<KeyEvent> keyEventHandler;
	Timeline timeline;
	AnimationTimer animationTimer;
	GraphicsContext gc;

	public GameLogic(Canvas canvas) {
		this.canvas = canvas;
		player = new Player(canvas, 0.5f, 0.8f, 0.1f, initialPlayerLives, playerImage);
		random = new Random();
		// spawnEnemies();
		gc = canvas.getGraphicsContext2D();

		File playerMediaFile = new File("sounds/lowImpactwav.mp3");
		playerMediaPlayer = new MediaPlayer(new Media(playerMediaFile.toURI().toString()));

		File enemyMediaFile = new File("sounds/pop.mp3");
		enemyMediaPlayer = new MediaPlayer(new Media(enemyMediaFile.toURI().toString()));
	}

	public GameLogic(Canvas canvas, int initialPlayerLives) {
		this(canvas);
		this.initialPlayerLives = initialPlayerLives;
		player = new Player(canvas, 0.5f, 0.8f, 0.1f, initialPlayerLives, playerImage);
	}

	public Timeline getTimeline() {
		if (timeline == null) {
			timeline = new Timeline(new KeyFrame(Duration.seconds(enemyRespawnInterval), e -> spawnEnemies()));
			// repeat for infinity (for now, later that would be the game duration!
			timeline.setCycleCount(Animation.INDEFINITE);
		}

		return timeline;
	}

	public void spawnEnemies() {

		for (int i = 0; i < numberOfEnemiesPerSpawn; ++i) {
			Enemy tmp;

			float randomValue = random.nextFloat();
			if (randomValue < rushEnemyProbability) {
				tmp = new Enemy(canvas, 0.1f + i * 0.06f, 0.2f, relativeEnemySize, 1,
						rushEnemyIcons[random.nextInt(rushEnemyIcons.length)], EnemyType.RUSH);
			} else if (randomValue < specialEnemyProbability + rushEnemyProbability) {
				tmp = new Enemy(canvas, 0.1f + i * 0.06f, 0.2f, relativeEnemySize, 1,
						specialEnemyIcons[random.nextInt(specialEnemyIcons.length)], EnemyType.SPECIAL);
				tmp.setShootingProbability(0.003f);
			} else {
				tmp = new Enemy(canvas, 0.1f + i * 0.06f, 0.2f, relativeEnemySize, 1,
						normalEnemyIcons[random.nextInt(normalEnemyIcons.length)], EnemyType.NORMAL);
			}
			enemies.add(tmp);
		}
	}

	public AnimationTimer getAnimationTimer() {
		if (animationTimer == null) {
			animationTimer = new AnimationTimer() {
				private long lastUpdate = 0;

				@Override
				public void handle(long now) {
					double t = (now - lastUpdate) / 1000000000.0; // time in seconds
					// force 25 fps -> update once every 1 / 25 = 0.04 seconds
					if (t >= 0.04) {
						lastUpdate = now;
						updateGameState();
					}
				}

			};
		}
		return animationTimer;
	}

	public void redrawBaseGame() {
		// clear canvas
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// draw background
		gc.drawImage(backgroundImage, 0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

		if (player != null) {
			if (player.getLives() <= 5) {
				for (int i = 0; i < player.getLives(); ++i) {
					gc.drawImage(livesImage, (0.02 + 0.05 * i) * canvas.getWidth(), 0.05 * canvas.getHeight(),
							canvas.getWidth() * 0.04, canvas.getWidth() * 0.04);
				}
			} else {
				gc.drawImage(livesImage, 0.02 * canvas.getWidth(), 0.02 * canvas.getWidth(), canvas.getWidth() * 0.04,
						canvas.getWidth() * 0.04);
				gc.setStroke(Color.WHITE);
				gc.setFill(Color.WHITE);
				gc.setFont(new Font(0.05 * canvas.getHeight()));
				gc.strokeText("x " + Integer.valueOf(player.getLives()).toString(), 0.07 * canvas.getWidth(),
						0.05 * canvas.getWidth());

			}

			gc.setStroke(Color.WHITE);
			gc.setFill(Color.WHITE);
			gc.setFont(new Font(0.04 * canvas.getHeight()));
			gc.strokeText("Current score: " + Integer.valueOf(player.getCurrentScore()).toString(),
					0.55 * canvas.getWidth(), canvas.getHeight() * 0.1, 0.4 * canvas.getWidth());

		}
	}

	public void redrawCanvasNoMove() {

		redrawBaseGame();

		// only one bullet of player visible
		if (playerBullet != null) {
			if (playerBullet.getLives() > 0) {
				playerBullet.draw();
			} else {
				playerBullet = null;
			}
		}

		for (var enemy : enemies) {
			enemy.draw();
		}
		for (var enemyBullet : enemyBullets) {
			enemyBullet.draw();
		}

		if (player != null) {
			if (player.getLives() > 0) {
				player.draw();
			}
		}
	}

	public void updateGameState() {

		redrawBaseGame();

		// only one bullet of player visible
		if (playerBullet != null) {
			if (playerBullet.getLives() > 0) {
				playerBullet.update();
				for (var enemy : enemies) {
					boolean colliding = playerBullet.checkCollision(enemy);
					if (colliding) {
						player.incrementScore(enemy.getType().getPoints());
						enemyMediaPlayer.seek(Duration.ZERO);
						enemyMediaPlayer.play();
					}
				}
			}
			// needs to be seperate as checkCollision mit change life so I need to check
			// again
			if (playerBullet.getLives() > 0) {
				playerBullet.draw();
			} else {
				playerBullet = null;
			}
		}

		enemyBullets.clear(); // clear enemy bullet list in order to update it
		for (var enemy : enemies) {
			enemy.update();
			enemy.draw();

			// update enemy bullets list
			enemyBullets.addAll(enemy.shotBullets);

			// check enemy collision with player
			// because enemies move now and can directly harm the player
			enemy.checkCollision(player);
		}

		enemies = (ArrayList<Enemy>) enemies.stream().filter(x -> x.getLives() > 0).collect(Collectors.toList());

		if (player != null) {
			// check if enemybullets are hitting player
			for (var bullet : enemyBullets) {
				bullet.update();
				bullet.draw();
				bullet.checkCollision(player);
			}
			if (player.getLives() > 0) {
				player.update();
				player.draw();
			} else {
				playerMediaPlayer.seek(Duration.ZERO);
				playerMediaPlayer.play();
				playerBullet = null;
				gameOverProperty.set(true);
			}
		}

		enemyBullets = (ArrayList<Bullet>) enemyBullets.stream().filter(x -> x.getLives() > 0)
				.collect(Collectors.toList());
	}

	// make sure there is only one
	public EventHandler<KeyEvent> getKeyPressEventHandler() {
		if (keyEventHandler == null) {
			keyEventHandler = new EventHandler<KeyEvent>() {
				public void handle(KeyEvent e) {
					String code = e.getCode().toString();

					// set movement direction based on key pressed
					// a = left
					// s = stop
					// d = right
					player.setMovementDirectionWithKeys(code);

					// shoot if space is pressed
//					if (code.equals("SPACE")) {
//						
//					}
				}
			};
		}
		return keyEventHandler;
	}

	public void Reload() {

		playerBullet = null;
		player = new Player(canvas, 0.5f, 0.8f, 0.1f, initialPlayerLives, playerImage);
		enemies.clear();
		enemyBullets.clear();
		player.setLives(initialPlayerLives);
		gameOverProperty.set(false);

		spawnEnemies();
		timeline.jumpTo(Duration.ZERO);

		unPause();

	}

	public void pause() {
		animationTimer.stop();
	}

	public void unPause() {
		animationTimer.start();
	}

	public boolean getGamePaused() {
		return gamePaused;
	}

	public void gameOver() {
		// updateGameState(); // redraw canvas once
		redrawCanvasNoMove();
		animationTimer.stop();
	}

	public Player getPlayer() {
		return player;
	}

	public void playerShoot() {
		// Einkommentieren um erst wieder schießen zu lassen, wenn Bullet trifft oder
		// Bildschirm verlässt
		// if(playerBullet == null || playerBullet.getYPos() < 0) {
		playerBullet = player.shoot();
		// }

	}

	// more getters and setters that might be usefull

	public void setMediaPlayerVolume(double volume) {
		this.playerMediaPlayer.setVolume(volume);
		this.enemyMediaPlayer.setVolume(volume);
	}

	public int getNumberOfEnemiesPerSpawn() {
		return numberOfEnemiesPerSpawn;
	}

	public void setNumberOfEnemiesPerSpawn(int numberOfEnemiesPerSpawn) {
		this.numberOfEnemiesPerSpawn = numberOfEnemiesPerSpawn;
	}

	public long getEnemyRespawnInterval() {
		return enemyRespawnInterval;
	}

	public void setEnemyRespawnInterval(long enemyRespawnInterval) {
		this.enemyRespawnInterval = enemyRespawnInterval;
		// create new Timeline with new enemy resapwn interval;

		timeline = null;
		timeline = getTimeline();

	}

	public int getInitialPlayerLives() {
		return initialPlayerLives;
	}

	public void setInitialPlayerLives(int initialPlayerLives) {
		this.initialPlayerLives = initialPlayerLives;
	}

	public float getRushEnemyProbability() {
		return rushEnemyProbability;
	}

	public void setRushEnemyProbability(float rushEnemyProbability) {
		this.rushEnemyProbability = rushEnemyProbability;
	}

	public float getSpecialEnemyProbability() {
		return specialEnemyProbability;
	}

	public void setSpecialEnemyProbability(float specialEnemyProbability) {
		this.specialEnemyProbability = specialEnemyProbability;
	}

	public float getRelativeEnemySize() {
		return relativeEnemySize;
	}

	public void setRelativeEnemySize(float relativeEnemySize) {
		this.relativeEnemySize = relativeEnemySize;
	}

	public void setEnemyShootProbability(float enemyShootProbability) {
		this.enemyShootPropability = enemyShootProbability;
	}

}