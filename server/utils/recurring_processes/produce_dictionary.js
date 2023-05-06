//produces a day dictionary based on the previous day's database inputs 
//and the general inputs 

const schedule = require('node-schedule'); 
const path = require('path');
const parent_dir = path.join(__dirname, '..');
const mongo_db_interactions = require(parent_dir + "\\mongo_db_api\\mongo.js");
const batch_processing = require('./batch_processing.js'); 
const dictionary = require('./dictionary.js');

function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const job = schedule.scheduleJob('*/2 * * * *', createDailyDictionary)//TODO adjust times now runs at each midnight 

//weights used for the weighted average 
const PREVIOUS_DATA_WEIGHT = 0.4; 
const TODAYS_DATA_WEIGHT = 0.6; 

//maps to count word occurences and weighted average
const today_word_map = new Map(); 
const rest_word_map = new Map(); 


async function createDailyDictionary(){

    log('Running create dictionary script');

    let today = new Date(); 

    let previous_than_today_docs =  await mongo_db_interactions.retrieveData(
        "UserData", 
        "Translated_and_non", 
        {timestamp:{$lt: "2023-04-04"}}); 

    let todays_docs = await mongo_db_interactions.retrieveData(       
        "UserData", 
        "Translated_and_non", 
        {timestamp:{$eq:"2023-04-04"}})

   
  
    //decrypt words and count occurences
    previous_than_today_docs.forEach(encryptedStruct => {
        let decrypted_struct = batch_processing.decryptData(encryptedStruct);
        decrypted_struct.translated_words.forEach(word => {
            addCountToWord(word, rest_word_map);
        });
    });

    //decrypt words and count occurences
    todays_docs.forEach(encryptedStruct => {
        let decrypted_struct = batch_processing.decryptData(encryptedStruct);
        decrypted_struct.translated_words.forEach(word => {
            addCountToWord(word, today_word_map);
        });
    });


    calculateWeightedAverages(today_word_map, rest_word_map); 

    console.log("HI there");

    if(today_word_map.size >= rest_word_map.size){
        dictionary.createNewDictionary(today_word_map);
    }else{
        dictionary.createNewDictionary(rest_word_map);
    }
    //empty the maps 
    today_word_map.clear(); 
    rest_word_map.clear(); 
    console.log("Hi there"); 
    

}

/**
 * Increases the count for each word occurence for 
 * the proper map  (today or previous period) 
 * @param {*} word accepts the word we want to increase the count for 
 * @param {*} map the previous day's or today's map  
 */
function addCountToWord(word, map){
    //add one to the word occurences
    if(map.has(word)){
        map.set(word, map.get(word) + 1); 
        log("Word: " + word + " occurences now are: " + map.get(word));
        return 
    }

    map.set(word, 1); 
    log("Word: " + word + " occurences now are: " + map.get(word)); 

}

/**
 * Retrieves the count for each word from the wordmap 
 * and calculates the weighted average. The weighted average 
 * is inserted as a count in each word. 
 * @param {*} map1 word map 1 (previous) order matters
 * @param {*} map2 word map 2 (today) order matters 
 * @returns the weighted average of each word
 */
function calculateWeightedAverages(map1, map2){

    let larger_map = null; 
    
    //depending on the size of the maps 
    //store the weighted average on the existing words 
    if(map1.size >= map2.size){
        for(const[key,value] of map1){
            //if both maps have the key calculate the weighted average normally 
            if(map2.has(key)){
                let average = PREVIOUS_DATA_WEIGHT * map1.get(key) + TODAYS_DATA_WEIGHT * map2.get(key);
                map1.set(key, average); 
                continue; 
            }
            let average = PREVIOUS_DATA_WEIGHT * map1.get(key) + TODAYS_DATA_WEIGHT * 0;  
            map1.set(key, average);
        }
    }else{
        for(const[key,value] of map2){
            //if both maps have the key calculate the weighted average normally 
            if(map1.has(key)){
                let average = PREVIOUS_DATA_WEIGHT * map1.get(key) + TODAYS_DATA_WEIGHT * map2.get(key);
                map2.set(key, average); 
                continue; 
            }
            let average = PREVIOUS_DATA_WEIGHT * 0 + TODAYS_DATA_WEIGHT * map2.get(key); 
            map2.set(key, average);
        }
    }

} 