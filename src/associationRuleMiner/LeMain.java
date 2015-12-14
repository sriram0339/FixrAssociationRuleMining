package associationRuleMiner;

import java.util.List;

public class LeMain {

	public static void main(String[] args) {
		if (args.length <= 1){
			System.out.println("Usage: java associationRuleMiner.LeMain <clustringJSONFile> <FeaturesDescriptionFile>");
			return;
		}
		System.out.println("Running on clustring file:"+args[0] + " Features description file:" + args[1]);
		ReadJSONFileData rjfd = new ReadJSONFileData(args[0]);
		ReadFeatureDescriptors featDesc = new ReadFeatureDescriptors(args[1]);
		featDesc.readDataFromFile();
		List<RepoData> h = rjfd.getRepoData();
		String [] featureNames = featDesc.getFeatureNames();
		//for (RepoData r: h){
		//	System.out.println(r.toString());
		//	System.out.println("----------");
	//	}
		
		FrequentItemSetDB fItems = new FrequentItemSetDB(h,featureNames);
		fItems.buildFrequentItemSets(AlgoParameters.frequentItemSetFreqCutoff);
		fItems.obtainAssociationRulesFromFrequentItemSets(AlgoParameters.associationRuleStrengthCutoff);
	}

}
