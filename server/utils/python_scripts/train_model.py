import pickle
import sys
from model import LSTM_LangModel, LSTM_LangModelForMobile
from util import *
import torch
import math
import os
import random
from datetime import datetime
from torch.utils.data import DataLoader
from torch.optim import Adam
import json

import os
import glob

def mostRecetntModel(directory_path):

    # Search for .pt files in the specified directory
    file_list = glob.glob(os.path.join(directory_path, '*.pt'))

    # Sort the file list based on modification time in descending order
    file_list.sort(key=lambda x: os.path.getmtime(x), reverse=True)

    if file_list:
        # Get the path of the most recent .pt file
        return file_list[0]
    else:
        return None
    
# Path to save the model / load previous models
path = "utils/python_scripts/saved_models/"
# Collect sentences in Greek for training
filename = sys.argv[1]

# Read the file and convert the JSON string back into a list
with open(filename, 'r', encoding="utf-8") as f:
    data = json.load(f)

data = data[:100]
# 85/15 split
random.shuffle(data)
split = math.ceil(len(data)*0.85)
if split > 2000:
    train, val = data[:2000], data[2000:]
else:
    train, val = data[:split], data[split:]


# Load tokenizer 
with open(path + "vectorizer_50000_char_120_32_512.pickle", "rb") as f:
    tokenizer = pickle.load(f)

# Encode dataets
train_dataset = tokenizer.encode_dataset(train)
val_dataset = tokenizer.encode_dataset(val)

# Configure device if available
#device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
device = torch.device('cpu')
# Model hyperparams.
input_size = len(tokenizer.vocab) + 1
embed_size = 32
hidden_size = 512
output_size = len(tokenizer.vocab) + 1


model = LSTM_LangModel(input_size, embed_size, hidden_size, output_size)
model.to(device)

# ========Main Training Loop=========

epochs = 1
batch_size = 5
accumulation_steps = 1

train_batches = math.ceil(len(train_dataset) / batch_size)
val_batches = math.ceil(len(val_dataset) / batch_size)
train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
val_loader = DataLoader(val_dataset, batch_size=batch_size)

# Optimizer & LR Scheduler
criterion = nn.CrossEntropyLoss(ignore_index=0)
optim = Adam(model.parameters(), lr=1e-4)
scheduler = torch.optim.lr_scheduler.OneCycleLR(optimizer=optim, max_lr=3e-4, epochs=epochs, steps_per_epoch= math.ceil(len(train_loader)/accumulation_steps))

print("Started Training")
best_loss = None
for epoch in range(1, epochs + 1):

    #print("Epoch {}/{}".format(epoch, epochs))
    model.train()
    train_loss = 0
    optim.zero_grad()

    for i, (sources, targets) in enumerate(train_loader):
        #print("\r[{}{}] Batch {}/{}".format(math.ceil((i + 1) / len(train_loader) * 40) * "=", (40 - math.ceil((i + 1) / len(train_loader) * 40)) * " ", i + 1, len(train_loader)), end="")

        sources, targets = sources.to(device), targets.to(device)
        optim.zero_grad()

        # Forward pass
        output, h, c = model(sources)

        # Reshape output & targets to work with the loss function
        targets = torch.flatten(targets, end_dim=1)
        output = torch.flatten(output, end_dim=1)

        # Calculate loss
        loss = criterion(output, targets)
        train_loss += loss

        # Backward pass & update weights
        loss.backward()
        optim.step()
        scheduler.step()

    # Evaluation
    with torch.no_grad():
        model.eval()
        val_loss = 0
        for i, (sources, targets) in enumerate(val_loader):
            sources, targets = sources.to(device), targets.to(device)
            output, h, c = model(sources)

            targets = torch.flatten(targets, end_dim=1)
            output = torch.flatten(output, end_dim=1)

            loss = criterion(output, targets)
            val_loss += loss

    epoch_train_loss = train_loss / train_batches
    epoch_val_loss = val_loss / val_batches
    #print("\nLoss: (train){} (val){}".format(epoch_train_loss, epoch_val_loss))
    

# =====Comparison with current best model=====
# Testing newly trained model
with torch.no_grad():
        model.eval()
        val_loss = 0
        for i, (sources, targets) in enumerate(val_loader):
            sources, targets = sources.to(device), targets.to(device)
            output, h, c = model(sources)

            targets = torch.flatten(targets, end_dim=1)
            output = torch.flatten(output, end_dim=1)

            loss = criterion(output, targets)
            val_loss += loss

        # Validation loss of the newly trained model
        val_loss_new = val_loss / val_batches


# Load the current best model
curr_model_weights = mostRecetntModel(path)
print("Loading current best model: {}".format(curr_model_weights))
current_model = LSTM_LangModel(input_size, embed_size, hidden_size, output_size).to(device)
current_model.load_state_dict(torch.load(curr_model_weights, map_location=device))
# Testing old model
with torch.no_grad():
        current_model.eval()
        val_loss = 0
        for i, (sources, targets) in enumerate(val_loader):
            sources, targets = sources.to(device), targets.to(device)
            output, h, c = current_model(sources)

            targets = torch.flatten(targets, end_dim=1)
            output = torch.flatten(output, end_dim=1)

            loss = criterion(output, targets)
            val_loss += loss

        # Validation loss of the old model
        val_loss_old = val_loss / val_batches


if val_loss_new < val_loss_old: 
    print("The new model is better. Adding new model..")
    # Get current date
    today = datetime.today()
    formatted_date = today.strftime('%Y_%m_%d_%H_%M_%S')
    name = "LM_{}.pt".format(formatted_date)
    
    # Add new model
    torch.save(model.state_dict(), path + name)
    
    # Produce optimized version for Android
   
    # First we need to convert the model from the regular class (used for training) to the android-convertible class
    android_model = LSTM_LangModelForMobile(input_size, embed_size, hidden_size, output_size)
    android_model.load_state_dict(torch.load(path + name, map_location=device))
    
    # Produce the optimized version:
    print("Producing optimized version for Android..")
    model.eval()
    scripted_model = torch.jit.script(android_model)
    scripted_model.save(path+'optimized/'+'OPT_{}'.format(name))
    print("Optimization done.")
else:
    print("The new model was not better.")
