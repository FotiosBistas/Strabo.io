"use strict"
const express = require('express')
const express_rate_limit = require('express-rate-limit')
const https = require('https');
const app = express();
const morgan = require('morgan');
const fs = require('fs');
const path = require('path');


require("dotenv").config(); 
require('./utils/process_keys');
require('./utils/train_model');
const mongoDBinteractions = require('./mongo_db_api/mongo.js'); 
const batch_utilities = require('./utils/batch_processing.js'); 



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

const accessLogStream = fs.createWriteStream(path.join(__dirname, 'access.log'), { flags: 'a' });

const logRequest = morgan(function (tokens, req, res) {
  return [
    tokens.date(req, res),
    req.ip,
    tokens.method(req, res),
    tokens.url(req, res),
    tokens.status(req, res),
    tokens.res(req, res, 'content-length'), '-',
    tokens['response-time'](req, res), 'ms'
  ].join(' ')
}, { stream: accessLogStream });

const batch_limiter = express_rate_limit.rateLimit({
    windowMs: 5 * 60 * 1000, //retry in 5 minutes  
    max: 2, //maximum batches received in 5 minutes 
    message: "Too many batches sent in 5 minutes. Try again later", 
    standardHeaders: true, 
})

app.use(logRequest);

/**
 * Receiving batches of translated and non-translated data 
 * along with user ID. 
 */
app.post('/Batch', batch_limiter,async (request,result) =>{
    log("Received batch in http server"); 
    const {uid,batch} = request.body;
 
    //batch should be two arrays the translated and non translated data

    // do something with the data
    log({uid,batch});
    try{
        let database_structs = batch_utilities.processBatch(batch);
        mongoDBinteractions.addStructToDatabase(database_structs);
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