package org.pattern.utils;

import org.robot.Enemy;

public class EnemyInfo {
	private Enemy enemy;
	private long lastTimeSaw;
	
	public EnemyInfo() {
		this.enemy = null;
		this.lastTimeSaw = Long.MIN_VALUE;
	}
	
	public EnemyInfo(Enemy enemy, long lastTimeSaw) {
		this.enemy = enemy;
		this.lastTimeSaw = lastTimeSaw;
	}

	public Enemy getEnemy() {
		return enemy;
	}

	public long getLastTimeSaw() {
		return lastTimeSaw;
	}

	public void setEnemy(Enemy enemy) {
		this.enemy = enemy;
	}

	public void setLastTimeSaw(long lastTimeSaw) {
		this.lastTimeSaw = lastTimeSaw;
	}
}
