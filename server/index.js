"use strict"
const { time } = require('console');
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



app.post('/Login', (request, result) => {

})



/**
 * Receiving batches of translated and non-translated data 
 */
app.post('/Batch', (request,result) =>{
    log("Received batch in http server"); 
    const {uid,batch} = request.body;
 
    //batch should be two arrays the translated and non translated data

    // do something with the data
    log({uid,batch});
    processBatch(batch);
    // send a response
    result.status(200).send('Received batch data');
})

/**
 * Receives a batch of type 
 * [ 
 *      {
 *          'non_translated': "",
 *          'translated': ""
 *      },
 *      ... 
 * ]
 * @param {*} batch 
 */
function processBatch(batch){
    batch.forEach(translation_struct => {
        //concatenate the two string to be inserted into the database
        //and processed by the tokenizers 
        //let concatenated = translation_struct.non_translated.concat(
        //    translation_struct.translated
        //    )
        let translated_words = wordTokenizer(translation_struct.translated);
        let translated_sentences = sentenceTokenizer(translation_struct.translated);
        let non_translated_words = wordTokenizer(translation_struct.non_translated);
        let non_translated_sentences = sentenceTokenizer(translation_struct.non_translated);

        let timestamp = new Date();
        //this will be inserted into the database
        let database_struct = {
            "translated_words": translated_words, 
            "translated_sentences": translated_sentences,
            "non_translated_words": non_translated_words, 
            "non_translated_sentences": non_translated_sentences,
            "number_of_words": translated_words.length, 
            "number_of_sentences": translated_sentences.length,
            "timestamp": timestamp, 
            "translated": translation_struct.translated, 
            "non_translated": translation_struct.non_translated
        }

        log(JSON.stringify(database_struct))
    })
}

/**
 * Breaks down the input into sentences based on !,?,.
 * TODO it might need ; 
 * @param {*} translation_struct
 * @returns 
 */
function sentenceTokenizer( translation_struct){
    let result = translation_struct.match( /[^\.!\?]+[\.!\?]+/g );
    return result;
}



/**
 * Receives the  translation_struct input and tokenizes the words. 
 * Removes all punctuation and lowercases all words. 
 * @param {*} translation_struct
 * @returns the word array  
 */
function wordTokenizer(translation_struct) {
    // Remove any punctuation inside the sentence and convert to lowercase
    let transformed =translation_struct.replace(/[.,\/#!$%\^&\*;?:{}=\-_`~()]/g," ").toLowerCase();

    // Split the sentence into an array of words using whitespace as the delimiter
    const words = transformed.trim().split(/\s+/);

    // Return the array of words
    return words;
}


