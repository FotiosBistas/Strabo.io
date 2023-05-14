greek_dictionary = new Set(); 
greek_dictionary.add("");
function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}


const DICTIONARY_SIZE = 4000; 

module.exports = {

    /**
     * After the dictionary is constructed for the specified time interval (e.g. a day)
     * each new sample in a batch produces a bitmap to determine whether the sample can be 
     * ignored and not inserted in the database.  
     * @param {*} words the words contained inside the sample 
     * @returns the bitmap for the specific sample.  
     */
    createBitmaps: function(words){
        

        if(greek_dictionary.size == 0){
            log("Dictionary has not been produced yet");
            return 0;
        }

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
     * Creates a new dictionary for the specified time interval
     * keeping the most common weighted averages 
     * @param {*} map accepts a map of the weighted averages 
    */
    createNewDictionary: function(map){

        keys = []; 

        //keep all the words as the words 
        //are smaller than the threshold 
        //dictionary size 
        if(map.size < DICTIONARY_SIZE){
            let new_set = new Set(map.keys()); 
            greek_dictionary.clear(); 
            greek_dictionary = new_set; 
            return 
        }


        //sort the weighted averages in a descending order
        //and take the most commonly used.
        let dictionary_array = Array.from(map, ([name, value]) => ({name, value})); 

        dictionary_array.sort((a, b) => b.value - a.value); 
        
        let frequent_word = dictionary_array.slice(0, DICTIONARY_SIZE).map(({name}) => name);

        let new_set = new Set(frequent_word); 
        //empties the dictionary 
        greek_dictionary.clear(); 

        greek_dictionary = new_set; 

    },

    /**
     * Returns the match score for a specific bitmap. 
     * E.g. if a sample has 3 matches over 5 it means it's a common occurence. 
     * @param {*} bitmap an array of type [1,1,0,0,1] 
     * @returns the percentage/match score 
     */
    extractMatchScore: function(bitmap){

        let sum = 0; 

        if(greek_dictionary.size == 0){
            return 0; 
        }

        bitmap.forEach(sample => {

            sum = sum + sample; 
        });


        return sum /greek_dictionary.size; 
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