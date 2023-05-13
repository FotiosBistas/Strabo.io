const hunspell = require('nodehun');
const path = require('path');
const fs = require('fs');


//read the dictionary files 
const affix = fs.readFileSync(__dirname + '/greek.aff');
const dictionary = fs.readFileSync(__dirname + '/greek.dic');

const greek_dictionary_hunspell = new hunspell(affix, dictionary);

module.exports = {


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
        if(percentage_of_wrong_words > 0.3){

            return false;
        }
        //insert into the database 
        return true; 
    },
} 