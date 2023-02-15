package de.javagf;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;

public class ViewerEngine {

	private Stage primaryStage;

	double windowMinWidth = 600.0;
	double windowMinHeight = 400.0;

	DoubleProperty currentSceneWidth = new SimpleDoubleProperty(0.0);
	DoubleProperty currentSceneHeight = new SimpleDoubleProperty(0.0);
	DoubleProperty currentStageX = new SimpleDoubleProperty(0.0);
	DoubleProperty currentStageY = new SimpleDoubleProperty(0.0);

	GameLogic logicComponent; // holds most of the general logic required for gameplay
	Canvas canvas; // hold a reference to canvas here as we need it

	Scene settingScene;
	Scene gameScene;
	Scene startScene;

	VBox gameRoot;
	VBox startRoot;
	VBox settingRoot;

	Group buttonsInGame;

	Random random = new Random();

	double decorationOffsetY = 0.0;
	double decorationOffsetX = 0.0;

	private Media media;
	private MediaPlayer mediaPlayer;
	private double mediaPlayerVolume = 0.1;
	private int songIndex;
	private ArrayList<File> songs;

	boolean gameIsRunning = false;

	public ViewerEngine(Stage primaryStage) {

		this.primaryStage = primaryStage;

		setupMediaPlayer();

		startRoot = BuildStartRoot();
		gameRoot = BuildGameRoot();
		settingRoot = BuildSettingRoot();

		// build scenes once
		startScene = new Scene(startRoot);
		gameScene = new Scene(gameRoot);
		// this is the dummest bugfix ever but ok
		// BUG: autofocused keys get pressed which results in a return to title screen
		// instead of shooting
		gameScene.addEventFilter(KeyEvent.KEY_PRESSED, k -> {
			if (k.getCode() == KeyCode.SPACE) {
				logicComponent.playerShoot();
				k.consume();
			}
			if (k.getCode() == KeyCode.ESCAPE) {
				gameRoot.setEffect(new GaussianBlur());
				logicComponent.pause();
				setupPauseMenu(gameRoot);
				k.consume();
			}
		});

		settingScene = new Scene(settingRoot);

		settingScene.heightProperty().addListener((obs, oldVal, newVal) -> {
			// don't scale background, only elements in controllGroup
			settingRoot.getChildren().get(0).setScaleY(newVal.doubleValue() / windowMinHeight);
		});
		settingScene.widthProperty().addListener((obs, oldVal, newVal) -> {
			settingRoot.getChildren().get(0).setScaleX(newVal.doubleValue() / windowMinWidth);
		});

		startScene.heightProperty().addListener((obs, oldVal, newVal) -> {
			// don't scale background, only elements in controllGroup
			startRoot.getChildren().get(0).setScaleY(newVal.doubleValue() / windowMinHeight);
		});
		startScene.widthProperty().addListener((obs, oldVal, newVal) -> {
			startRoot.getChildren().get(0).setScaleX(newVal.doubleValue() / windowMinWidth);
		});

		// to resize scenes?
		primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
			// the decorationstyle of the window results in an offset between stage height
			// and scene height
			// this offset needs to be calculated else the window keeps getting larger
			if (primaryStage.getScene() == startScene) {
				decorationOffsetY = startScene.getY();
			} else if (primaryStage.getScene() == gameScene) {
				decorationOffsetY = gameScene.getY();
			} else if (primaryStage.getScene() == settingScene) {
				decorationOffsetY = settingScene.getY();
			}
			currentSceneHeight.set(primaryStage.getHeight() - decorationOffsetY);

		});
		primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
			if (primaryStage.getScene() == startScene) {
				decorationOffsetX = startScene.getX();
			} else if (primaryStage.getScene() == gameScene) {
				decorationOffsetX = gameScene.getX();
			} else if (primaryStage.getScene() == settingScene) {
				decorationOffsetX = settingScene.getX();
			}

			currentSceneWidth.set(primaryStage.getWidth() - decorationOffsetX);
		});

		primaryStage.xProperty().addListener((obs, oldVal, newVal) -> {
			currentStageX.set(newVal.doubleValue());
		});

		primaryStage.yProperty().addListener((obs, oldVal, newVal) -> {
			currentStageY.set(newVal.doubleValue());
		});
	}

	// Sets MediaPlayer Up
	private void setupMediaPlayer() {
		// Media Player for music
		File directory;
		File[] files;

		// Sucht alle Files aus dem Ordner "music" und fügt sie ArrayList songs hinzu
		songs = new ArrayList<File>();
		directory = new File("music");
		files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				songs.add(file);
			}
		}

		songIndex = songs.size();
		resetMediaPlayer();

	}

	// resets MediaPlayer to controll Music
	private void resetMediaPlayer() {

		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}

		// Sucht ein zufälliges nächstes Lied, welches nicht das zuvorige Lied war
		// In dem Fall wird das vorherigen Lied durch songIndex ausgeschlossen
		int songIndexRandom;
		do {
			songIndexRandom = random.nextInt(0, songs.size() - 1);
		} while (songIndexRandom == songIndex || songs.size() == 1);

		songIndex = songIndexRandom;
		media = new Media(songs.get(songIndex).toURI().toString());
		mediaPlayer = new MediaPlayer(media);

		mediaPlayer.seek(Duration.ZERO);
		mediaPlayer.setVolume(mediaPlayerVolume);
		mediaPlayer.play();

		mediaPlayer.setOnEndOfMedia(new Runnable() {
			public void run() {
				resetMediaPlayer();
			}
		});
	}

	// Loads Start Menu scene
	public void LoadMenu() {
		// use old scene to set root size ...
		Scene oldScene = primaryStage.getScene();
		if (oldScene != null) {
			startRoot.setPrefSize(oldScene.getWidth(), oldScene.getHeight());
		}
		primaryStage.setScene(startScene);

		// primaryStage.show();
		gameIsRunning = false;
	}

	// loads game scene
	public void LoadGame() {

		Scene oldScene = primaryStage.getScene();
		if (oldScene != null) {
			gameRoot.setPrefSize(oldScene.getWidth(), oldScene.getHeight());
		}
		primaryStage.setScene(gameScene);

		gameScene.setOnKeyPressed(logicComponent.getKeyPressEventHandler());
		logicComponent.getAnimationTimer().start();

		var timeline = logicComponent.getTimeline(); // for spawning more enemies
		timeline.playFromStart();

		gameIsRunning = true;
	}

	// loads setting scene
	public void LoadSettings() {
		Scene oldScene = primaryStage.getScene();
		if (oldScene != null) {
			settingRoot.setPrefSize(oldScene.getWidth(), oldScene.getHeight());
		}
		settingScene.getRoot().requestFocus();
		primaryStage.setScene(settingScene);
	}

	// ------------------------------------------ build scene functions
	// ------------------------------------------------------
	public VBox BuildStartRoot() {
		var root = GetStartLayout();
		return root;
	}

	public VBox BuildSettingRoot() {
		VBox mainBox = setupStartMenu();
		mainBox.setAlignment(Pos.CENTER);
		mainBox.setSpacing(5);
		BackgroundImage backgroundImage = new BackgroundImage(new Image("file:images/background.png"),
				BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
				new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
		BackgroundImage backgroundAliens = new BackgroundImage(new Image("file:images/MenuAliens.gif"),
				BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
				new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, false));
		mainBox.setBackground(new Background(backgroundImage, backgroundAliens));

		Label lblSettings = new Label("Settings");
		lblSettings.setTextFill(Color.color(1, 1, 1));
		lblSettings.setFont(new Font(50.0));

		VBox.setVgrow(mainBox, Priority.ALWAYS);

		// make settings use same initial values as logicComponent ... Yeah I forgot
		// about that one, thanks:D
		Slider sldrPlayerHealth;
		if (logicComponent != null) {
			sldrPlayerHealth = new Slider(1, 20, logicComponent.initialPlayerLives);
		} else {
			sldrPlayerHealth = new Slider(1, 20, 5);
		}
		sldrPlayerHealth.setShowTickLabels(true);
		sldrPlayerHealth.setMajorTickUnit(5); // one is too large and looks stupid
		sldrPlayerHealth.valueProperty().addListener((obs, old, newValue) -> {
			logicComponent.setInitialPlayerLives(newValue.intValue());
		});

		Label lblPlayerHeath = new Label("Player Health: ");
		lblPlayerHeath.setTextFill(Color.color(1, 1, 1));
		lblPlayerHeath.setFont(new Font(20.0));

		VBox playerHealthBox = new VBox();
		playerHealthBox.maxWidth(windowMinWidth);
		playerHealthBox.setAlignment(Pos.CENTER);
		playerHealthBox.getChildren().addAll(lblPlayerHeath, sldrPlayerHealth);

		// Slider und Label für Respawntime
		Slider sldrRespawnTime;
		if (logicComponent != null) {
			sldrRespawnTime = new Slider(3, 50, logicComponent.enemyRespawnInterval);
		} else {
			sldrRespawnTime = new Slider(3, 50, 20);
		}
		sldrRespawnTime.setShowTickLabels(true);
		sldrRespawnTime.setShowTickMarks(true);
		sldrRespawnTime.valueProperty().addListener((obs, old, newValue) -> {
			logicComponent.setEnemyRespawnInterval(newValue.longValue());

		});

		Label lblRespawnTime = new Label("Enemy Respawn Time: ");
		lblRespawnTime.setTextFill(Color.color(1, 1, 1));
		lblRespawnTime.setFont(new Font(20.0));

		VBox respawnTimeBox = new VBox();
		respawnTimeBox.maxWidth(windowMinWidth);
		respawnTimeBox.setAlignment(Pos.CENTER);
		respawnTimeBox.getChildren().addAll(lblRespawnTime, sldrRespawnTime);

		// Slider und Label für Musiklautstärke
		Slider sldrMusicVolume = new Slider(0.0, 1.0, mediaPlayer.getVolume());
		sldrMusicVolume.valueProperty().addListener((obs, old, newValue) -> {
			mediaPlayer.setVolume(newValue.doubleValue());
			mediaPlayerVolume = newValue.doubleValue();
		});

		Label lblMusicVolume = new Label("Music Volume: ");
		lblMusicVolume.setTextFill(Color.color(1, 1, 1));
		lblMusicVolume.setFont(new Font(20.0));

		VBox musicVolumeBox = new VBox();
		musicVolumeBox.maxWidth(windowMinWidth);
		musicVolumeBox.setAlignment(Pos.CENTER);
		musicVolumeBox.getChildren().addAll(lblMusicVolume, sldrMusicVolume);

		// Slider und Label für Soundeffekte
		Slider sldrSoundEffect = new Slider(0.0, 1.0, mediaPlayer.getVolume());
		sldrSoundEffect.valueProperty().addListener((obs, old, newValue) -> {
			logicComponent.setMediaPlayerVolume(newValue.doubleValue());
		});

		Label lblSoundEffect = new Label("Effect Volume: ");
		lblSoundEffect.setTextFill(Color.color(1, 1, 1));
		lblSoundEffect.setFont(new Font(20.0));

		VBox soundEffectBox = new VBox();
		soundEffectBox.maxWidth(windowMinWidth);
		soundEffectBox.setAlignment(Pos.CENTER);
		soundEffectBox.getChildren().addAll(lblSoundEffect, sldrSoundEffect);

		// ComboBox und Label für Schwierigkeitseinstellung
		ComboBox<Difficulty> cmbDifficulty = new ComboBox<>();
		cmbDifficulty.getItems().addAll(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.JUSTDIE);

		cmbDifficulty.valueProperty().addListener((obs, old, newValue) -> {
			logicComponent.setNumberOfEnemiesPerSpawn(newValue.toEnemySpawns());
			logicComponent.setRushEnemyProbability(newValue.toRushEnemyProbability());
			logicComponent.setSpecialEnemyProbability(newValue.toSpeicalEnemyProbability());
			logicComponent.setEnemyShootProbability(newValue.toEnemyShootProbability());
		});
		cmbDifficulty.getSelectionModel().select(1);

		Label lblDifficulty = new Label("Difficulty: ");
		lblDifficulty.setTextFill(Color.color(1, 1, 1));
		lblDifficulty.setFont(new Font(20.0));

		HBox difficultyBox = new HBox();
		difficultyBox.setSpacing(20.0);
		difficultyBox.maxWidth(windowMinWidth);
		difficultyBox.setAlignment(Pos.CENTER);
		difficultyBox.getChildren().addAll(lblDifficulty, cmbDifficulty);

		// Zurück Button ins Hauptmenue
		Label backLabel = new Label("Back");
		backLabel.setTextFill(Color.color(1, 1, 1));
		backLabel.setFont(new Font(30.0));

		HBox backBox = new HBox();
		backBox.setMaxWidth(150);
		backBox.setAlignment(Pos.CENTER);
		backBox.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); " + "-fx-border-style: solid inside;"
				+ "-fx-border-width: 2;" + "-fx-border-color: rgba(0, 0, 0, 0.4)"); // FINEME
		backBox.getChildren().add(backLabel);
		backBox.setOnMouseClicked((MouseEvent e) -> {
			LoadMenu();
		});
		backBox.setOnMouseEntered((MouseEvent e) -> {
			backBox.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			backLabel.setTextFill(Color.GHOSTWHITE);
		});
		backBox.setOnMouseExited((MouseEvent e) -> {
			backBox.setStyle("-fx-background-color: rgba(10, 10,10, 0.4);" + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(0, 0, 0, 0.4)");
			backLabel.setTextFill(Color.WHITE);
		});

		VBox volumeBox = new VBox();
		volumeBox.getChildren().addAll(musicVolumeBox, soundEffectBox);

		VBox gameSettingsBox = new VBox();
		gameSettingsBox.getChildren().addAll(difficultyBox, respawnTimeBox, playerHealthBox);

		HBox allSettingsBox = new HBox();
		allSettingsBox.setAlignment(Pos.CENTER);
		allSettingsBox.setSpacing(50.0);
		allSettingsBox.getChildren().addAll(volumeBox, gameSettingsBox);

		VBox settingsButtons = new VBox();
		settingsButtons.setSpacing(10);

		settingsButtons.getChildren().addAll(lblSettings, allSettingsBox, backBox);
		settingsButtons.setAlignment(Pos.CENTER);

		Group controllGroup = new Group();
		controllGroup.getChildren().add(settingsButtons);

		mainBox.getChildren().add(controllGroup);
		return mainBox;
	}

	public VBox BuildGameRoot() {
		var root = setupLayout();
		if (logicComponent == null) {
			logicComponent = new GameLogic(canvas);
		}
		logicComponent.gameOverProperty.addListener((p, old, value) -> {
			if (value == true) {
				root.setEffect(new GaussianBlur());
				setupGameOverMenu(root);
			}
		});
		return root;

	}

	public VBox GetStartLayout() {
		VBox mainBox = setupStartMenu();
		mainBox.setAlignment(Pos.CENTER);
		mainBox.setSpacing(5);
		BackgroundImage backgroundImage = new BackgroundImage(new Image("file:images/background.png"),
				BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
				new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
				new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
		mainBox.setBackground(new Background(backgroundImage/* , backgroundAliens */));

		// Images
		Image spaceInvader = new Image("file:images/SpaceInvader2.gif");
		ImageView mascotView1 = new ImageView(spaceInvader);
		ImageView mascotView2 = new ImageView(spaceInvader);
		VBox.setVgrow(mainBox, Priority.ALWAYS);

		// Title
		Label titleLabel = new Label("Invaders");
		titleLabel.setFont(new Font("Lucida Console", 80.0));
		titleLabel.setTextFill(Color.color(1, 1, 1));

		// StartButton
		Image startOrangePlanet = new Image("file:images/PlanetOrange.png");
		Image startPurplePlanet = new Image("file:images/PlanetYellow.png");
		ImageView startPlanetImageView = new ImageView(startPurplePlanet);

		// ToDo: Dynamische Größe
		startPlanetImageView.setFitHeight(60);
		startPlanetImageView.setFitWidth(60);

		Label startLabel = new Label("Start");
		startLabel.setTextFill(Color.color(1, 1, 1));
		startLabel.setFont(new Font(40.0));

		HBox startBox = new HBox();
		startBox.setAlignment(Pos.CENTER);
		startBox.getChildren().addAll(startPlanetImageView, startLabel);
		startBox.setOnMouseClicked((MouseEvent e) -> {
			LoadGame();
			logicComponent.Reload();
		});
		startBox.setOnMouseEntered((MouseEvent e) -> {
			startPlanetImageView.setImage(startOrangePlanet);
			startPlanetImageView.setFitHeight(70);
			startPlanetImageView.setFitWidth(70);
		});
		startBox.setOnMouseExited((MouseEvent e) -> {
			startPlanetImageView.setImage(startPurplePlanet);
			startPlanetImageView.setFitHeight(60);
			startPlanetImageView.setFitWidth(60);
		});

		// Settings Button
		Image settingsGreenPlanet = new Image("file:images/PlanetGreen.png");
		Image settingsYellowPlanet = new Image("file:images/PlanetPurple.png");
		ImageView settingsPlanetImageView = new ImageView(settingsGreenPlanet);

		// ToDo: Dynamische Größe
		settingsPlanetImageView.setFitHeight(60);
		settingsPlanetImageView.setFitWidth(60);

		Label settingsLabel = new Label("Settings");
		settingsLabel.setTextFill(Color.color(1, 1, 1));
		settingsLabel.setFont(new Font(40.0));

		HBox settingsBox = new HBox();
		settingsBox.setAlignment(Pos.CENTER);
		settingsBox.getChildren().addAll(settingsPlanetImageView, settingsLabel);
		settingsBox.setOnMouseClicked((MouseEvent e) -> {
			LoadSettings();
		});
		settingsBox.setOnMouseEntered((MouseEvent e) -> {
			settingsPlanetImageView.setImage(settingsYellowPlanet);
			settingsPlanetImageView.setFitHeight(70);
			settingsPlanetImageView.setFitWidth(70);
		});
		settingsBox.setOnMouseExited((MouseEvent e) -> {
			settingsPlanetImageView.setImage(settingsGreenPlanet);
			settingsPlanetImageView.setFitHeight(60);
			settingsPlanetImageView.setFitWidth(60);
		});

		// Exit Button
		Image startBluePlanet = new Image("file:images/PlanetBlue.png");
		Image startWhitePlanet = new Image("file:images/PlanetWhite.png");
		ImageView exitPlanetImageView = new ImageView(startBluePlanet);

		// TODO: Dynamische Größe
		exitPlanetImageView.setFitHeight(60);
		exitPlanetImageView.setFitWidth(60);

		Label exitLabel = new Label("Quit Game");
		exitLabel.setTextFill(Color.color(1, 1, 1));
		exitLabel.setFont(new Font(40.0));

		HBox exitBox = new HBox();
		exitBox.setAlignment(Pos.CENTER);
		exitBox.getChildren().addAll(exitPlanetImageView, exitLabel);
		exitBox.setOnMouseClicked((MouseEvent e) -> {
			primaryStage.close();
		});
		exitBox.setOnMouseEntered((MouseEvent e) -> {
			exitPlanetImageView.setImage(startWhitePlanet);
			exitPlanetImageView.setFitHeight(70);
			exitPlanetImageView.setFitWidth(70);
		});
		exitBox.setOnMouseExited((MouseEvent e) -> {
			exitPlanetImageView.setImage(startBluePlanet);
			exitPlanetImageView.setFitHeight(60);
			exitPlanetImageView.setFitWidth(60);
		});

		HBox mascotTitleBox = new HBox();
		mascotTitleBox.getChildren().addAll(mascotView1, titleLabel, mascotView2);
		mascotTitleBox.setAlignment(Pos.TOP_CENTER);

		VBox menuButtons = new VBox();
		menuButtons.setSpacing(10);

		// menuButtons.getChildren().addAll(mascotTitleBox, startBox, resumeBox,
		// settingsBox, exitBox);
		menuButtons.getChildren().addAll(mascotTitleBox, new Group(startBox), new Group(settingsBox),
				new Group(exitBox));
		menuButtons.setAlignment(Pos.CENTER);

		Group controllGroup = new Group();
		controllGroup.getChildren().add(menuButtons);

		mainBox.getChildren().add(controllGroup);
		return mainBox;
	}

	private VBox setupStartMenu() {

		VBox root = new VBox();

		root.setPrefSize(windowMinWidth, windowMinHeight);
		// root.setMinSize(windowMinWidth, windowMinHeight);
		return root;

	}

	private VBox setupLayout() {

		// VBox root = new VBox();

		// root.setPrefSize(windowMinWidth, windowMinHeight);
		// root.setMinSize(windowMinWidth, windowMinHeight);
		VBox mainBox = new VBox();
		mainBox.setPrefSize(windowMinWidth, windowMinHeight);
		mainBox.setSpacing(5);
		VBox.setVgrow(mainBox, Priority.ALWAYS);

		Button btnPause = new Button("Pause");
		btnPause.setMaxWidth(Double.MAX_VALUE);
		btnPause.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logicComponent.pause();
				// root.setEffect(new GaussianBlur());
				// setupPauseMenu(root);
				mainBox.setEffect(new GaussianBlur());
				setupPauseMenu(mainBox);
			}

		});

		Button btnGiveUp = new Button("Give Up");
		btnGiveUp.setMaxWidth(Double.MAX_VALUE);
		btnGiveUp.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logicComponent.pause();
//				root.setEffect(new GaussianBlur()); 
//				setupGameOverMenu(root);
				mainBox.setEffect(new GaussianBlur());
				setupGameOverMenu(mainBox);
			}

		});

		HBox menuButtons = new HBox();
		menuButtons.getChildren().addAll(/* btnHome, btnRestart, btnSettings, */ btnPause, btnGiveUp);

		// mainBox.getChildren().add(menuButtons);

		Pane canvasHolder = new Pane();
		canvasHolder.maxWidth(Double.MAX_VALUE);
		canvasHolder.maxHeight(Double.MAX_VALUE);
		VBox.setVgrow(canvasHolder, Priority.ALWAYS);
		HBox.setHgrow(canvasHolder, Priority.ALWAYS);
		canvas = new Canvas(canvasHolder.getWidth(), canvasHolder.getHeight());
		var gc = canvas.getGraphicsContext2D();

		// make canvas resizable using bindings and listeners
		canvas.heightProperty().bind(canvasHolder.heightProperty());
		canvas.widthProperty().bind(canvasHolder.widthProperty());

		canvas.widthProperty().addListener((obs, oldWidth, newWidth) -> drawCanvas(gc));
		canvas.heightProperty().addListener((obs, oldHeight, newHeight) -> drawCanvas(gc));

		canvasHolder.getChildren().add(canvas);
		canvasHolder.setStyle("-fx-background-color: black; " + "-fx-border-color: purple; " + "-fx-border-width: 5");

		// mainBox.getChildren().addAll(menuButtons, canvasHolder);

		mainBox.getChildren().addAll(menuButtons, canvasHolder);

		return mainBox;

	}

	private void setupPauseMenu(VBox root) {

		VBox pauseRoot = new VBox(5);
		pauseRoot.getChildren().add(new Label("Paused"));
		pauseRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
		pauseRoot.setAlignment(Pos.CENTER);
		pauseRoot.setPadding(new Insets(20));

		var popupStage = new Stage(StageStyle.TRANSPARENT);

		ImageView pauseAlien = new ImageView(new Image("file:images/SpaceInvader2.gif"));
		pauseRoot.getChildren().add(pauseAlien);

		// Resume Button
		Label resumeLabel = new Label("Resume");
		resumeLabel.setTextFill(Color.color(1, 1, 1));
		resumeLabel.setFont(new Font(15.0));

		HBox resumeBox = new HBox();
		resumeBox.setAlignment(Pos.CENTER); 
		resumeLabel.setPrefWidth(100);
		resumeLabel.setAlignment(Pos.CENTER);
		resumeLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		resumeBox.getChildren().add(resumeLabel);
		resumeBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			logicComponent.unPause();
			popupStage.hide();
		});
		resumeBox.setOnMouseEntered((MouseEvent e) -> {
			resumeLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			resumeLabel.setTextFill(Color.GHOSTWHITE);
		});
		resumeBox.setOnMouseExited((MouseEvent e) -> {
			resumeLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			resumeLabel.setTextFill(Color.WHITE);
		});
		pauseRoot.getChildren().add(resumeBox);

		// Restart Button
		Label restartLabel = new Label("Restart");
		restartLabel.setTextFill(Color.color(1, 1, 1));
		restartLabel.setFont(new Font(15.0));

		HBox restartBox = new HBox();
		restartBox.setAlignment(Pos.CENTER); 
		restartLabel.setPrefWidth(100);
		restartLabel.setAlignment(Pos.CENTER);
		restartLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		restartBox.getChildren().add(restartLabel);
		restartBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			logicComponent.Reload();
			logicComponent.unPause();
			popupStage.hide();
		});
		restartBox.setOnMouseEntered((MouseEvent e) -> {
			restartLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			restartLabel.setTextFill(Color.GHOSTWHITE);
		});
		restartBox.setOnMouseExited((MouseEvent e) -> {
			restartLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			restartLabel.setTextFill(Color.WHITE);
		});
		pauseRoot.getChildren().add(restartBox);

		// StartMenu Button
		Label startMenuLabel = new Label("Menu");
		startMenuLabel.setTextFill(Color.color(1, 1, 1));
		startMenuLabel.setFont(new Font(15.0));

		HBox startMenuBox = new HBox();
		startMenuBox.setAlignment(Pos.CENTER); 
		startMenuLabel.setPrefWidth(100);
		startMenuLabel.setAlignment(Pos.CENTER);
		startMenuLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		startMenuBox.getChildren().add(startMenuLabel);
		startMenuBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			LoadMenu();
			popupStage.hide();
		});
		startMenuBox.setOnMouseEntered((MouseEvent e) -> {
			startMenuLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			startMenuLabel.setTextFill(Color.GHOSTWHITE);
		});
		startMenuBox.setOnMouseExited((MouseEvent e) -> {
			startMenuLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			startMenuLabel.setTextFill(Color.WHITE);
		});
		pauseRoot.getChildren().add(startMenuBox);

		// Exit Button
		Label exitLabel = new Label("Exit");
		exitLabel.setTextFill(Color.color(1, 1, 1));
		exitLabel.setFont(new Font(15.0));

		HBox exitBox = new HBox();
		exitBox.setAlignment(Pos.CENTER); 
		exitLabel.setPrefWidth(100);
		exitLabel.setAlignment(Pos.CENTER);
		exitLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		exitBox.getChildren().add(exitLabel);
		exitBox.setOnMouseClicked((MouseEvent e) -> {
			primaryStage.close();
		});
		exitBox.setOnMouseEntered((MouseEvent e) -> {
			exitLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			exitLabel.setTextFill(Color.GHOSTWHITE);
		});
		exitBox.setOnMouseExited((MouseEvent e) -> {
			exitLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			exitLabel.setTextFill(Color.WHITE);
		});
		pauseRoot.getChildren().add(exitBox);
		
		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setScene(new Scene(pauseRoot, Color.TRANSPARENT));
		popupStage.addEventFilter(KeyEvent.KEY_PRESSED, k -> {
			if (k.getCode() == KeyCode.ESCAPE) {
				root.setEffect(null);
				logicComponent.unPause();
				popupStage.hide();
				k.consume();
			}
		});

		addPopupListeners(popupStage);

		popupStage.show();
		popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
		popupStage.setY(currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2 - popupStage.getHeight() / 2);

	}

	private void setupGameOverMenu(VBox root) {

		logicComponent.gameOver();

		VBox gameOverRoot = new VBox(5);
		gameOverRoot.getChildren().add(new Label("You died. Game Over"));
		gameOverRoot.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
		gameOverRoot.setAlignment(Pos.CENTER);
		gameOverRoot.setPadding(new Insets(20));

		var popupStage = new Stage(StageStyle.TRANSPARENT);

		ImageView pauseAlien = new ImageView(new Image("file:images/SpaceInvader2.gif"));
		gameOverRoot.getChildren().add(pauseAlien);
		
		// Restart Button
		Label restartLabel = new Label("Restart");
		restartLabel.setTextFill(Color.color(1, 1, 1));
		restartLabel.setFont(new Font(15.0));

		HBox restartBox = new HBox();
		restartBox.setAlignment(Pos.CENTER);
		restartLabel.setPrefWidth(100);
		restartLabel.setAlignment(Pos.CENTER);
		restartLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		restartBox.getChildren().add(restartLabel);
		restartBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			logicComponent.Reload();
			logicComponent.unPause();
			popupStage.hide();
		});
		restartBox.setOnMouseEntered((MouseEvent e) -> {
			restartLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			restartLabel.setTextFill(Color.GHOSTWHITE);
		});
		restartBox.setOnMouseExited((MouseEvent e) -> {
			restartLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			restartLabel.setTextFill(Color.WHITE);
		});
		gameOverRoot.getChildren().add(restartBox);

		// Settings Button
		Label settingsLabel = new Label("Settings");
		settingsLabel.setTextFill(Color.color(1, 1, 1));
		settingsLabel.setFont(new Font(15.0));

		HBox settingsBox = new HBox();
		settingsBox.setAlignment(Pos.CENTER);
		settingsLabel.setPrefWidth(100);
		settingsLabel.setAlignment(Pos.CENTER);
		settingsLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		settingsBox.getChildren().add(settingsLabel);
		settingsBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			LoadSettings();
			popupStage.hide();
		});
		settingsBox.setOnMouseEntered((MouseEvent e) -> {
			settingsLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			settingsLabel.setTextFill(Color.GHOSTWHITE);
		});
		settingsBox.setOnMouseExited((MouseEvent e) -> {
			settingsLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			settingsLabel.setTextFill(Color.WHITE);
		});
		gameOverRoot.getChildren().add(settingsBox);

		// StartMenu Button
		Label startMenuLabel = new Label("Menu");
		startMenuLabel.setTextFill(Color.color(1, 1, 1));
		startMenuLabel.setFont(new Font(15.0));

		HBox startMenuBox = new HBox();
		startMenuBox.setAlignment(Pos.CENTER);
		startMenuLabel.setPrefWidth(100);
		startMenuLabel.setAlignment(Pos.CENTER);
		startMenuLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		startMenuBox.getChildren().add(startMenuLabel);
		startMenuBox.setOnMouseClicked((MouseEvent e) -> {
			root.setEffect(null);
			LoadMenu();
			popupStage.hide();
		});
		startMenuBox.setOnMouseEntered((MouseEvent e) -> {
			startMenuLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			startMenuLabel.setTextFill(Color.GHOSTWHITE);
		});
		startMenuBox.setOnMouseExited((MouseEvent e) -> {
			startMenuLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			startMenuLabel.setTextFill(Color.WHITE);
		});
		gameOverRoot.getChildren().add(startMenuBox);

		// Exit Button
		Label exitLabel = new Label("Exit");
		exitLabel.setTextFill(Color.color(1, 1, 1));
		exitLabel.setFont(new Font(15.0));

		HBox exitBox = new HBox();
		exitLabel.setPrefWidth(100);
		exitLabel.setAlignment(Pos.CENTER);
		exitBox.setAlignment(Pos.CENTER);
		exitLabel.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4); -fx-border-style: solid inside;"
				+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
		exitBox.getChildren().add(exitLabel);
		exitBox.setOnMouseClicked((MouseEvent e) -> {
			primaryStage.close();
		});
		exitBox.setOnMouseEntered((MouseEvent e) -> {
			exitLabel.setStyle("-fx-background-color: rgba(20, 20, 20, 0.4); " + "-fx-border-style: solid inside; "
					+ "-fx-border-width: 2; " + "-fx-border-color: rgba(50, 50, 50, 0.4)");
			exitLabel.setTextFill(Color.GHOSTWHITE);
		});
		exitBox.setOnMouseExited((MouseEvent e) -> {
			exitLabel.setStyle("-fx-background-color: rgba(10, 10,10, 0.4); -fx-border-style: solid inside; "
					+ "-fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.4)");
			exitLabel.setTextFill(Color.WHITE);
		});
		gameOverRoot.getChildren().add(exitBox);

		popupStage.initOwner(primaryStage);
		popupStage.initModality(Modality.APPLICATION_MODAL);
		popupStage.setScene(new Scene(gameOverRoot, Color.TRANSPARENT));

		addPopupListeners(popupStage);
		popupStage.show();
		popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
		popupStage.setY(
				currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2 - popupStage.getHeight() / 2);

		popupStage.addEventFilter(KeyEvent.KEY_PRESSED, k -> {
			if (k.getCode() == KeyCode.ESCAPE) {
				root.setEffect(null);
				logicComponent.Reload();
				popupStage.hide();
				k.consume();
			}
		});

	}

	public void addPopupListeners(Stage popupStage) {
		// add listeners to move popup automatically
		// ugly but it works
		currentSceneWidth.addListener((obs, oldVal, newVal) -> {

			popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
			popupStage.setY(currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2
					- popupStage.getHeight() / 2);

		});

		currentSceneHeight.addListener((obs, oldVal, newVal) -> {

			popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
			popupStage.setY(currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2
					- popupStage.getHeight() / 2);

		});

		currentStageX.addListener((obs, oldVal, newVal) -> {

			popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
			popupStage.setY(currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2
					- popupStage.getHeight() / 2);

		});

		currentStageY.addListener((obs, oldVal, newVal) -> {

			popupStage.setX(currentStageX.get() + currentSceneWidth.get() / 2 - popupStage.getWidth() / 2);
			popupStage.setY(currentStageY.get() + decorationOffsetY + currentSceneHeight.get() / 2
					- popupStage.getHeight() / 2);

		});

	}

	public void drawCanvas(GraphicsContext gc) {
		// gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		// bugfix: enemies shouldn't move faster because of resizing ...
		// now using redraw function that doesn't move objects ...
		logicComponent.redrawCanvasNoMove();
	}

}
