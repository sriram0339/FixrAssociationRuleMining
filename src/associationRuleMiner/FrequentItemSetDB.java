package associationRuleMiner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class FrequentItemSetDB {
	List<RepoData> rList;
	String [] featureNames;
	List<FINodes> topLevel;
	List<AssociationRule> associationRuleList;
	
	public FrequentItemSetDB(List<RepoData> h, String [] fNames){
		rList= h;
		topLevel = new ArrayList<FINodes>();
		featureNames = fNames;
		associationRuleList = new ArrayList<AssociationRule>();
	}

	public void buildFrequentItemSets(int cutoffLength){
		TreeSet<Integer> allIndices = new TreeSet<Integer>(); 
		TreeMap<Integer, TreeSet<Integer> > idxMap = new TreeMap<Integer, TreeSet<Integer> > ();
		for (RepoData r: rList){
			r.addIndexToSet(allIndices,idxMap);	
		}
		
		Iterator<Integer> vi = allIndices.descendingIterator();
		TreeSet<Integer> frequentIndices = new TreeSet<Integer>();
		while (vi.hasNext()){
			int j = vi.next();
			TreeSet<Integer> lj = idxMap.get(j);
			assert(lj != null);
			if (lj.size() >= cutoffLength){
				FINodes rj = new FINodes(this, frequentIndices, lj, idxMap,null, j);
				frequentIndices.add(j);
				//System.out.println("Frequent Feature:" + getFeatureName(j));
				topLevel.add(rj);
			}
			
		}
		for (FINodes rj:topLevel){
			rj.addChildrenNodesRecursive(cutoffLength);
			rj.printFrequentItemSets();
		}
	}
	
	public Set<Integer> obtainFrequencyForSet (TreeSet<Integer> qry){
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
				return null;
			}
		}
		assert(curNode != null);
		return curNode.relObjs;
	}
	
	private FINodes findRootNodeForID(int j) {
		for (FINodes n: topLevel){
			if (n.myID == j)
				return n;
		}
		return null;
	}

	public void obtainAssociationRulesFromFrequentItemSets( double beta, CommitDateRanges cdr){
		/* Walk all the nodes in the frequent item tree.
		 * For each node n with 2 or more indices in it's set.
		 *   Compute the ratio of frequency of node / frequency of antecedent set
		 *   If ratio is more than beta ==> output association rule.
		 */
		for (FINodes n: topLevel){
			n.mineAssociationRules(this, beta, cdr);
		}
	}
	public String getFeatureNameNoID(int i){
		assert(i >= 0);
		if (i < featureNames.length){
			return featureNames[i];
		}
		return "UNKNOWN_FEATURE_ERR";
	}
	public String getFeatureName( int i){
		assert( i >= 0);
		if (i < featureNames.length){
			return featureNames[i] +"("+i+")";
		}
		return ("UNKNOWN_FEATURE ("+i +")");
	}

	public void pushAssociationRule(AssociationRule aRule) {
		associationRuleList.add(aRule);
		
	}
	public void htmlDumpAllRules(String fStem, List<Integer> clusterBoundaries){
		 try {
			//int curCluster = 0;
			//int curClusterBoundary = clusterBoundaries.get(curCluster);
			PrintWriter fStream = new PrintWriter("index.html", "UTF-8");
			fStream.println("<html>\n <head>\n  <title> Association Rules Mined </title>\n </head> \n <body>");
			fStream.println("<table><tbody>");
			int curCluster = 0;
			//int count = 0;
			for (AssociationRule a: associationRuleList){
//				if (count > curClusterBoundary){
//					
//					curCluster++;
//					if (curCluster < clusterBoundaries.size()){
//						curClusterBoundary = clusterBoundaries.get(curCluster);
//					} else {
//						curClusterBoundary = associationRuleList.size() +1;
//					}
//				}
//				count ++;
				if (a.clusterID != curCluster){
					fStream.println("<tr></tr> <tr><td> <h3> Begin New Cluster </h3> </tr><tr></tr> ");
					curCluster = a.clusterID;
				}
				boolean trendingRule = a.isTrendingRule();
				String s = a.toString();
				fStream.format("<tr> <td> %d <td> Rule # %d: <td> (Matches: %d) <td> (Violations: %d) <td> <a href=\"%s%d.html\"> %s </a> \n", a.clusterID, a.ruleID, a.numMatches(), a.numViolations(), fStem,a.ruleID,s);
				if (trendingRule){
					fStream.println("<font color=\"red\" style=\"large\"> &nbsp; &nbsp; TimeTrending </font>");
				}
				fStream.println("</tr>");
				a.htmlPrintRuleWithChart(fStem);
			}
			fStream.println("</body>\n</html>");
			fStream.close();
		 }catch (IOException e){
			 e.printStackTrace();
			 System.exit(1);
		 }
	}
	public void printAllAssociationRules(){
		for (AssociationRule a:associationRuleList){
			System.out.println(a.toString());
			System.out.println("------");
			a.displayTimeSignature();
			System.out.println("------");
		}
		System.out.println(associationRuleList.size()+" rules printed.");
	}

	public RepoData getRepoDataFromID(int l) {
		assert( l >= 0 && l < rList.size());
		return rList.get(l);
	}
	
}
