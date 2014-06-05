package org.pattern.utils;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class VisitCountStorageSegmented {
	private int NUM_BINS = 43;
	private int SMOOTH_BINS = 8;
	private Map<BitSet, double[]> storage;
	
	

	public Map<BitSet, double[]> getStorage() {
		return storage;
	}

	public void setStorage(Map<BitSet, double[]> storage) {
		this.storage = storage;
	}

	public VisitCountStorageSegmented() {
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
	
	
	public List<BitSet> getNearest(BitSet point) {
		List<BitSet> nearest = new LinkedList<>();
		List<Integer> distances = new LinkedList<>();

		Iterator<BitSet> it = storage.keySet().iterator();
		BitSet temp = new BitSet();
		
		while(it.hasNext()) {
			BitSet thisBitSet = it.next();
		

			temp.clear();
			temp.or(thisBitSet); //cloning
			temp.xor(point);
			
			int thisDistance = temp.cardinality();

			int index = 0;
			ListIterator<Integer> dIt = distances.listIterator();
			while (dIt.hasNext()) {
				if (dIt.next() > thisDistance) 
					break;
				index = dIt.nextIndex();
			}

			nearest.add(index, thisBitSet);
			distances.add(index, thisDistance);
		}
		return nearest;
	}
	
	public double getVisits(BitSet p, double gf) {
		List<BitSet> neareset = getNearest(p);
		int K  = Math.min(4, neareset.size());
		double danger = 0;
	
		int bin = (int) (gf * NUM_BINS / 2);
		bin += NUM_BINS / 2;
		
		for (int i = 0; i < K; i++) {
			int startBin = Math.max(bin - SMOOTH_BINS, 0);
			int endBin = Math.min(bin + SMOOTH_BINS, NUM_BINS);

			for (int j = startBin; j < endBin; j++) {
				danger += storage.get(neareset.get(i))[j];
			}
		}
		return danger;
	}
	
	public double getPeak(BitSet p) {
		double max = Double.MIN_VALUE;
		int bin = 0;
		
		double[] bins = new double[NUM_BINS];
		List<BitSet> neareset = getNearest(p);
		int K = Math.min(4, neareset.size());

		if (K == 0)
			return 0.0;

		for (int j = 0; j < K; j++) {
			for (int i = 0; i < NUM_BINS; i++) {
				bins[i] += storage.get(neareset.get(j))[i];
			}
		}
		// TODO get best "three bin" average
		for (int i = 0; i < NUM_BINS; i++) {
			if (bins[i] > max) {
				max = bins[i];
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
	

	
}
