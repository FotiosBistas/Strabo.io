const crypto = require('crypto');
const algorithm = 'aes-256-ctr'; 
const fs = require('fs');
const bcrypt = require('bcrypt');
const prompt = require('prompt-sync')({sigint: true}); 
const IV_LENGTH = 16; 

const passphrase = Buffer.from(prompt('Please enter passphrase: '), 'utf-8');
const hashed_passphrase = process.env.pass;

const res = bcrypt.compareSync(passphrase, hashed_passphrase);

if(!res){
    throw new Error("Wrong passphrase given");
}

module.exports.passphrase = passphrase;