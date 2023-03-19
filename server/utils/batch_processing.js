
const CryptoJS = require('crypto-js');


const private_key = "something something"//TODO 


function log(text){
    let time = new Date(); 
    console.log("[" + time.toLocaleTimeString() + "] " + " " + text)
}

module.exports = {

    
    /**
     * Receives a batch of type 
     * [ 
     *      {
     *          'non_translated': "",
     *          'translated': ""
     *      },
     *      ... 
     * ]
     * and tokenizes it.
     * @param {*} batch 
     * @returns the processed batches along with metadata
     */
    processBatch(batch){
        let database_structs = [] 
        batch.forEach(translation_struct => {
            let translated_words = this.wordTokenizer(translation_struct.translated);
            let translated_sentences = this.sentenceTokenizer(translation_struct.translated);
            let non_translated_words = this.wordTokenizer(translation_struct.non_translated);
            let non_translated_sentences = this.sentenceTokenizer(translation_struct.non_translated);

            let timestamp = new Date().toISOString().split('T')[0];//TODO probably not needed in the end, helps to debug 

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
                "timestamp": timestamp, //TODO probably not needed in the end, helps to debug 
                "translated": translation_struct.translated, 
                "non_translated": translation_struct.non_translated
            };

            let encrypted_struct = this.encryptData(database_struct);
            let decrypted_struct = this.decryptData(encrypted_struct);
            database_structs.push(database_struct);
        })
        log("Rough size of object is: " + this.roughSizeOfObject(database_structs) + " bytes");
        return database_structs;
    },

    /**
     * Breaks down the input into sentences based on !,?,., ;
     * @param {*} translation_struct
     * @returns 
     */
    sentenceTokenizer( translation_struct){
        let result = translation_struct.match( /[^\.;!\?]+[\.;!\?]+/g );
        return result;
    },

    /**
     * Encrypts the data before sending them over to the database 
     * @param {*} database_struct a finalized structure for the data that 
     * will be inserted into the database  
     */
    encryptData(database_struct){
        //value is either timestamp, full text data of batch, or array 
        
        let encrypted_struct = {}; 


        non_needed_encryption_fields = ['timestamp', 'number_of_sentences', 'number_of_words'];  

        for(const [key,value] of Object.entries(database_struct)){
            
            log(`Key: ${key}: value: ${value}`);

            if(non_needed_encryption_fields.includes(key)){
                encrypted_struct[key] = value;
                continue;
            }

            let encryptable_string = value; 

            if(Array.isArray(value)){
                encryptable_string = value.toString(); 
            }

            log("Encryptable string is: " + encryptable_string);

            let cipher = CryptoJS.AES.encrypt(encryptable_string, private_key).toString();
        
            encrypted_struct[key] = cipher; 
        }
        
        return encrypted_struct; 
    },

    /**
     * After retrieving the data from the database retrieve them
     * using the private key. 
     * @param {*} encrypted_struct structure received from database.  
     */
    decryptData(encrypted_struct){

        //value is either timestamp, full text data of batch, or array 
        
        let decrypted_struct = {}; 


        non_needed_decryption_fields = ['timestamp', 'number_of_sentences', 'number_of_words'];  

        for(const [key,value] of Object.entries(encrypted_struct)){
            
            log(`Key: ${key}: value: ${value}`);

            if(non_needed_decryption_fields.includes(key)){
                decrypted_struct[key] = value;
                continue;
            }

            let decryptable_string = value; 

            log("Decryptable string is: " + decryptable_string);

            let bytes = CryptoJS.AES.decrypt(decryptable_string, private_key);
            let originalField = bytes.toString(CryptoJS.enc.Utf8);
        
            decrypted_struct[key] = originalField; 
        }
        
        return decrypted_struct; 
    },

    /**
     * Receives the  translation_struct input and tokenizes the words. 
     * Removes all punctuation and lowercases all words. 
     * @param {*} translation_struct
     * @returns the word array  
     */
    wordTokenizer(translation_struct) {
        // Remove any punctuation inside the sentence and convert to lowercase
        let transformed =translation_struct.replace(/[.,\/#!$%\^&\*;?:{}=\-_`~()]/g," ").toLowerCase();

        // Split the sentence into an array of words using whitespace as the delimiter
        const words = transformed.trim().split(/\s+/);

        // Return the array of words
        return words;
    },

    /**
     * Helper that counts the bytes of an object. 
     * @param {*} object 
     * @returns 
     */
    roughSizeOfObject( object ) {

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





}