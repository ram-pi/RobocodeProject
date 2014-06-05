package org.pattern.utils;

import java.io.OutputStreamWriter;

public class VisitCountStorage {
	private double storage[];
	
	public double[] getStorage() {
		return storage;
	}

	public void setStorage(double[] storage) {
		this.storage = storage;
	}

	public VisitCountStorage() {
		storage = new double[Costants.NUM_BINS];
	}
	
	public int visit(double gf) {
		gf = Math.min(1.0, gf);
		gf = Math.max(-1.0, gf);
		
		int bin = (int)(gf * Costants.NUM_BINS/2.);
		bin += Costants.NUM_BINS/2;
		
		for(int i = 0; i < Costants.NUM_BINS; i++) {
//			storage[i] /= 3.;
			if (i == bin) {
				storage[i] = 1;
				continue;
			}
			
			storage[i] += 1./(Math.abs(bin - i)*2);
		}
		
		return bin;
	}
	
	public void decay(double factor) {
		for (int i = 0; i < Costants.NUM_BINS; i++) {
			storage[i]/= factor;
		}
	}
	
	public double getVisits(double gf) {
		double danger = 0;
		int bin = (int)(gf * Costants.NUM_BINS/2);
		bin += Costants.NUM_BINS/2;

		int startBin = Math.max(bin-Costants.SMOOTH_BINS, 0);
		int endBin = Math.min(bin+Costants.SMOOTH_BINS, Costants.NUM_BINS);
		
		for (int i = startBin; i < endBin; i++) {
			danger += storage[i];
		}
		return danger;
	}
	
	public double getPeak() {
		double max = Double.MIN_VALUE;
		int bin = 0;

		// TODO get best "three bin" average
		for (int i = 0; i < Costants.NUM_BINS; i++) {
			if (storage[i] > max) {
				max = storage[i];
				bin = i;
			}
		}

		if (bin < Costants.NUM_BINS / 2) {
			return -(1 - 1. / Costants.NUM_BINS / 2 * bin);
		} else if (bin > Costants.NUM_BINS / 2) {
			return 1. / (Costants.NUM_BINS / 2) * (bin - Costants.NUM_BINS / 2);
		} else
			return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < Costants.NUM_BINS;i++){
			sb.append(" "+i+": ");
			sb.append(storage[i]);
		}
		return sb.toString();
	}
	
}
