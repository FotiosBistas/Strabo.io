const crypto = require('crypto');
const prompt = require('prompt-sync')();
const bcrypt = require('bcrypt');
const algorithm = 'aes-256-ctr';

const IV_LENGTH = 16;

// Read the encrypted key, passphrase, and IV from the environment variable
const encryptedKey = process.env.enkey;
const iv = process.env.iv;

// Prompt the user for the passphrase
const passphrase = prompt('Please enter the passphrase: ');

// Compare the user input passphrase with the hashed passphrase
bcrypt.compareSync(passphrase, process.env.passphrase, function(err, result) {
  if (err) {
    console.error(err);
    return;
  }
  if (result) {
    // If the hash matches, decrypt the private key using the passphrase and IV
    const decipher = crypto.createDecipheriv(algorithm, passphrase, iv);
    let private_key = decipher.update(encryptedKey, 'hex', 'utf8');
    private_key += decipher.final('utf8');
    console.log(private_key);
  } else {
    console.error('Passphrase is incorrect');
  }
});