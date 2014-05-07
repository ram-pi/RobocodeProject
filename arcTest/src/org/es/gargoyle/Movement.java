package org.es.gargoyle;

import java.util.Observable;
import java.util.Observer;

public class Movement implements Observer{
	private Gargoyle gargoyle;
	public Movement(Gargoyle gargoyle) {
		this.gargoyle = gargoyle;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof GBulletFiredEvent) {
			gargoyle.out.println("Bullet fired");
		}
		
	}
	
}
