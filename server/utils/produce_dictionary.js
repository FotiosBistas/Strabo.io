//produces a day dictionary based on the previous day's database inputs 
//and the general inputs 



const {PythonShell} = require('python-shell'); 
const schedule = require('node-schedule'); 
const path = require('path');
const parent_dir = path.join(__dirname, '..');
const mongo_db_interactions = require(parent_dir + "\\mongo_db_api\\mongo.js");


function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const job = schedule.scheduleJob('*/20 * * * * *', createDailyDictionary)//TODO adjust times now runs at each midnight 


async function createDailyDictionary(){

    log('Running create dictionary script');

    let today = new Date(); 

    let previous_than_today_docs =  await mongo_db_interactions.retrieveData(
        "UserData", 
        "Translated_and_non", 
        {_id:{$lt: mongo_db_interactions.objectIdWithTimestamp(today)}}); 

    let todays_docs = await mongo_db_interactions.retrieveData(       
         "UserData", 
        "Translated_and_non", 
        {_id:{$lt: mongo_db_interactions.objectIdWithTimestamp(today)}})

    docs.forEach(sample => {
        
    });
    

}
