package associationRuleMiner;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeMain {

	public static void main(String[] args) {
		if (args.length <= 1){
			System.out.println("Usage: java associationRuleMiner.LeMain <clustringJSONFile> <FeaturesDescriptionFile>");
			return;
		}
		try {
			//====
			// Create date ranges
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

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
			
			// -- JSON Dump All
			try {
				JSONObject allData = new JSONObject();
				// First print all repository data out
				/*JSONArray a = new JSONArray();
				for (RepoData r: h){
					JSONObject o = r.dumpJSON();
					a.put(o);
				}
				allData.put("repoData", a);*/
				// Next, print all feature data
				JSONArray feats = new JSONArray();
				for (String s: featureNames){
					feats.put(s);
				}
				allData.put("featureList", feats);
				JSONArray assocRules = new JSONArray();
				for (AssociationRule aRule: fItems.associationRuleList){
					assocRules.put(aRule.dumpJSONObject());
				}
				allData.put("associationRules", assocRules);
				
				PrintWriter fstream = new PrintWriter(AlgoParameters.jsonFileName, "UTF-8"); //true tells to append data.
				allData.write(fstream);
				fstream.close();
				
			}catch (JSONException e){
				System.err.println("Failed to dump JSON -- bailing.");
				e.printStackTrace();
			}catch (IOException e){
				System.err.println("Could not write JSON -- bailing.");
				e.printStackTrace();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
