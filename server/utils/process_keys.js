const crypto = require('crypto');
const algorithm = 'aes-256-ctr'; 
const fs = require('fs');
const bcrypt = require('bcrypt');
const prompt = require('prompt-sync')({sigint: true}); 
const IV_LENGTH = 16; 

// Read the encrypted key, passphrase, and IV from the environment variable
const encryptedKey = process.env.enkey;
const iv = process.env.iv;
const passphrase = Buffer.from(prompt('Please enter passphrase: '), 'utf-8');
const hashed_passphrase = process.env.pass;

const res = bcrypt.compareSync(passphrase, hashed_passphrase);

if(!res){
    throw new Error("Wrong passphrase given");
}