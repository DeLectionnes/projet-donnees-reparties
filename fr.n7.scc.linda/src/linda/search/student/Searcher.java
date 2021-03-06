package linda.search.student;

import linda.*;
import java.util.Arrays;
import java.util.UUID;

public class Searcher implements Runnable {

    private final Linda linda;

    public Searcher(Linda linda) {
        this.linda = linda;
    }

    public void run() {
    	linda.write(new Tuple(Code.Signal, SignalID.SearcherBegin));
        System.out.println("Ready to do a search");
        Tuple treq = linda.read(new Tuple(Code.Request, UUID.class, String.class));
        UUID reqUUID = (UUID)treq.get(1);
        String req = (String) treq.get(2);
        Tuple tv;
        System.out.println("Looking for: " + req);
        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Signal, SignalID.SearchEnd, reqUUID), new CbSearcherDone());
        
        while ((tv = linda.tryTake(new Tuple(Code.Value,  String.class, reqUUID))) != null) {
        	System.err.println("Dans la boucle");
            String val = (String) tv.get(1);
            int dist = getLevenshteinDistance(req, val);
            if (dist < 10) { // arbitrary
                linda.write(new Tuple(Code.Result, reqUUID, val, dist));
            }
        }
        linda.write(new Tuple(Code.Searcher, "done", reqUUID));
        linda.write(new Tuple(Code.Signal, SignalID.SearcherEnd));
        Thread.currentThread().interrupt();
        return;
    }
    
    
    private class CbSearcherDone implements linda.Callback {
    	public void call(Tuple t) {
    		//Pour réinitialiser le chercheur, on décide plutôt d'en recréer un nouveau puis de le détruire
    		Main.startSearcher();
    		linda.write(new Tuple(Code.Signal, SignalID.SearcherEnd));
    		Thread.currentThread().interrupt();
    		return;
    	}
    }
    
    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

}

