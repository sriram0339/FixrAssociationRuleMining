package associationRuleMiner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LeMain {

	public static void main(String[] args) {
		if (args.length <= 1){
			System.out.println("Usage: java associationRuleMiner.LeMain <clustringJSONFile> <FeaturesDescriptionFile>");
			return;
		}
		try {
			//====
			// Create date ranges
			SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yy");

			Date sDate = sdf.parse(AlgoParameters.startDate);
			Date eDate = sdf.parse(AlgoParameters.endDate);
			CommitDateRanges cdr = new CommitDateRanges(sDate,eDate,AlgoParameters.nBins);
			
			//====
			
			System.out.println("Running on clustring file:"+args[0] + " Features description file:" + args[1]);
			ReadJSONFileData rjfd = new ReadJSONFileData(args[0],cdr);
			ReadFeatureDescriptors featDesc = new ReadFeatureDescriptors(args[1]);
			featDesc.readDataFromFile();
			List<RepoData> h = rjfd.getRepoData();
			String [] featureNames = featDesc.getFeatureNames();
			// -- Debugging message
			System.out.println("Start of date range, number of commits in this period");
			cdr.prettyPrintDateRanges();
			

			FrequentItemSetDB fItems = new FrequentItemSetDB(h,featureNames);
			fItems.buildFrequentItemSets(AlgoParameters.frequentItemSetFreqCutoff);
			fItems.obtainAssociationRulesFromFrequentItemSets(AlgoParameters.associationRuleStrengthCutoff,cdr);
			fItems.printAllAssociationRules();
			fItems.htmlDumpAllRules(AlgoParameters.fileStem);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
