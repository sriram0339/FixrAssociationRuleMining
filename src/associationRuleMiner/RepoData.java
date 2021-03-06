package associationRuleMiner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class RepoData {
	public int id;
	public int linenum;
	public String repo;
	public String childHash;
	public String parentHash;
	public String name;
	public String filename;
	public String commitDate;
	public Date formattedDate;
	public List<Integer> indices;
	public List<Boolean> values;
	public long commitTimeMilliSecondsEpoch;
	public int binID;
	public TreeMap<String, List<Integer> > contextToFeatureIDs;
	
	public RepoData(int myId){
		this.id = myId;
		this.linenum = myId;
		repo="";
		childHash="";
		parentHash="";
		name="";
		filename ="";
		commitDate="";
		indices=new ArrayList<Integer>(0);
		values=new ArrayList<Boolean>(0);
		binID=0;
		contextToFeatureIDs = new TreeMap<String, List<Integer> >();
	}
	
	public void splitRepoDataByContextAndAdd(List<RepoData> h){
		// Iterate through the contextToFeatureIDs
		for (Map.Entry<String, List<Integer> > e:contextToFeatureIDs.entrySet()){
			RepoData r = new RepoData(h.size());
			r.setRepo(repo);
			r.filename = this.name;
			r.linenum = this.id;
			r.childHash = this.childHash;
			r.parentHash=this.parentHash;
			r.name = this.name + "::"+e.getKey();
			r.commitDate = this.commitDate;
			r.formattedDate = this.formattedDate;
			r.commitTimeMilliSecondsEpoch = this.commitTimeMilliSecondsEpoch;
			r.binID = this.binID;
			List<Integer> newIndices = e.getValue();
			
			for (int j: newIndices){
				int idx = indices.get(j);
				r.indices.add(idx);
				r.values.add(true);
			}
			h.add(r);
		}
	}
	
	public int numIndices(){
		return indices.size();
	}
	public void setRepo(String val) {
		this.repo = val;
		
	}

	public void setChildHash(String val) {
		this.childHash=val;
		
	}

	public void setParentHash(String val) {
		this.parentHash=val;
		
	}

	public void setName(String val) {
		this.name=val;
		
	}

	public void setIndicesString(String val) {
		// List of comma separated indices
		// Split them into fields and parse each field
		if (val.length() <=0 )
			return;
		//System.out.println(this.name);
		//System.err.println(val);
		Pattern p = Pattern.compile("\\s*,\\s*");
		String [] inds = p.split(val);
		for(String s: inds){
			int i = Integer.parseInt(s);
			if (AlgoParameters.ignoreZeroFeature && i <= 1){
				//System.err.println("Ignored feature # "+i);
			} else {
				indices.add(i);
				//System.err.println("Idx: "+i);
			}
		}
	}
	public void parseContexts(String val) {
		// List of comma separated context lists
		if (val.length () <= 0)
			return;
		//System.err.println(this.linenum+":"+val);
		//System.err.println(indices);
		int n = val.length();
		int nListNests = 0;
 
		List<String> inds = new ArrayList<String>();
		int last = 0;
		for (int i = 0; i < n; ++i){
			switch (val.charAt(i)){
			case ',':
				if (nListNests == 0){
					String tmp = val.substring(last,i);
					inds.add(tmp);
					last = i+1;
				}
				break;
			case '[':
				nListNests++;
				break;
			case ']':
				nListNests --;
				break;					
			}
		}
		if (last < n){
			String tmp = val.substring(last,n);
			inds.add(tmp);
		}
		
		Pattern p = Pattern.compile("\\s*,\\s*");
		int idxCount = 0;
		for (String ctxList: inds){
			int nCtxList = ctxList.length();
			//System.err.println(ctxList);
			assert(ctxList.charAt(0)== '[' && ctxList.charAt(nCtxList-1) ==']');
			String lstString = ctxList.substring(1,nCtxList-1);
			String [] featContextList = p.split(lstString);
			for (String ctx:featContextList){
				addFeatureContext(ctx,idxCount);
				//System.err.println("CTX =>"+ctx + " IDX:" + indices.get(idxCount));
			}
			idxCount = idxCount +1;
		}
		
	}
	
	private void addFeatureContext(String ctx, int idxCount) {
		List<Integer> iList = null;
		if (contextToFeatureIDs.containsKey(ctx)){
			iList = contextToFeatureIDs.get(ctx);
		} else {
			iList = new ArrayList<Integer> ();
			contextToFeatureIDs.put(ctx, iList);
		}
		iList.add(idxCount);
		return;
	}
	
	public void setValuesString(String val){
		// List of comma separated indices
		// Split them into fields and parse each field
		if (val.length() <=0)
			return;
		Pattern p = Pattern.compile("\\s*,\\s*");
		String [] inds = p.split(val);
		for(String s: inds){
			if (s.equals("1.0") || s.equals("1")){
				values.add(true);
			} else {
				System.out.println(">>> "+ s + "<<<"+ val + ">>"+ repo + "<<"+ name + "<<<"+parentHash);
				
				assert(false); // For the time being let us not permit this
				values.add(false);
			}
		}
	}

	public String toString(){
		String str;
		str = "ID: "+ this.id + "\n";
		str = str+"Repo: " + this.repo + "\n";
		str = str + "Name: " + this.name + "\n";
		str = str + "Indices: (length = "+ indices.size() +") ~~ " + (indices.toString()) + "\n";
		str = str + "Values: " + (values.toString()) + "\n";	
		return str;
	}
	
	public int getMaxIndex() {
		int m = 0;
		for(int j: indices){
			if (j > m)
				m = j;
		}
		return m;
	}
	
	public void addIndexToSet(Set<Integer> s, TreeMap<Integer, TreeSet<Integer> > m){
		for (int j: indices){
			s.add(j);
			TreeSet<Integer> mj = null;
			if (m.containsKey(j)){
				mj = m.get(j);
			} else {
				mj = new TreeSet<Integer>();
			}
			mj.add(this.id);
			m.put(j, mj);
		}
	}
	public void setCommitDate(String val, CommitDateRanges cdr) {
		this.commitDate = val;
		try {
			DateFormat formatter = new SimpleDateFormat(AlgoParameters.dateFormatInJSON);
			formattedDate = formatter.parse(val);
			this.commitTimeMilliSecondsEpoch = formattedDate.getTime();
			this.binID=cdr.addCommitToBin(this.commitTimeMilliSecondsEpoch);
			
		//DEBUG MSG:	System.out.println("Commit Date " + val + " Epoch Time: "+ this.commitTimeMilliSecondsEpoch);
		} catch(ParseException e){
			System.out.println("Badly formatted date "+val+" in object "+ this.name);
		}
	}
	
	public long getTime(){
		return this.commitTimeMilliSecondsEpoch;
	}
	public int getCommitBinID(){
		return binID;
	}
	
	public String getCommitURL(){
		String str = "<tr><td> Commit URL: <td> <a href = \" http://github.com/"+this.repo+"/commit/"+this.childHash + "?diff=split\", target=\"_new\">";
		str = str + this.name + "(" + this.childHash +")";
		str = str + "</a> </tr>\n";
		
		return str;
	}
	
	public JSONObject dumpJSON(){
		JSONObject rValue = new JSONObject();
		try {
			rValue.put("id",id );
			rValue.put("repoName", this.repo);
			rValue.put("childHash", this.childHash);
			rValue.put("name", this.name);
			rValue.put("commitDate", commitDate);
			return rValue;
		} catch (JSONException e){
			System.out.println("Fatal: JSON exeception");
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalArgumentException e){
			System.err.println("Date Formatting failed:" + commitDate);
			e.printStackTrace();
		}
		// Should never reach here.
		assert(false);
		return null;
	}
	
}
