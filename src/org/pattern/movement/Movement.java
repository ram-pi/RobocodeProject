package org.pattern.movement;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.pattern.radar.GBulletFiredEvent;

import robocode.AdvancedRobot;

public class Movement implements Observer{
	private AdvancedRobot robot;
	private List<GBulletFiredEvent> bullets;
	
	public Movement(AdvancedRobot robot) {
		this.robot = robot;
		bullets = new LinkedList<>();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof GBulletFiredEvent) {
			bullets.add((GBulletFiredEvent)arg);
		}
	}
	
	public void doMovement() {
		
	}

	public void consumeOnPaintEvent(Graphics2D g) {
		
		for (GBulletFiredEvent bullet : bullets) {
   			double radius = bullet.getVelocity() * (robot.getTime() - bullet.getFiringTime());
   			
   			/* the bullet is fired from cannon that is displaced 10px from the center of the robot */
   			radius += 10;
   			
   			g.drawArc((int)(bullet.getFiringRobot().getX() - radius), (int)(bullet.getFiringRobot().getY() - radius), (int)radius*2, (int)radius*2, 0, 360);
   			g.drawRect((int)bullet.getFiringRobot().getX(), (int)bullet.getFiringRobot().getY(), 10, 10);
		}
		
	}
	
}
