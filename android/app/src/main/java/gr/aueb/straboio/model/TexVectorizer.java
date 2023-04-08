package gr.aueb.straboio.model;

import android.content.Context;

import org.pytorch.Tensor;

import java.util.List;
import java.util.Map;

public class TexVectorizer {

    public enum Mode {
        WORD, CHAR
    }

    private Mode mode;
    private Vocabulary vocabulary;
    private Context c;

    public TexVectorizer(Context c, Mode mode) {
        this.c = c;
        this.mode = mode;
        this.vocabulary = new Vocabulary();
    }

    public TexVectorizer(Context c, Mode mode, String pathToExistingVocab){
        this(c, mode);
        this.loadExistingVocab(pathToExistingVocab);
    }

    /**
     * Loads existing vocabulary from external file:
     * @param pathToExistingVocab the path to the external vocabulary.
     */
     private void loadExistingVocab(String pathToExistingVocab){
        this.vocabulary.load(this.c, pathToExistingVocab);
    }

    /**
     * Builds the vocabulary from a corpus of sentences. The words get encoded by
     * count of appearances in the data.
     * @param corpus : A list of sentences as strings.
     */
    public void buildVocab(List corpus){
        buildVocab(corpus, 25000);
    }

    /**
     * Builds the vocabulary from a corpus of sentences. The words get encoded by
     * count of appearances in the data.
     * @param corpus : A list of sentences as strings.
     * @param MAX_SIZE : the maximum allowed size of the corpus.
     */
    public void buildVocab(List<String> corpus, final int MAX_SIZE){
        TokenCounter tkcount = new TokenCounter();
        this.vocabulary.insertToken("<pad>");

        switch(mode){
            case WORD:
                // In the case of words, we remove punctuation and split on whitespaces
                for (String line : corpus){
                    // Remove punctuation
                    line.replaceAll("["+Vocabulary.PUNCTUATION+"]", "");
                    // Split the line in whitespaces to get the words & update counts
                    tkcount.count(line.split("["+Vocabulary.WHITESPACE+"]"));
                }
                break;
            case CHAR:
                // Here we do not do any regularization, and split on every character.
                for (String line : corpus)
                    for (String character : line.split(""))
                        tkcount.count(character);
                break;
        }
        // Add the most frequent tokens to the vocabulary.
        for(Map.Entry<String, Integer> entry : tkcount.mostCommon(MAX_SIZE)){
            this.vocabulary.insertToken(entry.getKey());
        }
        // Add [SOS] token
        this.vocabulary.insertToken("<s>");
    }

    /**
     * Takes a sentence and returns its encoding, based on the vocabulary, to be used for inference.
     * @param sequence The sentence to be encoded.
     * @return Encoded sentence in form of a torch.Longtensor object.
     */
    public Tensor toTensor(String sequence){
        String[] tokens = new String[0];
        switch (this.mode){
            case WORD:
                tokens = sequence
                        .replaceAll("["+Vocabulary.PUNCTUATION+"]", "")
                        .split("["+Vocabulary.WHITESPACE+"]");
                break;
            case CHAR:
                tokens = sequence.split("");
                break;
        }
        long[] vectorized_tokens = new long[tokens.length];
        for (int i=0; i<tokens.length; i++){
            vectorized_tokens[i] = this.vocabulary.index(tokens[i]);
        }

        return Tensor.fromBlob(vectorized_tokens, new long[]{ vectorized_tokens.length });
    }

    public int getIndex(String tok){
        return this.vocabulary.index(tok);
    }
}