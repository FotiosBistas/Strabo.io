const {PythonShell} = require('python-shell'); 
const schedule = require('node-schedule'); 

function log(text){
    let time = new Date(); 
    console.log("[" + time + "] " + " " + text)
}

const job = schedule.scheduleJob( {hour: 00, minute: 00}, runTrainScript);// TODO adjust times now runs at each midnight 

function runTrainScript(){

    log('Running train script');

    //TODO appropriate query
    //TODO modify to run on batches do not call the script with 10000 samples 


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