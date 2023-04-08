import pickle

from model import LSTM_LangModel
from util import *
import torch
import math
from torch.utils.data import DataLoader
from torch.optim import Adam
from torch.optim.lr_scheduler import OneCycleLR

# Collect data
# TODO: Figure out how to get the data
path = ""
save_path = ""
data =[]

# random.shuffle(data)
train, val = data[:50000], data[93000:]
# train, val = data[:500], data[90000:90100]

with open(path + "vectorizer_50000_char_120_128_300.pickle", "rb") as f:
    tokenizer = pickle.load(f)
train_dataset = tokenizer.encode_dataset(train)
val_dataset = tokenizer.encode_dataset(val)

# Configure device if available
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
# Model hyperparams.
input_size = len(tokenizer.vocab) + 1
embed_size = 128
hidden_size = 1024
output_size = len(tokenizer.vocab) + 1

model = LSTM_LangModel(input_size, embed_size, hidden_size, output_size)
model = model.to(device=device)

# ========Main Training Loop=========

epochs = 40
batch_size = 64
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
flag = True
for epoch in range(1, epochs + 1):

    print("Epoch {}/{}".format(epoch, epochs))
    model.train()
    train_loss = 0
    optim.zero_grad()

    for i, (sources, targets) in enumerate(train_loader):
        print("\r[{}{}] Batch {}/{}".format(math.ceil((i + 1) / len(train_loader) * 40) * "=",
                                            (40 - math.ceil((i + 1) / len(train_loader) * 40)) * " ", i + 1,
                                            len(train_loader)), end="")
        if flag == True:
            flag = False
            print(sources.size())

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

        # Grad accumulation
        if (i / accumulation_steps == len(train_loader) / accumulation_steps) and (
                len(train_loader) % accumulation_steps != 0):
            loss = loss / (
                        len(train_loader) % accumulation_steps)  # The last batches may not be enough for a full accumulation step
        else:
            loss = loss / accumulation_steps

        # Backward pass & update weights
        loss.backward()

        # if (i+1) % accumulation_steps == 0:
        if (i + 1) % accumulation_steps == 0 or i == len(
                train_loader) - 1:  # If the finally batches can't if a full accumulation step
            #optim.step()
            scheduler.step()
            optim.zero_grad()

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
    if not best_loss:
        best_loss = epoch_val_loss
        torch.save(model.state_dict(),
                   save_path + "1layer_LSTM_LM_{}_{}_{}_{}_{}.pt".format(len(train), tokenizer.mode, input_size,
                                                                         embed_size, hidden_size))
    elif (best_loss > epoch_val_loss):
        best_loss = epoch_val_loss
        torch.save(model.state_dict(),
                   save_path + "1layer_LSTM_LM_{}_{}_{}_{}_{}.pt".format(len(train), tokenizer.mode, input_size,
                                                                         embed_size, hidden_size))
