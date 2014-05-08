package org.pattern.radar;

import java.util.Map;

import org.robot.Enemy;

public class UpdatedEnemiesListEvent {

	private Map<String, Enemy> enemies;
	
	public UpdatedEnemiesListEvent(Map<String, Enemy> enemies) {
		this.enemies = enemies;
	}

}
