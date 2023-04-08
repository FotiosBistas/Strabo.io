import torch
import torch.nn as nn

class LSTM_LangModel(nn.Module):
    def __init__(self, input_size, embed_size, hidden_size, output_size):
        super(LSTM_LangModel, self).__init__()
        self.hidden_size = hidden_size

        self.embed = nn.Embedding(input_size, embed_size, padding_idx=0)
        self.lstm = nn.LSTM(embed_size, hidden_size, batch_first=True)
        self.dense = nn.Linear(hidden_size, output_size)
        self.dropout = nn.Dropout(0.2)

    def forward(self, x):
        input_embedded = self.embed(x)
        output_lstm, (h, c) = self.lstm(input_embedded)
        output = self.dropout(output_lstm)
        output = self.dense(output)
        return output, h, c
