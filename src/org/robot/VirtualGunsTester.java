package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.shooting.Bullet;
import org.pattern.shooting.ShootingUtils;

import robocode.AdvancedRobot;

public class VirtualGunsTester extends AdvancedRobot {
	private List<Bullet> virtualBullets = new LinkedList<>();
	private double firePower = 3.0;

	@Override
	public void run() {

		Point2D target1 = new Point2D.Double(100, 100);
		Point2D target2 = new Point2D.Double(200, 400);
		Point2D target3 = new Point2D.Double(400, 800);

		double angle1 = ShootingUtils.findAngle(target1.getX(), target1.getY(), getX(), getY());
		double angle2 = ShootingUtils.findAngle(target2.getX(), target2.getY(), getX(), getY());
		double angle3 = ShootingUtils.findAngle(target3.getX(), target3.getY(), getX(), getY());

		Bullet b1 = new Bullet(new Point2D.Double(getX(), getY()), angle1, firePower, getTime(), target1);
		Bullet b2 = new Bullet(new Point2D.Double(getX(), getY()), angle2, firePower, getTime(), target2);
		Bullet b3 = new Bullet(new Point2D.Double(getX(), getY()), angle3, firePower, getTime(), target3);

		virtualBullets.add(b1); virtualBullets.add(b2); virtualBullets.add(b3);

		while (true) {
			for (Bullet b : virtualBullets) {
				long timeElapsed = getTime() - b.getTime();
				double bulletSpeed = 20 - 3*firePower;
				double spaceWalked = (double) timeElapsed * bulletSpeed;
				double startX = b.getActualPosition().getX();
				double startY = b.getActualPosition().getY();
				startX = startX + spaceWalked*(Math.sin(Math.toRadians(b.getBearing())));
				startY = startY + spaceWalked*(Math.cos(Math.toRadians(b.getBearing())));
				b.setTime(getTime());
				Point2D tmp = new Point2D.Double(startX, startY);
				b.setActualPosition(tmp);
				if (startX < 0 || startX > this.getBattleFieldWidth() || startY < 0 || startY > this.getBattleFieldHeight())
					b.setPassed(true);
				if (startX == target1.getX() && startY == target1.getY()) {
					b.setEnemyFired(true);
					b.setPassed(true);
				}
			}
			execute();
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		for (Bullet b : virtualBullets) {
			if (!b.isPassed())
				g.drawRect((int)b.getActualPosition().getX(), (int)b.getActualPosition().getY(), 10, 10);
		}
	}
}
