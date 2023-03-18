greek_dictionary = new Set(); 

function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}

module.exports = {

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

    insertLetterandWordInDictionary: function(words){
        words.forEach(word => {
            greek_dictionary.add(word); 
        });
    }, 


}