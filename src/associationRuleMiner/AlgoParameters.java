package associationRuleMiner;

public class AlgoParameters {
	
	// frequentItemSetFreqCutoff
	// This is a cutoff parameter for regarding an itemset as "frequent".
	// Set this number to be reasonably large so that if a series of methods appear in <frequentItemSetFreqCutOff>
	// commits, then it is deemed statistically significant.
	
	public static int frequentItemSetFreqCutoff=2500;
	
	// associationRuleStrengthCutoff
	// This is a cutoff parameter for the strength of an association rule
	public static double associationRuleStrengthCutoff=0.9;
	
	public static String startDate = "01/01/08";
	public static String endDate = "01/01/16";
	public static int nBins = 24;
	
}
