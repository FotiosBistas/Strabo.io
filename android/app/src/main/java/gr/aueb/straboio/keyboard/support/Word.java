package gr.aueb.straboio.keyboard.support;

public class Word {

    private StringBuilder wordRaw;
    private StringBuilder wordTranslated;
    private int boundStart;
    private int boundEnd;

    /**
     * Each word inserted by the user is modeled as a 'Word' object. The object contains information for
     * location of the word inserted by the user in the sentence, the translation from the model and the raw
     * input.
     *
     * For more info checkout: https://github.com/FotiosBistas/Strabo.io/pull/2#issuecomment-1549467102
     *
     * @param wordRaw the raw input to the translation model
     * @param wordTranslated the (potentially corrected) output of the translation model
     * @param boundStart the starting bound of 'wordTranslated'
     *                   e.g. 'Φώτης' -> 0Φ1ώτ2η3ς4 (boundStart == 1)
     * @param boundEnd the ending bound of 'wordTranslated'
     *                 e.g. 'Φώτης' -> 0Φ1ώτ2η3ς4 (boundStart == 4)
     */
    public Word(StringBuilder wordRaw, StringBuilder wordTranslated, int boundStart, int boundEnd) {
        this.wordRaw = wordRaw;
        this.wordTranslated = wordTranslated;
        this.boundStart = boundStart;
        this.boundEnd = boundEnd;
    }

    public StringBuilder getWordRaw() {
        return wordRaw;
    }

    public StringBuilder getWordTranslated() {
        return wordTranslated;
    }

    public int getBoundStart() {
        return boundStart;
    }

    public int getBoundEnd() {
        return boundEnd;
    }

    public int length(){
        return this.wordTranslated.length();
    }

    public void setWordRaw(StringBuilder wordRaw) {
        this.wordRaw = wordRaw;
    }

    public void setWordTranslated(StringBuilder wordTranslated) {
        this.wordTranslated = wordTranslated;
    }

    public void setBoundStart(int boundStart) {
        this.boundStart = boundStart;
    }

    public void setBoundEnd(int boundEnd) {
        this.boundEnd = boundEnd;
    }

    /**
     * Moves the word one position to the right in the sentence.
     */
    public void shift(){
        this.shift(1);
    }

    /**
     * moves the word p positions to the right (if p>0) or left (if p<0)
     * @param p positions
     */
    public void shift(int p){
        this.boundStart = this.boundStart + p;
        this.boundEnd = this.boundEnd + p;
    }

    /**
     * Checks if the cursorIndex is pointing inside of the word
     * @param cursorIndex the cursor's index
     * @return true if the cursor points in the word, false if not
     */
    public boolean inRange(int cursorIndex){
        return (cursorIndex >= this.boundStart && cursorIndex <= this.boundEnd);
    }

    /**
     * Inserts character in the translated word according to the given cursor position.
     * @param character the character to be inserted
     * @param cursorPosition the cursor's position after the insertion of the character
     */
    public void insert(String character, int cursorPosition){
        // WARNING: the cursor index value is the cursor's position after the insertion of the character.
        // Thus, in this case we want to insert the character in position cursorPosition - 1.
        this.wordTranslated.insert(cursorPosition - this.boundStart, character);
        this.boundEnd = this.boundEnd + character.length();
    }

    /**
     * Inserts character both in the raw and translated word.
     * @param character the character to be inserted
     * @param cursorPosition the cursor's position after the insertion of the character
     */
    public void insertBoth(String character, int cursorPosition){
        this.wordRaw.insert(cursorPosition - boundStart, character);
        this.insert(character, cursorPosition);
    }

    /**
     * Deletes character at the given position in the translated word
     * @param cursorPosition the cursor's position after the deletion of the character
     */
    public void delete(int cursorPosition){
        // WARNING: the cursor index value is the cursor's position after the deletion of the character.
        // Thus, in this case we want to delete the character in position cursorPosition + 1.
        if(this.wordTranslated.length() != 0){
            this.wordTranslated.deleteCharAt(cursorPosition + 1 - this.boundStart);
            this.boundEnd--;
            if (this.wordTranslated.length() == 0){
                this.wordRaw = new StringBuilder();
            }
        }
    }

    /**
     * Deletes character at the given position in the raw and translated word
     * @param cursorPosition the cursor's position after the deletion of the character
     */
    public void deleteBoth(int cursorPosition){
        if(this.wordRaw.length() != 0){
            this.wordRaw.deleteCharAt(cursorPosition + 1 - this.boundStart);
            this.delete(cursorPosition);
        }
    }

    /**
     * Checks if the word is empty, thus ready to be removed from the sentence.
     * @return true if raw and translated are empty, false if not
     */
    public boolean isEmpty(){
        return this.wordRaw.length() == 0 && this.wordTranslated.length() == 0;
    }

    @Override
    public String toString() {
        return "Word{" +
                "wordRaw='" + wordRaw + "'"+
                ", wordTranslated='" + wordTranslated +"'"+
                ", boundStart=" + boundStart +
                ", boundEnd=" + boundEnd +
                '}';
    }

    /**
     * Increments the ending bound of the translated word by 1.
     */
    public void incrBoundEnd() {
        this.incrBoundEnd(1);
    }

    /**
     * Increases the ending bound of the translated word by 'length'.
     * @param length increment units
     */
    public void incrBoundEnd(int length) {
        this.boundEnd = this.boundEnd + length;
    }

    /**
     * Checks if 'input' contains latin or/and punctuation or/and digits only.
     * @param input
     * @return true if 'input' contains latin or/and punctuation or/and digits only, false if not
     */
    public static boolean containsOnlyLatinPunctuationNumbers(String input) {
        // Define the regular expression pattern
        String pattern = "^[a-zA-Z0-9\\p{Punct}]+$";

        // Use the pattern to match the input string
        return input.matches(pattern);
    }

    /**
     * Checks if 'input' contains greek or/and punctuation or/and digits only.
     * @param input
     * @return true if 'input' contains greek or/and punctuation or/and digits only, false if not
     */
    public static boolean containsOnlyGreekPunctuationNumbers(String input) {
        // Define the regular expression pattern
        String pattern = "^[\\p{IsGreek}0-9\\p{Punct}]+$";

        // Use the pattern to match the input string
        return input.matches(pattern);
    }

    /**
     * Checks if raw and translated word are written in the same language.
     * @return true if raw and translated word are written in the same language, false if not
     */
    public boolean inTheSameLanguage(){
        return (
                containsOnlyGreekPunctuationNumbers(wordRaw.toString()) && containsOnlyGreekPunctuationNumbers(wordTranslated.toString())
                ) || (
                containsOnlyLatinPunctuationNumbers(wordRaw.toString()) && containsOnlyLatinPunctuationNumbers(wordTranslated.toString())
        );
    }
}
