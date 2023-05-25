package gr.aueb.straboio.keyboard.support;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Sentence {


    private ArrayList<Word> words;

    /**
     * Data structure which holds Word objects while the user types their input.
     */
    public Sentence() {
        this.words = new ArrayList<>();
    }

    /**
     * Inserts a character in the sentence at the given cursor position.
     * @param character the character to be inserted
     * @param newCursorPosition the cursor position after the insertion of the character
     */
    public void insertChar(String character, int newCursorPosition){

        if(character.equals(" ")){
            insertSPACE(newCursorPosition);
            return;
        }

        // remove empty words...
        removeEmptyWords();

        if(this.words.isEmpty()){
            Word newWord = new Word(
                    new StringBuilder(character),
                    new StringBuilder(character),
                    newCursorPosition,
                    newCursorPosition + character.length() + -1
            );
            this.words.add(newWord);
            return;
        }

        // First we need to check in which one of the words the insertion has occured:
        for(int i=0; i<this.words.size(); i++){
            Word target = this.words.get(i);

            if(target.inRange(newCursorPosition - 1) || Math.abs(newCursorPosition - target.getBoundStart()) == 0){
                // Character position belongs in the range of word w, so we insert the char:
                if(target.inTheSameLanguage()) {
                    target.insertBoth(character, newCursorPosition);
                } else {
                    target.insert(character, newCursorPosition);
                }
                // Update the bounds of the words that follow:
                for(int j=i+1; j<this.words.size(); j++){
                    this.words.get(j).shift(character.length());
                }
                return;
            }

            Word nextTarget = (i+1 < this.words.size()) ? this.words.get(i+1) : null;

            // Check if the next word must contain the newly inserted character:
            if(nextTarget!=null && Math.abs(newCursorPosition - nextTarget.getBoundStart()) == 0){
                // Character position belongs in the starting position of next word w_i+1, so we insert the char there:
                if(nextTarget.inTheSameLanguage()) {
                    nextTarget.insertBoth(character, newCursorPosition);
                } else {
                    nextTarget.insert(character, newCursorPosition);
                }
                // Update the bounds of the words that follow:
                for(int j=i+2; j<this.words.size(); j++){
                    this.words.get(j).shift(character.length());
                }
                return;
            }

            // else if there are no words matching up to that cursor position, this means that
            // a new word must be inserted.

            if((target.getBoundEnd() < newCursorPosition - 1) && (
                    nextTarget == null || (nextTarget.getBoundStart() > newCursorPosition - 1)
            )) {
                Word newWord = new Word(
                        new StringBuilder(character),
                        new StringBuilder(character),
                        newCursorPosition,
                        newCursorPosition + character.length() + -1
                );
                this.words.add(i+1, newWord);
                // Update the bounds of the words that follow:
                for(int j=i+2; j<this.words.size(); j++){
                    this.words.get(j).shift(character.length()+1);
                }
                return;
            }
        }
    }

    /**
     * Simulates the insertion of a SPACE (' ') character inside the sentence.
     * In reality, if the space is inserted inside a word, the word gets split in two smaller words.
     * Otherwise it is ignored.
     * @param newCursorPosition the cursor position after the insertion of the space character
     */
    private void insertSPACE(int newCursorPosition){

        // remove empty words...
        removeEmptyWords();

        if(this.words.isEmpty()){
            // ignore...
            return;
        }
        System.out.println(this);
        // First we need to check in which one of the words the separation by space has occured:
        for(int i=0; i<this.words.size(); i++){
            Word target = this.words.get(i);
            if(target.inRange(newCursorPosition)){
                // Space Character position belongs in the range of word w, so we split the word:
                target = this.words.remove(i);
                Word firstPart = new Word(
                        new StringBuilder(target.getWordRaw().subSequence(0, newCursorPosition - target.getBoundStart())),
                        new StringBuilder(target.getWordTranslated().subSequence(0, newCursorPosition - target.getBoundStart())),
                        target.getBoundStart(),
                        newCursorPosition - 1
                );

                this.words.add(i, firstPart);

                target.setWordRaw(
                        new StringBuilder(target.getWordRaw().subSequence(newCursorPosition - target.getBoundStart(), target.getWordRaw().length()))
                );
                target.setWordTranslated(
                        new StringBuilder(target.getWordTranslated().subSequence(newCursorPosition - target.getBoundStart(), target.getWordTranslated().length()))
                );
                target.setBoundStart(newCursorPosition);
                target.setBoundEnd(target.getBoundEnd());

                if(i+1<this.words.size()){
                    this.words.add(i+1, target);
                } else {
                    this.words.add(target);
                }

                for(int j=i+1; j<this.words.size(); j++)
                    this.words.get(j).shift();
                return;
            }
        }
    }

    /**
     * Inserts an entire word in the sentence. Method typically used when the AI assist is turned ON.
     * @param word the word to be inserted in the sentence
     */
    public void insertWord(Word word){

        // remove empty words...
        removeEmptyWords();

        if(this.words.isEmpty()){
            this.words.add(word);
            return;
        }

        // First we need to check what the word's position is in the sentence:
        for(int i=0; i<this.words.size(); i++){
            Word target = this.words.get(i);
            Word nextTarget = (i+1 < this.words.size()) ? this.words.get(i+1) : null;

            if(word.getBoundStart() > target.getBoundEnd()) {
                if(nextTarget==null || word.getBoundStart() <= nextTarget.getBoundStart()){
                    this.words.add(i + 1, word);
                    // Update the bounds of the words that follow:
                    if(i+2<this.words.size())
                        this.words.get(i+2).shift(); // add space
                    for (int j = i + 2; j < this.words.size(); j++) {
                        this.words.get(j).shift(word.length());
                    }
                    return;
                }

            }
        }
    }

    /**
     * Deletes character at given position inside the sentence
     * @param newCursorPosition cursor position after the deletion of the character
     */
    public void delete(int newCursorPosition){

        // remove empty words...
        removeEmptyWords();
        // First we need to check in which one of the words the deletion has occured:
        for(int i=0; i<this.words.size(); i++){
            Word target = this.words.get(i);
            if(target.inRange(newCursorPosition + 1)){
                // deleted character position belongs in the range of word w, so we delete the char:
                if(target.inTheSameLanguage()) {
                    target.deleteBoth(newCursorPosition);
                } else {
                    target.delete(newCursorPosition);
                }
                // Update the bounds of the words that follow:
                for(int j=i+1; j<this.words.size(); j++){
                    this.words.get(j).shift(-1);
                }
                // remove empty words...
                removeEmptyWords();
                return;
            }
        }
    }

    /**
     * Removes words that are classified as empty.
     */
    private void removeEmptyWords() {
        for(int i=0; i<this.words.size(); i++){
            if(this.words.get(i).isEmpty()){
                // Remove word
                this.words.remove(i);
                for (int j=i; j<this.words.size(); j++){
                    this.words.get(j).shift(-1);
                }
            }
        }
    }

    /**
     * Empties the sentence of words.
     */
    public void erase(){
        this.words.clear();
    }

    public boolean isEmpty(){
        return this.words.isEmpty();
    }

    /**
     * Constructs a JSON object of the sentence containing the constructed raw sentence and
     * the constructed translated version.
     * @return JSON object of format: {"raw": "geia sas", "translated": "γεια σας"}
     */
    public JSONObject toJSON(){
        StringBuilder raw = new StringBuilder();
        StringBuilder translated = new StringBuilder();;

        for(Word w : this.words){
            raw.append(w.getWordRaw());
            raw.append(" ");
            translated.append(w.getWordTranslated());
            translated.append(" ");
        }

        // Remove the redundant SPACE character added from the iteration.
        String rawSentence = raw.substring(0, raw.length()-1);
        String translatedSentence = translated.substring(0, translated.length()-1);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("raw", rawSentence);
            jsonObject.put("translated", translatedSentence);
        } catch (JSONException e) {
            System.err.println(e);
        }

        return jsonObject;
    }

    @Override
    public String toString() {
        String out =  "Sentence{\n";
        for(Word w: this.words){
            out = out + "\t" + w.toString() + "\n";

        }
        return out + '}';
    }
}
