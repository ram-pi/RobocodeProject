package org.pattern.utils;

import java.util.LinkedList;
import java.util.List;

public class VisitCountStorageDensity {
	private List<Double> storage;
	


	public List<Double> getStorage() {
		return storage;
	}

	public void setStorage(List<Double> storage) {
		this.storage = storage;
	}

	public  VisitCountStorageDensity() {
		storage = new LinkedList<>();
	}
	
	public void visit(double gf) {
		gf = Math.min(1.0, gf);
		gf = Math.max(-1.0, gf);

		
		storage.add(gf);
	}
	
//	public void decay(double factor) {
//		for (int i = 0; i < Costants.NUM_BINS; i++) {
//			storage[i]/= factor;
//		}
//	}
	


}
