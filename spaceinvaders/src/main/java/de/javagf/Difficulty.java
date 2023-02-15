package de.javagf;

public enum Difficulty {

	EASY("Easy", 5, 0.2f, 0.1f, 0.003f),
	MEDIUM("Medium", 10, 0.3f, 0.2f, 0.005f),
	HARD("Hard", 15, 0.4f, 0.3f, 0.01f),
	JUSTDIE("Just Die", 20, 0.6f, 0.3f, 0.03f);
	
	private final String name;
	private final int numberOfEnemiesPerSpawn;
	private final float chanceSpecialEnemies;
	private final float chanceRushEnemies;
	private final float enemyShootProbability;
	
	private Difficulty(String name, int numberOfEnemiesPerSpawn, float chanceSpeicalEnemies, float chanceRushEnemies, float enemyShootProbability) {
		this.name = name;
		this.numberOfEnemiesPerSpawn = numberOfEnemiesPerSpawn;
		this.chanceSpecialEnemies = chanceSpeicalEnemies;
		this.chanceRushEnemies = chanceRushEnemies;
		this.enemyShootProbability = enemyShootProbability;
	}
	
	
	public String ToString() {
		return name;
	}

	public int toEnemySpawns()  {
		return numberOfEnemiesPerSpawn;
	}
	
	public float toSpeicalEnemyProbability()  {
		return chanceSpecialEnemies;
	}
	
	public float toRushEnemyProbability()  {
		return chanceRushEnemies;
	}
	
	public float toEnemyShootProbability()  {
		return enemyShootProbability;
	}
	
}


