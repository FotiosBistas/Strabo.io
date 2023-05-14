const hunspell = require('nodehun');
const path = require('path');
const fs = require('fs');


//read the dictionary files 
const affix = fs.readFileSync(__dirname + '/greek.aff');
const dictionary = fs.readFileSync(__dirname + '/greek.dic');

const greek_dictionary_hunspell = new hunspell(affix, dictionary);
const WRONG_THRESHOLD = 0.3; 

module.exports = {

    /**
     * Checks how many of the words in the sentence are wrong to 
     * make a decision whether to add the data into the database. 
     * @param {*} words the words to be spell checked  
     * @returns false if the sentence is wrong above a threshold else the percentage of wrong words.  
     */
    spellCheckWords: async function(words){

        let wrong_words_counter = 0; 
        for(let i = 0; i < words.length ; i++){
            try{
                let result = await greek_dictionary_hunspell.spell(words[i]);
                let suggestions = await greek_dictionary_hunspell.suggest(words[i]);
                //word doesn't exist in dictionary or it is correctly spelled 
                if(suggestions === null || result){
                    continue; 
                }

                //word is not in greek 
                if(suggestions.length == 0){
                    continue; 
                }

                wrong_words_counter++; 

            }catch(err){
                console.log(err);
                continue;
            }
        }
    
        let percentage_of_wrong_words = wrong_words_counter / words.length; 
        //don't insert into the database 
        if(percentage_of_wrong_words > WRONG_THRESHOLD){

            return false;
        }
        //insert into the database 
        return percentage_of_wrong_words.toFixed(1); 
    },
} 