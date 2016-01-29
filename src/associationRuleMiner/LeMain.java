package associationRuleMiner;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LeMain {

	public static void main(String[] args) {
		try {
			// ====
			// Create date ranges
			long startTime = System.nanoTime();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");

			Date sDate = sdf.parse(AlgoParameters.startDate);
			Date eDate = sdf.parse(AlgoParameters.endDate);
			CommitDateRanges cdr = new CommitDateRanges(sDate, eDate, AlgoParameters.nBins);

			// ====

			System.out.println("Running on clustring file:" + AlgoParameters.repoDataJSONFilename
					+ " Features description file:" + AlgoParameters.featuresFilename);
			ReadJSONFileData rjfd = new ReadJSONFileData(AlgoParameters.repoDataJSONFilename, cdr);
			ReadFeatureDescriptors featDesc = new ReadFeatureDescriptors(AlgoParameters.featuresFilename);
			
			featDesc.readDataFromFile();
			List<RepoData> h = rjfd.getRepoData();
			String[] featureNames = featDesc.getFeatureNames();
			// -- Debugging message
			System.out.println("Start of date range, number of commits in this period");
			cdr.prettyPrintDateRanges();
			long timeToRead = System.nanoTime();
			
			FrequentItemSetDB fItems = new FrequentItemSetDB(h, featureNames);
			fItems.buildFrequentItemSets(AlgoParameters.frequentItemSetFreqCutoff);
			fItems.obtainAssociationRulesFromFrequentItemSets(AlgoParameters.associationRuleStrengthCutoff, cdr);
			
			long timeToMineRules = System.nanoTime();
			
			int numClustersForRules = 1;
			List<Integer> clusterBoundaries = new ArrayList<Integer>();
			clusterBoundaries.add(0);
			
			if (AlgoParameters.writeAssociationRuleGraph) {
				AssociationRuleGraph aGraph = new AssociationRuleGraph(fItems.associationRuleList);
				aGraph.dotPrintAssociationRuleGraph(AlgoParameters.assocDotFileName);
				//aGraph.visualizeGraphAndDumpToFile("a.png");
				Set<Set<Integer>> ss = aGraph.clusterComponents();
				int clCount = 0;
				int ruleCount = 0;
				for (Set<Integer> sj : ss) {
					System.out.println("Cluster ID : " + clCount + "(" + sj.size() + ")");
					if (ruleCount > 0)
						clusterBoundaries.add(ruleCount);
					ruleCount = ruleCount + sj.size();
					clCount++;
					
					/*for (int j : sj) {
						AssociationRule ruleJ = fItems.associationRuleList.get(j);
						System.out.println("\t" + ruleJ.toString());
					}*/
				}
				
				Collections.sort(fItems.associationRuleList);
			}

			// fItems.printAllAssociationRules();
			fItems.htmlDumpAllRules(AlgoParameters.fileStem,clusterBoundaries);

			// -- JSON Dump All
			try {
				JSONObject allData = new JSONObject();
				// First print all repository data out
				/*
				 * JSONArray a = new JSONArray(); for (RepoData r: h){
				 * JSONObject o = r.dumpJSON(); a.put(o); }
				 * allData.put("repoData", a);
				 */
				// Next, print all feature data
				if (AlgoParameters.writeJSON) {

					JSONArray feats = new JSONArray();
					for (String s : featureNames) {
						feats.put(s);
					}
					allData.put("featureList", feats);
					JSONArray assocRules = new JSONArray();
					for (AssociationRule aRule : fItems.associationRuleList) {
						assocRules.put(aRule.dumpJSONObject());
					}
					allData.put("associationRules", assocRules);

					PrintWriter fstream = new PrintWriter(AlgoParameters.jsonFileName, "UTF-8");
					allData.write(fstream);
					fstream.close();
				}
				
				System.out.println("Rules Mined = "+ fItems.associationRuleList.size());
				System.out.println("Time elapsed (association rule mining): "+ (timeToMineRules - timeToRead)/1000000.0 );
				
			} catch (JSONException e) {
				System.err.println("Failed to dump JSON -- bailing.");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not write JSON -- bailing.");
				e.printStackTrace();
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
