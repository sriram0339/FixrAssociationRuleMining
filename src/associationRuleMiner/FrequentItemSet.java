package associationRuleMiner;

import java.util.Set;
import java.util.TreeSet;

public class FrequentItemSet {
	public static int nSets = 0;
	public int id;
	FrequentItemSetDB fItemDB;
	Set<Integer> setIndices;
	
	public FrequentItemSet(FrequentItemSetDB fItems, Set<Integer> myIndices){
		fItemDB = fItems;
		setIndices = new TreeSet<Integer>(myIndices);
		id = nSets;
		nSets++;
	}
	

}
