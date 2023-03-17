const { MongoClient , ServerApiVersion} = require('mongodb'); 
const uri = "mongodb+srv://Fotis:MQN5IGV2uTI0Pe0n@dictionarycluster.mkmtbvi.mongodb.net/?retryWrites=true&w=majority";
//hi
function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}

const options = {
    useNewUrlParser:true, 
    useUnifiedTopology:true, 
    serverSelectionTimeoutMS: 5000,
    serverApi: ServerApiVersion.v1
};

// Create a new MongoClient instance
const client = new MongoClient(uri, options);

module.exports = {

    connectToDatabase: async function(){
        try{
            await client.connect();
            log("Opened new connection successfully");
        }catch(err){
            log("Couldn't connect to database: " + err);
        }    
    },


    closeConnection: async function(){
        try{
            await client.close();
            log("Closed connection successfully");
        }catch(err){
            log("Couldn't close database connection");
        }
    },

    addStructToDatabase: async function(database_structs){
        try {

            const result = await client.db("UserData").collection("Translated-and-non").insertMany(database_structs);
            log("Documents inserted: " + result.insertedCount);
        } catch (err) {
            log("Documents were not inserted into the database error: " + err);
        } 
    }  

}