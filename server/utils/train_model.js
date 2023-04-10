const {PythonShell} = require('python-shell'); 
const schedule = require('node-schedule'); 

const path = require('path');
const parent_dir = path.join(__dirname, '..');
const mongo_db_interactions = require(parent_dir + "\\mongo_db_api\\mongo.js");


function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const job = schedule.scheduleJob( {hour: 00, minute: 00}, runTrainScript);// TODO adjust times now runs at each midnight 

function runTrainScript(){

    log('Running train script');

    //TODO appropriate query to get only the sentences from the database 
    //TODO modify to run on batches do not call the script with 10000 samples 
    mongo_db_interactions.retrieveData("UserData", "Translated_and_non", "TODO INSERT QUERY HERE");


    let options = {
        mode: 'text', 
        pythonOptions: ['-u'], //print results 
        scriptPath: './utils/python_scripts', 
        args:[] //TODO add necessary enviroment variables these can be the batches 
    }

    PythonShell.run('train_model.py', options).then(messages => {
        console.log(JSON.stringify(messages));
        console.log('finished');
    });

}


process.on('SIGINT', function() {
    log("Shutting down jobs gracefully");
    schedule.gracefulShutdown(); 
});