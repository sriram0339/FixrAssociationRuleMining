package associationRuleMiner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FINodes {
	
	TreeSet<Integer> allIdx;
	TreeSet<Integer> relObjs;
	TreeMap<Integer, TreeSet<Integer> > idx2ObjIDs;
	TreeSet<Integer> mySet;
	public int myID;
	List<FINodes> children;
	FrequentItemSetDB fItemDB;
	
	public FINodes( FrequentItemSetDB f,
			TreeSet<Integer> allIndices,  // Collection of all indices
			TreeSet<Integer> objSet,
			TreeMap<Integer, TreeSet<Integer>> idxMap,  // Map from each integer index to a set of object ids
			TreeSet<Integer> mSet, 
			int j) {

		myID = j;
		fItemDB = f;
		if (mSet != null)
			mySet = new TreeSet<Integer>(mSet);
		else 
			mySet = new TreeSet<Integer>();
		mySet.add(j);
		relObjs  =objSet;
		allIdx = allIndices;
		idx2ObjIDs = idxMap;
		children = null;
		

	}

	void addChildrenNodes(int cutoff){
		// Consider each child node j < myID
		// Compute the frequency of occurrences of mySet U { j }
		//  If it is beyond cutoff, create a new node.
	 //   System.out.println("Adding children Size: "+ allIdx.size());
		Iterator<Integer> it = allIdx.descendingIterator();
		children = new ArrayList<FINodes>();
		TreeSet<Integer> siblingIndices = new TreeSet<Integer> ();
		while (it.hasNext()){
			int j = it.next();
			if ( j < myID){
				// Compute the frequency of mySet U j
				TreeSet<Integer> relIdx = new TreeSet<Integer>(relObjs);
				assert(idx2ObjIDs.containsKey(j));
				TreeSet<Integer> mIdx = this.idx2ObjIDs.get(j);
				if (mIdx == null) continue;
				relIdx.retainAll(mIdx);
				if (relIdx.size() >= cutoff ){
					FINodes newChild = new FINodes(fItemDB,siblingIndices,relIdx,idx2ObjIDs,mySet,j);
					children.add(newChild);
					siblingIndices.add(j);
				}
			}
		}
	}
	
	void addChildrenNodesRecursive(int cutoff){
		addChildrenNodes(cutoff);
		assert(children != null);
		for(FINodes n: children){
			n.addChildrenNodesRecursive(cutoff);
		}
	}
	
	private  String featureSetToString(Set<Integer> mSet){
		String ret = "{ ";
		for(int i:mSet){
			String fName = fItemDB.getFeatureName(i);
			ret = ret + fName + ", ";
		}
		ret = ret + "}";
		return ret;
	}
	
	void printFrequentItemSets(){
		
		//System.out.println("Set: "+ featureSetToString(this.mySet) +"\t\t Frequency: "+relObjs.size()+"\n");
		
		if (children != null){
			for(FINodes n: children){
				n.printFrequentItemSets();
			}
		}
	}

	public FINodes findChildByID(int j) {
		if (children == null) return  null;
		for (FINodes n:children){
			if (n.myID == j){
				return n;
			}
		}
		return null;
	}

	public int itemSetFrequency() {
		
		return relObjs.size();
	}

	private void printAssociationRule(TreeSet<Integer> ant, int j, double r){
		System.out.println("Association Rule with strength: "+ r);
		System.out.println(featureSetToString(ant) + "===>" + fItemDB.getFeatureName(j));
		System.out.println("----------------");
	}
	
    public void mineAssociationRules(FrequentItemSetDB fItemDB, double beta, CommitDateRanges cdr) {
		int k = mySet.size();
		if ( k >= 2){
			// Consider association rules for this node.
			int freq = relObjs.size();
			for (int j: mySet){
				TreeSet<Integer> tmp = new TreeSet<Integer>(mySet);
				tmp.remove(j);
				Set<Integer> antecedentMatchingRepoObjIDs = fItemDB.obtainFrequencyForSet(tmp);
				int antecedentFreq = antecedentMatchingRepoObjIDs.size();
				assert(antecedentFreq > 0);
				double r = (double) freq / (double) antecedentFreq;
				if (r >= beta){
					//printAssociationRule(tmp,j,r);
					AssociationRule aRule = new AssociationRule(tmp, j, r, fItemDB);
					fItemDB.pushAssociationRule(aRule);
					aRule.setDateRanges(cdr);
					for (int l:antecedentMatchingRepoObjIDs){
						RepoData rdata = fItemDB.getRepoDataFromID(l);
						aRule.evaluateRuleAgainstRepoData(rdata);
					}
				}
			}
		}
		if (children != null){
			for (FINodes n:children)
				n.mineAssociationRules(fItemDB, beta,cdr);
		}
	}
}
