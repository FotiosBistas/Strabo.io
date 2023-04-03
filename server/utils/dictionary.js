greek_dictionary = new Set(); 

function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}

module.exports = {

    /**
     * After the dictionary is constructed for the specified time interval (e.g. a day)
     * each new sample in a batch produces a bitmap to determine whether the sample can be 
     * ignored and not inserted in the database.  
     * @param {*} words the words contained inside the sample 
     * @returns the bitmap for the specific sample.  
     */
    createBitmaps: function(words){
        let word_bitmap = new Array(greek_dictionary.size).fill(0);

        index = 0; 
        greek_dictionary.forEach(word => {
            if(words.includes(word)){
                word_bitmap[index] = 1; 
            }
            index++;
        })

        return word_bitmap;  
    },

    /**
     * Returns the match score for a specific bitmap. 
     * E.g. if a sample has 3 matches over 5 it means it's a common occurence. 
     * @param {*} bitmap an array of type [1,1,0,0,1] 
     * @returns the percentage/match score 
     */
    extractMatchScore: function(bitmap){

        let sum = 0; 
        bitmap.forEach(sample => {

            sum = sum + sample; 
        });

        return sum/length(greek_dictionary.size); 
    },

    /**
     * Inserts a greek word inside the dictionary. 
     * @param {*} words 
     */
    insertLetterandWordInDictionary: function(words){
        words.forEach(word => {
            greek_dictionary.add(word); 
        });
    }, 


}