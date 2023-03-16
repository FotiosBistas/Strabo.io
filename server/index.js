"use strict"
const express = require('express')
const https = require('https')
const app = express();


function log(text){
    let time = new Date(); 
    console.log("[" + time.toLocaleTimeString() + "] " + " " + text)
}
let result = null 
//result = https.createServer(app).listen(3000, () =>{
//    log("Listening on port 3000 https server")
//})



app.use(express.static('public'));

// parse url-encoded content from body
app.use(express.urlencoded({ extended: true }));

// parse application/json content from body
app.use(express.json()) ;


if(!result){
    log("Could not create https server")
    app.listen(3000, () =>{
        log("Listening on port 3000 http server")
    })
}

/**
 * User will request for the model 
 */
app.get('/Model', (req,res) => {
    
})


/**
 * Receiving batches of translated and non-translated data 
 */
app.post('/Batch', (request,result) =>{
    log("Received batch in http server"); 
    const {uid,message} = request.body;
  
    // do something with the data
    log({uid,message});
    const words = wordTokenizer(message)
    const sentence = sentenceTokenizer(message)
    const timestamp = new Date(); 
    log(words)
    // send a response
    result.status(200).send('Received batch data');
})


/**
 * Breaks down the input into sentences based on !,?,.
 * @param {*} message 
 * @returns 
 */
function sentenceTokenizer(message){
    let result = message.match( /[^\.!\?]+[\.!\?]+/g );
    return result;
}



/**
 * Receives the message as input and tokenizes the words. 
 * Removes all punctuation and lowercases all words. 
 * @param {*} message 
 * @returns the word array  
 */
function wordTokenizer(message) {
    // Remove any punctuation inside the sentence and convert to lowercase
    let transformed = message.replace(/[.,\/#!$%\^&\*;?:{}=\-_`~()]/g," ").toLowerCase();

    // Split the sentence into an array of words using whitespace as the delimiter
    const words = transformed.trim().split(/\s+/);

    // Return the array of words
    return words;
}


