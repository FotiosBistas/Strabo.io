const { MongoClient , ServerApiVersion} = require('mongodb'); 
const uri = "mongodb+srv://Fotis:MQN5IGV2uTI0Pe0n@dictionarycluster.mkmtbvi.mongodb.net/?retryWrites=true&w=majority";
//hi
function log(text){
    var time = new Date();
    console.log("[" + time.toLocaleTimeString() + "] " + text);
}
//
//const options = {
//    useNewUrlParse:true, 
//    useUnifiedTopology:true, 
//    poolSize: 10, 
//    serverSelectionTimeoutMS: 5000,
//};

// Create a new MongoClient instance
const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true, serverApi: ServerApiVersion.v1 });

module.exports = {


    addStructToDatabase: async function(database_structs){
        try {
            await client.connect();
            log("Opened new connection successfully");

            const result = await client.db("UserData").collection("Translated-and-non").insertMany(database_structs);
            log("Documents inserted: " + result.insertedCount);
        } catch (err) {
            log("Documents were not inserted into the database error: " + err);
        } finally {
            client.close();
        }
    }  

}