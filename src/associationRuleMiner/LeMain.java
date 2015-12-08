package associationRuleMiner;

import java.util.List;

public class LeMain {

	public static void main(String[] args) {
		if (args.length <= 0){
			System.out.println("Usage: java associationRuleMiner.LeMain <fileName>");
			return;
		}
		System.out.println("Running on file:"+args[0]);
		ReadJSONFileData rjfd = new ReadJSONFileData(args[0]);
		List<RepoData> h = rjfd.getRepoData();
		for (RepoData r: h){
			System.out.println(r.toString());
			System.out.println("----------");
		}
		
		FrequentItemSetDB fItems = new FrequentItemSetDB(h);
		fItems.buildFrequentItemSets(0.05);
		fItems.obtainAssociationRulesFromFrequentItemSets(0.8);
	}

}
