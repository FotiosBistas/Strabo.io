"use strict"
const express = require('express')
const https = require('https');
const app = express();
require("dotenv").config(); 
require('./utils/process_keys');
require('./utils/train_model');
const mongoDBinteractions = require('./mongo_db_api/mongo.js'); 
const dictionary_interactions = require('./utils/dictionary.js');
const batch_utilities = require('./utils/batch_processing.js'); 
const { v4: uuidv4 } = require('uuid'); 
const batch_processing = require('./utils/batch_processing.js');




function log(text){
    let time = new Date(); 
    console.log("[" + time.toLocaleTimeString() + "] " + " " + text)
}
let server = null 
//server = https.createServer(app).listen(3000, () =>{
//    log("Listening on port 3000 https server")
//})



app.use(express.static('public'));

// parse url-encoded content from body
app.use(express.urlencoded({ extended: true, limit: '10mb' }));



// parse application/json content from body
app.use(express.json( {limit:'10mb'})) ;


if(!server){
    log("Could not create https server")
    server = app.listen(3000, async () =>{
        await mongoDBinteractions.connectToDatabase(); 
        log("Listening on port 3000 http server")
    })
}

/**
 * User will request for the model 
 */
app.get('/Model', (req,res) => {
    
})


/**
 * When a user starts using the dictionary 
 * an identifier for the session (TODO I don't know what session means yet)
 * is assigned to them  
 */
app.post('/Login', (request, result) => {
    let userID = {sessionId: uuidv4()};
    log(userID);
})



/**
 * Receiving batches of translated and non-translated data 
 * along with user ID. 
 */
app.post('/Batch', async (request,result) =>{
    log("Received batch in http server"); 
    const {uid,batch} = request.body;
 
    //batch should be two arrays the translated and non translated data

    // do something with the data
    log({uid,batch});
    try{
        let database_structs = batch_utilities.processBatch(batch);
        mongoDBinteractions.addStructToDatabase(database_structs);
        for(let i = 0; i < database_structs.length; i++){
            batch_processing.decryptData(database_structs[i]);
        }
        log("Processed batch successfully")
        // send a response
        result.status(200).send('Received batch data');
    }catch(err){
        log("Error while processing batch: " + err);
        result.status(500).send("Couldn't process request")
    }
    
})


process.on('SIGINT',async () => {
    log('Received SIGINT signal, shutting down server...');
    await mongoDBinteractions.closeConnection(); 
    server.close(() => {
      log('Server shut down gracefully');
      process.exit(0);
    });
});