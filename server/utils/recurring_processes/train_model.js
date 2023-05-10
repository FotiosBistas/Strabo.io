const {PythonShell} = require('python-shell'); 
const schedule = require('node-schedule'); 

const path = require('path');
const mongo_directory = path.dirname(path.dirname(__dirname));
const mongo_db_interactions = require( mongo_directory + "\\mongo_db_api\\mongo.js");

function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const job = schedule.scheduleJob( "*/1 * * * *", runTrainScript);// TODO adjust times now runs at each midnight 

function runTrainScript(){

    log('Running train script');

    //TODO modify to run on batches do not call the script with 10000 samples 
    // Retrieve the parallel data from mongodb
    const results = mongo_db_interactions.retrieveData("UserData", "Translated_and_non", "{}, { translated: 1, _id: 0 }");
    
    const translatedList = [];
    // Load data into lists
    results.forEach(item => {
        translatedList.push(item.translated);
    });

    // Split to 2000-item batches 
    const batchSize = 2000;
    const translatedBatches = [];

    for (let i = 0; i < translatedList.length; i += batchSize) {

    const translatedBatch = translatedList.slice(i, i + batchSize);
    translatedBatches.push(translatedBatch);
    }

    // Run training for each batch
    for (let i = 0; i < translatedBatches.length; i++) {

        let options = {
            mode: 'text', 
            pythonOptions: ['-u'], //print results 
            scriptPath: './utils/python_scripts', 
            args:[translatedBatches[i]] //TODO add necessary enviroment variables these can be the batches 
        }

        PythonShell.run('train_model.py', options).then(messages => {
            console.log(JSON.stringify(messages));
            console.log('finished');
        });
    }   
}


process.on('SIGINT', function() {
    log("Shutting down jobs gracefully");
    schedule.gracefulShutdown(); 
});