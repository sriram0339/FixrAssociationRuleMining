package associationRuleMiner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

public class FrequentItemSetDB {
	List<RepoData> rList;
	List<FINodes> topLevel;
	
	public FrequentItemSetDB(List<RepoData> h){
		rList= h;
		topLevel = new ArrayList<FINodes>();
	}

	public void buildFrequentItemSets(double alpha){
		int n = rList.size();
		int cutoffLength= (int) ( alpha * (double) n); 
		TreeSet<Integer> allIndices = new TreeSet<Integer>(); 
		TreeMap<Integer, TreeSet<Integer> > idxMap = new TreeMap<Integer, TreeSet<Integer> > ();
		for (RepoData r: rList){
			r.addIndexToSet(allIndices,idxMap);	
		}
		
		Iterator<Integer> vi = allIndices.descendingIterator();
		while (vi.hasNext()){
			int j = vi.next();
			TreeSet<Integer> lj = idxMap.get(j);
			assert(lj != null);
			if (lj.size() >= cutoffLength){
				FINodes rj = new FINodes(rList, allIndices, lj, idxMap,null, j);
				topLevel.add(rj);
				rj.addChildrenNodesRecursive(cutoffLength);
				rj.printFrequentItemSets();
			}
		}	
	}
	
	public int obtainFrequencyForSet (TreeSet<Integer> qry){
		// Return 0 if query is not a frequent item set

		FINodes curNode = null;
		Iterator<Integer> it = qry.descendingIterator();
		while (it.hasNext()){
			int j = it.next();
			if (curNode == null){
				curNode = findRootNodeForID(j);
			} else {
				curNode = curNode.findChildByID(j);
			}
			if (curNode == null){
				return 0;
			}
		}
		assert(curNode != null);
		int freq = curNode.itemSetFrequency();
		return freq;
	}
	
	private FINodes findRootNodeForID(int j) {
		for (FINodes n: topLevel){
			if (n.myID == j)
				return n;
		}
		return null;
	}

	public void obtainAssociationRulesFromFrequentItemSets( double beta){
		/* Walk all the nodes in the frequent item tree.
		 * For each node n with 2 or more indices in it's set.
		 *   Compute the ratio of frequency of node / frequency of antecedent set
		 *   If ratio is more than beta ==> output association rule.
		 */
		for (FINodes n: topLevel){
			n.mineAssociationRules(this, beta);
		}
	}
	
}
