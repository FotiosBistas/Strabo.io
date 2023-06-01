package gr.aueb.straboio.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class TokenCounter {
    private HashMap counters;

    public TokenCounter(){
        this.counters = new HashMap<String, Integer>();
    }

    public void count(String token){
        if(this.counters.containsKey(token)){
            this.counters.put(token, ((Integer) this.counters.get(token)) + 1);
        } else {
            this.counters.put(token, 1);
        }
    }

    public void count(String[] tokens){
        for (String token : tokens){
            this.count(token);
        }
    }

    public ArrayList<Map.Entry<String, Integer>> mostCommon(int k){
        PriorityQueue queue = new PriorityQueue<Map.Entry<String, Integer>>(k,
                (entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()));

        for (Object entry : this.counters.entrySet()) {
            queue.offer(entry);
            if (queue.size() > k) {
                queue.poll();
            }
        }

        // Create a HashMap from the k biggest entries
        ArrayList<Map.Entry<String, Integer>> result = new ArrayList<Map.Entry<String, Integer>>();
        while (!queue.isEmpty()) {
            result.add((Map.Entry<String, Integer>) queue.poll());
        }

        return result; // k most common tokens
    }

}