package gr.aueb.straboio.keyboard.support;

import android.util.Pair;
import java.util.ArrayList;

public class Buffer {

    private ArrayList<Pair<String, Integer>> contents;
    private String output;

    public Buffer() {
        this.contents = new ArrayList<>();
        this.output = "";
    }

    public synchronized void flush(){
        this.contents.clear();
        this.output = "";
    }

    public synchronized void push(String character, int cursorPositionAfterInsertionOrDeletion){
        this.contents.add(
          new Pair<>(
                  character,
                  cursorPositionAfterInsertionOrDeletion
          )
        );
    }

    public synchronized void update(String outputedText){
        this.output = outputedText;
    }

    @Override
    public String toString() {
        String toReturn = "";
        for(Pair p : this.contents){
            toReturn = toReturn + "{" + p.first + ", '" + p.second + "', " + "}\n";
        }
        toReturn = toReturn + "Output: '" + this.output + "'";
        return toReturn;
    }
}
