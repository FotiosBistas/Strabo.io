"use strict"
const express = require('express');
const express_rate_limit = require('express-rate-limit');
const https = require('https');
const morgan = require('morgan');
const fs = require('fs');
const path = require('path');

const PORT = 443; // HTTPS port

const credentials = { 
  key: fs.readFileSync('./ssl/key.pem'), 
  cert: fs.readFileSync('./ssl/cert.pem')
};

require("dotenv").config(); 
require('./utils/process_keys');
const spammer = require('./utils/recurring_processes/block_spammer_ips')
require('./utils/recurring_processes/produce_dictionary.js');
require('./utils/spam_data_detection.js');
const mongoDBinteractions = require('./mongo_db_api/mongo.js'); 
const batch_utilities = require('./utils/batch_processing.js'); 



function log(text){
    let time = new Date(); 
    console.log("[" + time.toLocaleTimeString() + "] " + " " + text)
}

let server = null;

const app = express();

app.use(express.static('public'));

// parse url-encoded content from body
app.use(express.urlencoded({ extended: true, limit: '10mb' }));



// parse application/json content from body
app.use(express.json( {limit:'10mb'})) ;

const SAVED_MODELS_DIRECTORY = './utils/python_scripts/saved_models/optimized';

/**
 * Returns the file name of the latest model.
 */
app.get('/peek', function(req, res) {
    fs.readdir(SAVED_MODELS_DIRECTORY, function(err, files) {
        if (err) {
            res.status(500).send('INTERNAL SERVER ERROR');
            log(err);
            return;
        }

        let latest; // file name of the latest model
        let latestTime = 0;

        files.forEach(function(fileName) {
            if (path.extname(fileName) === '.pt') {
                const filePath = path.join(SAVED_MODELS_DIRECTORY, fileName);
                const fileTime = fs.statSync(filePath).mtime.getTime();

                if (!latest || fileTime > latestTime) {
                    latest = fileName;
                    latestTime = fileTime;
                }
            }
        });

        if (latest) {
            res.json({'model_name': latest});
            log('Latest model: ' + latest);
        } else {
            res.status(404).send('No models found.');
            log('No models found.');
        }
    });
});


/**
 * User will request for the model 
 * Sends the latest model to the user.
 */
app.get('/Model', (req,res) => {
  fs.readdir(SAVED_MODELS_DIRECTORY, function(err, files) {
    if (err) {
        res.status(500).send('INTERNAL SERVER ERROR');
        log(err);
        return;
    }

    let latest; // file name of the latest model
    let latestTime = 0;

    files.forEach(function(fileName) {
        if (path.extname(fileName) === '.pt') {
            const filePath = path.join(SAVED_MODELS_DIRECTORY, fileName);
            const fileTime = fs.statSync(filePath).mtime.getTime();

            if (!latest || fileTime > latestTime) {
                latest = fileName;
                latestTime = fileTime;
            }
        }
    });

    if (latest) {
        // Send the most recent file
        res.sendFile(path.resolve(SAVED_MODELS_DIRECTORY, latest));
        log('Sending latest model: ' + latest);
    } else {
        res.status(404).send('No models found.');
        log('No models found.');
    }
  });
})

const accessLogStream = fs.createWriteStream(path.join(__dirname, 'access.log'), { flags: 'a' });

const logRequest = morgan(function (tokens, req, res) {
  return [
    req.ip,
    tokens.method(req, res),
    tokens.url(req, res),
    tokens.status(req, res),
    tokens['response-time'](req, res)+ 'ms',
    tokens.date(req, res),
  ].join(' ')
}, { stream: accessLogStream });

const batch_limiter = express_rate_limit.rateLimit({
    windowMs: 5 * 60 * 1000, //retry in 5 minutes  
    max: 1, //maximum batches received in 5 minutes 
    message: "Too many batches sent in 5 minutes. Try again later", 
    standardHeaders: true, 
})

// Middleware function to block requests from spammer IPs
function blockSpammers(req, res, next) {
  if (spammer.spammer_ips.get(req.ip)) {
    return res.status(403).send('Access denied');
  }
  next();
}

app.use(logRequest);
app.use(blockSpammers);
/**
 * Receiving batches of translated and non-translated data 
 * along with user ID. 
 */
app.post('/Batch',batch_limiter,async (request,result) =>{
    log("Received batch in http server"); 
    const {batch} = request.body;

    //batch should be two arrays the translated and non translated data

    // do something with the data
    log(JSON.stringify(batch));
    try{
        let database_structs = await batch_utilities.processBatch(batch);
        await mongoDBinteractions.addStructToDatabase(database_structs);
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

if(!server){
  server = https.createServer(credentials, app).listen(PORT, async () => {
    await mongoDBinteractions.connectToDatabase(); 
    log(`Listening on port ${PORT} https server`);
  })
}
