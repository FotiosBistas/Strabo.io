import pickle
import sys
from model import LSTM_LangModel
from util import *
import torch
import math
import os
import random
from datetime import datetime
from torch.utils.data import DataLoader
from torch.optim import Adam
from torch.optim.lr_scheduler import OneCycleLR


# Path to save model
path = "saved_models/"
# Collect sentences in Greek for training
data = sys.argv[1]

random.shuffle(data)
# 85/15 split
split = math.ceil(len(data)*0.85)
if split > 2000:
    train, val = data[:2000], data[2000:]
else:
    train, val = data[:split], data[split:]



with open(path + "vectorizer_50000_char_120_32_512.pickle", "rb") as f:
    tokenizer = pickle.load(f)

train_dataset = tokenizer.encode_dataset(train)
val_dataset = tokenizer.encode_dataset(val)

# Configure device if available
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
# Model hyperparams.
input_size = len(tokenizer.vocab) + 1
embed_size = 32
hidden_size = 512
output_size = len(tokenizer.vocab) + 1

# Load the current best model
current_model = LSTM_LangModel(input_size, embed_size, hidden_size, output_size)
current_model.load_state_dict(torch.load(path + "LSTM_LM_50000_char_120_32_512.pt"))

model = LSTM_LangModel(input_size, embed_size, hidden_size, output_size)
model = model.to(device=device)

# ========Main Training Loop=========

epochs = 20
batch_size = 80
#accumulation_steps = 1

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

    print("Epoch {}/{}".format(epoch, epochs))
    model.train()
    train_loss = 0
    optim.zero_grad()

    for i, (sources, targets) in enumerate(train_loader):
        print("\r[{}{}] Batch {}/{}".format(math.ceil((i + 1) / len(train_loader) * 40) * "=",
                                            (40 - math.ceil((i + 1) / len(train_loader) * 40)) * " ", i + 1,
                                            len(train_loader)), end="")

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
    print("\nLoss: (train){} (val){}".format(epoch_train_loss, epoch_val_loss))
    
# comparisson with current best model
print("\n")
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

# Testing old model
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

        # Validation loss of the old model
        val_loss_old = val_loss / val_batches


if val_loss_new < val_loss_old: 
    print("The new model is better. Replacing the old model..")
    # Get current date
    today = datetime.today()
    formatted_date = today.strftime('%d/%m/%Y')
    save_path = path+"old/"+formatted_date+"/"
    os.mkdir(save_path)
    # Save old model to dir
    torch.save(model.state_dict(), save_path+"LSTM_LM_{}_{}_{}_{}_{}_new.pt".format(50000, tokenizer.mode, input_size, embed_size, hidden_size))
    # Replace with new model
    torch.save(model.state_dict(), path+"LSTM_LM_{}_{}_{}_{}_{}_new.pt".format(50000, tokenizer.mode, input_size, embed_size, hidden_size))
    print("Replacement done.")
else:
    print("The new model was not better.")