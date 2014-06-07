package org.pattern.utils;


import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.pattern.radar.GBulletFiredEvent;

public class VisitCountStorageSegmented {

	private Map<BitSet, Double> storage;
	
	
	public VisitCountStorageSegmented() {
		storage = new HashMap<BitSet, Double>();
	}
	
	public void visit(BitSet point, double gf) {
		gf = Math.min(1.0, gf);
		gf = Math.max(-1.0, gf);
		
		Double p = storage.get(point);
		p = new Double(gf);
		storage.put(point, p);
		
	}
	
	public double getGF(BitSet p) {
		return storage.get(p);
	}
	
	public int distance(BitSet a, BitSet b) {
		BitSet temp = new BitSet();
		
		temp.clear();
		temp.or(a); //cloning
		temp.xor(b);
		
		return temp.cardinality();
	}
	
	
	public List<BitSet> getNearest(BitSet point) {
		List<BitSet> nearest = new LinkedList<>();
		List<Integer> distances = new LinkedList<>();

		Iterator<BitSet> it = storage.keySet().iterator();
		
		while(it.hasNext()) {
			BitSet thisBitSet = it.next();

			
			int thisDistance = distance(thisBitSet, point);

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
	
}
