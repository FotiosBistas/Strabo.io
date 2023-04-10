package gr.aueb.straboio.model;

import android.util.Pair;

import org.pytorch.IValue;
import org.pytorch.Tensor;

import java.util.ArrayList;
import java.util.Objects;

public class LSTMState implements BeamSearchState {

    private ArrayList<String> translated;
    private ArrayList<String> remaining;
    private Pair hidden;
    private IValue out;
    private float score;

    public LSTMState(ArrayList<String> translated, ArrayList<String> remaining, Pair hidden, IValue out, float score) {
        this.translated = translated;
        this.remaining = remaining;
        this.hidden = hidden;
        this.out = out;
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    public ArrayList<String> getRemaining() {
        return remaining;
    }

    public ArrayList<String> getTranslated() {
        return translated;
    }

    public Pair getHidden() {
        return hidden;
    }

    public IValue getOut() {
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LSTMState lstmState = (LSTMState) o;
        return Float.compare(lstmState.score, score) == 0
                && Objects.equals(translated, lstmState.translated)
                && Objects.equals(remaining, lstmState.remaining)
                && Objects.equals(hidden, lstmState.hidden)
                && Objects.equals(out, lstmState.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(translated, remaining, hidden, out, score);
    }
}
