package associationRuleMiner;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CommitDateRanges {
	// Manage ranges of dates.
	Date firstDate;
	Date lastDate;
	int nBins;
	long [] dateRanges;
	int [] numCommitsInRange;
	public CommitDateRanges(Date fDate, Date lDate, int nB){
		firstDate = fDate;
		lastDate = lDate;
		nBins = nB;
		dateRanges = new long[nBins];
		numCommitsInRange = new int[nBins];
		long startTime = fDate.getTime();
		long endTime = lDate.getTime();
		long diff = endTime - startTime;
	
		long step = diff/(long) nBins;
	
		for (int i=0; i < nBins; ++i){
			dateRanges[i] = startTime+  (long) ( (double) i * step ) ;
			numCommitsInRange[i] = 0;
		}
		
		
	}
	
	public int addCommitToBin(long cTime){
		int i = getDateBin(cTime);
		numCommitsInRange[i] += 1;
		return i;
	}
	
	public int getDateBin(long cTime){
		for (int i = 0; i < nBins; ++i){
			if (cTime <= dateRanges[i]) return i;
		}
		return nBins-1;
	}

	public void prettyPrintDateRanges(){
		System.out.println("Date Ranges");
		System.out.println("------------------");
		for (int i = 0; i < nBins; ++i){
			System.out.println(i + " ---> " +  getBinDate(i) + "," + numCommitsInRange[i]);
		}
		System.out.println("------------------");
	}
	
	public int getNumBins(){
		return nBins;
	}

	public String getBinDate(int i) {
		assert(i >= 0 && i <nBins);
		Date cDate = new Date (dateRanges[i]);
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String dateFormatted = formatter.format(cDate);
		return dateFormatted;
	}

	public int getNumCommitsForRange(int i) {
		assert(i >= 0 && i < nBins );
		return numCommitsInRange[i];
	}

	public String getDateLabels() {
		String delim=", ";
		int i = 0;
		String str = "\"< "+getBinDate(i)+"\"";
		for (i=1; i < nBins-1;++i){
			str = str + delim + "\""+getBinDate(i)+"\"";
		}
		str = str + delim + "\"> "+getBinDate(nBins-1)+"\"";
		return str;
	}
	
	
}
