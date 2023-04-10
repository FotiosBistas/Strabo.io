package gr.aueb.straboio.model;

import android.util.Log;
import android.util.Pair;

import org.pytorch.IValue;
import org.pytorch.Tensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import gr.aueb.straboio.model.functions.ActivationFunction;

public class LanguageModelLSTM implements LanguageModel{

    public static final Map<String,String[]> GREEKLISH_TO_GREEK =  Map.ofEntries(
            Map.entry("A", new String[]{
                    "Α", "Ά"
            }),
            Map.entry("Ai", new String[]{
                    "Αι", "Αί"
            }),
            Map.entry("B", new String[]{
                    "Β", "Μπ"
            }),
            Map.entry("D", new String[]{
                    "Δ", "Ντ"
            }),
            Map.entry("E", new String[]{
                    "Ε", "Αι", "Έ", "Αί"
            }),
            Map.entry("Ει", new String[]{
                    "Ει", "Εί"
            }),
            Map.entry("F", new String[]{
                    "Φ"
            }),
            Map.entry("G", new String[]{
                    "Γ"
            }),
            Map.entry("H", new String[]{
                    "Η", "X", "Ή"
            }),
            Map.entry("I", new String[]{
                    "Η", "Ι", "Υ", "Ει", "Οι", "Υι", "Ή", "Ί", "Ύ", "Εί", "Οί", "Υί"
            }),
            Map.entry("K", new String[]{
                    "Κ"
            }),
            Map.entry("Ks", new String[]{
                    "Ξ"
            }),
            Map.entry("L", new String[]{
                    "Λ"
            }),
            Map.entry("M", new String[]{
                    "Μ"
            }),
            Map.entry("Mp", new String[]{
                    "Μπ"
            }),
            Map.entry("N", new String[]{
                    "Ν"
            }),
            Map.entry("Nt", new String[]{
                    "Ντ"
            }),
            Map.entry("O", new String[]{
                    "Ο", "Ω", "Ό", "Ώ"
            }),
            Map.entry("Oi", new String[]{
                    "Οι", "Οί"
            }),
            Map.entry("Ou", new String[]{
                    "Ου", "Ού"
            }),
            Map.entry("P", new String[]{
                    "Π"
            }),
            Map.entry("Ps", new String[]{
                    "Ψ"
            }),
            Map.entry("Q", new String[]{
                    "Θ"
            }),
            Map.entry("R", new String[]{
                    "Ρ"
            }),
            Map.entry("S", new String[]{
                    "Σ"
            }),
            Map.entry("T", new String[]{
                    "Τ"
            }),
            Map.entry("Th", new String[]{
                    "Θ"
            }),
            Map.entry("U", new String[]{
                    "Θ", "Ου", "Ού", "Υ", "Ύ"
            }),
            Map.entry("V", new String[]{
                    "Β"
            }),
            Map.entry("W", new String[]{
                    "Ω", "Ώ"
            }),
            Map.entry("X", new String[]{
                    "Ξ", "Χ"
            }),
            Map.entry("Y", new String[]{
                    "Υ", "Ύ"
            }),
            Map.entry("Yi", new String[]{
                    "Υι", "Υί"
            }),
            Map.entry("Z", new String[]{
                    "Ζ"
            }),
            Map.entry("a", new String[]{
                    "α", "ά"
            }),
            Map.entry("ai", new String[]{
                    "αι", "αί"
            }),
            Map.entry("b", new String[]{
                    "β", "μπ"
            }),
            Map.entry("d", new String[]{
                    "δ", "ντ"
            }),
            Map.entry("e", new String[]{
                    "ε", "αι", "έ", "αί"
            }),
            Map.entry("ei", new String[]{
                    "ει", "εί"
            }),
            Map.entry("f", new String[]{
                    "φ"
            }),
            Map.entry("g", new String[]{
                    "γ"
            }),
            Map.entry("h", new String[]{
                    "η", "χ", "ή"
            }),
            Map.entry("i", new String[]{
                    "η", "ι", "υ", "ει", "οι", "υι", "ή", "ί", "ύ", "εί", "οί", "υί"
            }),
            Map.entry("k", new String[]{
                    "κ"
            }),
            Map.entry("ks", new String[]{
                    "ξ"
            }),
            Map.entry("l", new String[]{
                    "λ"
            }),
            Map.entry("m", new String[]{
                    "μ"
            }),
            Map.entry("mp", new String[]{
                    "μπ"
            }),
            Map.entry("n", new String[]{
                    "ν"
            }),
            Map.entry("nt", new String[]{
                    "ντ"
            }),
            Map.entry("o", new String[]{
                    "ο", "ω", "ό", "ώ"
            }),
            Map.entry("oi", new String[]{
                    "οι", "οί"
            }),
            Map.entry("ou", new String[]{
                    "ου", "ού"
            }),
            Map.entry("p", new String[]{
                    "π"
            }),
            Map.entry("ps", new String[]{
                    "ψ"
            }),
            Map.entry("r", new String[]{
                    "ρ"
            }),
            Map.entry("s", new String[]{
                    "σ","ς"
            }),
            Map.entry("t", new String[]{
                    "τ"
            }),
            Map.entry("th", new String[]{
                    "θ"
            }),
            Map.entry("u", new String[]{
                    "υ", "θ", "ου", "ύ", "ού"
            }),
            Map.entry("ui", new String[]{
                    "υι", "υί"
            }),
            Map.entry("v", new String[]{
                    "β"
            }),
            Map.entry("w", new String[]{
                    "ω", "ώ"
            }),
            Map.entry("x", new String[]{
                    "ξ", "χ"
            }),
            Map.entry("y", new String[]{
                    "υ", "ύ"
            }),
            Map.entry("z", new String[]{
                    "ζ"
            })
    );
    private Model model;
    private TexVectorizer texVectorizer;
    private ActivationFunction acfunc;
    private int beams;

    public LanguageModelLSTM(Model model, TexVectorizer texVectorizer, int beams) {
        this.model = model;
        this.texVectorizer = texVectorizer;
        this.acfunc = acfunc;
        this.beams = beams;
    }

    @Override
    public String translate(String sentence) {
        ArrayList<String> sentenceSplit = new ArrayList<>(Arrays.asList(sentence.split("")));
        ArrayList<String> translated = new ArrayList<String>();

        Tensor startInput = this.texVectorizer.toTensor("<s>");
        IValue[] outputs = this.model.forward(IValue.from(startInput));
        LSTMState initialState = new LSTMState(
                translated,
                sentenceSplit,
                new Pair(outputs[1], outputs[2]),
                outputs[0],
                0.f
        );


        ArrayList<BeamSearchState> states = new ArrayList<>();
        states.add(initialState);

        for(String token : sentenceSplit){

            ArrayList<BeamSearchState> candidates = new ArrayList<>();
            for(BeamSearchState state : states){
                ArrayList<BeamSearchState> freshCandidates = this.getCandidates(state);
                candidates.addAll(freshCandidates);
            }

            ArrayList<BeamSearchState> tempCandSet = new ArrayList<>();
            for(BeamSearchState state : candidates){
                if(!tempCandSet.contains(state))
                    tempCandSet.add(state);
            }
            candidates = tempCandSet;

            ArrayList<BeamSearchState> bestCandidates = new ArrayList<>();
            for(int i=0; i<beams; i++){
                if(!candidates.isEmpty()){
                    float[] probs = new float[candidates.size()];
                    int j = 0;
                    for(BeamSearchState cand : candidates){
                        probs[j] = ((LSTMState) cand).getScore();
                        j++;
                    }
                    BeamSearchState bestCandidate = candidates.remove(this.indexOfMax(probs));
                    bestCandidates.add(bestCandidate);
                }
            }

            states = bestCandidates;
        }

        float[] probs = new float[states.size()]; int j = 0;
        for(BeamSearchState state : states){
            probs[j] = ((LSTMState) state).getScore();
            j++;
        }
        LSTMState sent = (LSTMState) states.remove(this.indexOfMax(probs));
        StringBuilder translation = new StringBuilder("");
        for(String s : sent.getTranslated()){
            translation.append(s);
        }
        return translation.toString();
    }

    private ArrayList<BeamSearchState> getCandidates(BeamSearchState state) {
        LSTMState lstmState = (LSTMState) state;
        ArrayList<BeamSearchState> candidates = new ArrayList<>();

        if(lstmState.getRemaining().isEmpty()){
            candidates.add(state);
            return candidates;
        }

        for(short length = 1; length<=2; length++){
            if(lstmState.getRemaining().size() >= length){
                String[] replacements;
                if(length == 2){
                    String token = lstmState.getRemaining().get(0) + lstmState.getRemaining().get(1);
                    Log.d("GET_CAND", "token:"+token);
                    replacements = GREEKLISH_TO_GREEK.containsKey(token)
                    ? GREEKLISH_TO_GREEK.get(token)
                    : new String[] {};
                    Log.d("GET_CAND", ""+lstmState.getRemaining().toString());
                    Log.d("GET_CAND", "length: "+length+", rem_size: "+lstmState.getRemaining().size());
                    Log.d("GET_CAND", "replacemens: "+replacements.length);
                } else {
                    String token = lstmState.getRemaining().get(0);
                    replacements = GREEKLISH_TO_GREEK.containsKey(token)
                            ? GREEKLISH_TO_GREEK.get(token)
                            : new String[] { token };
                }

                for(String fthoggos : replacements){
                    IValue h_n = (IValue) lstmState.getHidden().first;
                    IValue c_n = (IValue) lstmState.getHidden().second;
                    IValue out = lstmState.getOut();
                    float score = lstmState.getScore();
                    ArrayList<String> translatedTokens = new ArrayList<>();
                    for(String token : fthoggos.split("")){

                        IValue probs = ActivationFunction.logSoftmax(out);
                        int idx = this.texVectorizer.getIndex(token);
                        score = score + probs.toTensor().getDataAsFloatArray()[idx];

                        Tensor input = this.texVectorizer.toTensor(token);

                        IValue[] outputs = this.model.forward(
                                IValue.from(input),
                                h_n,
                                c_n
                        );
                        out = outputs[0];
                        h_n = outputs[1];
                        c_n = outputs[2];
                        translatedTokens.add(token);
                    }

                    ArrayList<String> translatedTokensSum = new ArrayList<>(lstmState.getTranslated());
                    translatedTokensSum.addAll(translatedTokens);
                    List<String> temp = lstmState.getRemaining().subList(length, lstmState.getRemaining().size());
                    ArrayList<String> remaining = new ArrayList<>();
                    for(String rem : temp){
                        remaining.add(rem);
                    }
                    BeamSearchState newCandidate = new LSTMState(
                            translatedTokensSum,
                            remaining,
                            new Pair(h_n, c_n),
                            out,
                            score
                    );
                    candidates.add(newCandidate);

                }

            }
        }

        return candidates;
    }

    private int indexOfMax(float[] arr) {
        int maxIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }


}
