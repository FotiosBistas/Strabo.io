"use strict"
const express = require('express')
const https = require('https');
const app = express();
const mongoDBinteractions = require('./mongo_db_api/mongo.js'); 
const dictionary_interactions = require('./dictionary.js');
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
 * TODO: SESSION ID 
 * 
 */
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
    try{
        processBatch(batch);
        log("Processed batch successfully")
        // send a response
        result.status(200).send('Received batch data');
    }catch(err){
        log("Error while processing batch: " + err);
        result.status(500).send("Couldn't process request")
    }
    
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
    let database_structs = [] 
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

        let timestamp = new Date().toISOString().split('T')[0];
       // let date = new Date();
       // date.setDate(date.getDate() + 1);  
       // let timestamp = date.toISOString().split('T')[0];
        //this will be inserted into the database
        //TODO INDEXING 
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

        database_structs.push(database_struct)
        //log(JSON.stringify(database_struct))
    })
    log("Rough size of object is: " + roughSizeOfObject(database_structs) + " bytes");
    try{
        mongoDBinteractions.addStructToDatabase(database_structs);
        database_structs.forEach(struct => {
            
            dictionary_interactions.insertLetterandWordInDictionary(struct.translated_words); 
        });
        log(JSON.stringify(greek_dictionary))
    }catch(err){
        throw err; 
    }
}

/**
 * Breaks down the input into sentences based on !,?,.
 * TODO it might need ; 
 * @param {*} translation_struct
 * @returns 
 */
function sentenceTokenizer( translation_struct){
    let result = translation_struct.match( /[^\.;!\?]+[\.;!\?]+/g );
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


function roughSizeOfObject( object ) {

    var objectList = [];
    var stack = [ object ];
    var bytes = 0;

    while ( stack.length ) {
        var value = stack.pop();

        if ( typeof value === 'boolean' ) {
            bytes += 4;
        }
        else if ( typeof value === 'string' ) {
            bytes += value.length * 2;
        }
        else if ( typeof value === 'number' ) {
            bytes += 8;
        }
        else if
        (
            typeof value === 'object'
            && objectList.indexOf( value ) === -1
        )
        {
            objectList.push( value );

            for( var i in value ) {
                stack.push( value[ i ] );
            }
        }
    }
    return bytes;
}


process.on('SIGINT',async () => {
    log('Received SIGINT signal, shutting down server...');
    await mongoDBinteractions.closeConnection(); 
    server.close(() => {
      log('Server shut down gracefully');
      process.exit(0);
    });
});