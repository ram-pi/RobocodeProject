package org.pattern.utils;

import java.io.OutputStreamWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VisitCountStorage {
	private int NUM_BINS = 43;
	private int SMOOTH_BINS = 4;
	private Map<BitSet, double[]> storage;
	
	

	public VisitCountStorage() {
		storage = new HashMap<BitSet, double[]>();
	}
	
	public int visit(BitSet point, double gf) {
		gf = Math.min(1.0, gf);
		gf = Math.max(-1.0, gf);
		
		double[] bins = storage.get(point);
		if (bins == null) {
			bins = new double[NUM_BINS];
			storage.put(point, bins);
		}
		
		int bin = (int)(gf * NUM_BINS/2.);
		bin += NUM_BINS/2;
		
		for(int i = 0; i < NUM_BINS; i++) {
//			storage[i] /= 3.;
			if (i == bin) {
				bins[i] = 1;
				continue;
			}
			
			bins[i] += 1./(Math.abs(bin - i)*2);
		}
		
		return bin;
	}
	
//	public void decay(double factor) {
//		for (int i = 0; i < NUM_BINS; i++) {
//			storage[i]/= factor;
//		}
//	}
	
	private List<double[]> getNearest(BitSet point) {
		List<BitSet> nearest = new LinkedList<>();
		
		int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
		BitSet maxElement = null;
		for (BitSet bitset : storage.keySet()) {
			BitSet temp = new BitSet();
			temp.or(bitset); //cloning
			temp.and(bitset);
			int distance = temp.cardinality();
			
			if (nearest.size() < 3) {
				nearest.add(bitset);
				if (distance > max) {
					max = distance;
					maxElement = bitset;
				}
				continue;
			}
			
			if (distance < max) {
				BitSet toRemove = null;
				for (Bitset bs : nearest) {
					
				}
			}
			
		}
		
	}
	public double getVisits(Object p, double gf) {
		double danger = 0;
		int bin = (int)(gf * NUM_BINS/2);
		bin += NUM_BINS/2;

		int startBin = Math.max(bin-SMOOTH_BINS, 0);
		int endBin = Math.min(bin+SMOOTH_BINS, NUM_BINS);
		
		for (int i = startBin; i < endBin; i++) {
			danger += storage[i];
		}
		return danger;
	}
	
	public double getPeak() {
		double max = Double.MIN_VALUE;
		int bin = 0;

		// TODO get best "three bin" average
		for (int i = 0; i < NUM_BINS; i++) {
			if (storage[i] > max) {
				max = storage[i];
				bin = i;
			}
		}

		if (bin < NUM_BINS / 2) {
			return -(1 - 1. / NUM_BINS / 2 * bin);
		} else if (bin > NUM_BINS / 2) {
			return 1. / (NUM_BINS / 2) * (bin - NUM_BINS / 2);
		} else
			return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < NUM_BINS;i++){
			sb.append(" "+i+": ");
			sb.append(storage[i]);
		}
		return sb.toString();
	}
	
}
