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


class LSTM_LangModelForMobile(nn.Module):
    
    def __init__(self, input_size, embed_size, hidden_size, output_size):
        super(LSTM_LangModel, self).__init__()
        self.hidden_size = hidden_size

        self.embed = nn.Embedding(input_size, embed_size, padding_idx=0)
        self.lstm = nn.LSTM(embed_size, hidden_size, batch_first=True)
        self.dense = nn.Linear(hidden_size, output_size)
        self.dropout = nn.Dropout(0.5)

    def forward(self, x, h0, c0):
        input_embedded = self.embed(x)
        output_lstm, (h, c) = self.lstm(input_embedded, (h0, c0))
        output = self.dropout(output_lstm)
        output = self.dense(output)
        return output, h, c

    @torch.jit.export
    def init_hidden(self):
        """
        Helper function to initiate the hidden states
        fed to the model in the first forward call.
        :return: cell, hidden  ----> Tensor.Zero
        """

        # hidden_shape: (num_layers, batch_size, hidden_size)
        hidden = torch.zeros(1, self.hidden_size)
        cell = torch.zeros(1, self.hidden_size)
        return hidden, cell