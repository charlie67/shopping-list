import csv
from torch.utils.data import Dataset, DataLoader
import torch
from torch import nn
from transformers import BertForSequenceClassification, BertTokenizer
from torch.utils.data import DataLoader
from tqdm import tqdm
from sklearn.model_selection import train_test_split

import util


class CustomDataset(Dataset):
    def __init__(self, texts, labels, tokenizer, max_length=128):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = self.texts[idx]
        label = self.labels[idx]

        encoding = self.tokenizer(
            text,
            truncation=True,
            padding='max_length',
            max_length=self.max_length,
            return_tensors='pt'
        )

        input_ids = encoding['input_ids'].squeeze()
        attention_mask = encoding['attention_mask'].squeeze()

        return {
            'input_ids': input_ids,
            'attention_mask': attention_mask,
            'label': torch.tensor(label, dtype=torch.long)
        }


if __name__ == '__main__':
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Using device: {device}")

    ingredients_ft, steps_ft, description_ft = util.extract_data()

    # Assuming labels are integers (0 for ingredient, 1 for step, 2 for description)
    all_texts = steps_ft + ingredients_ft + description_ft
    labels = [1] * len(steps_ft) + [0] * len(ingredients_ft) + [2] * len(description_ft)

    # Split the dataset into training and test sets
    train_texts, test_texts, train_labels, test_labels = train_test_split(all_texts, labels, test_size=0.2, random_state=42)

    model = BertForSequenceClassification.from_pretrained('bert-base-uncased',
                                                          num_labels=3).to(device)  # 2 labels: ingredient, step
    tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')

    custom_dataset_train = CustomDataset(texts=train_texts, labels=train_labels, tokenizer=tokenizer)
    custom_dataset_test = CustomDataset(texts=test_texts, labels=test_labels, tokenizer=tokenizer)

    dataloader_train = DataLoader(custom_dataset_train, batch_size=64, shuffle=True)
    dataloader_test = DataLoader(custom_dataset_test, batch_size=64, shuffle=False)

    optimizer = torch.optim.AdamW(model.parameters(), lr=1e-5)
    criterion = nn.CrossEntropyLoss()

    # Number of training epochs
    num_epochs = 1

    # Training loop
    for epoch in range(num_epochs):
        model.train()
        total_loss = 0

        for batch in tqdm(dataloader_train, desc=f'Epoch {epoch + 1}/{num_epochs}'):
            input_ids = batch['input_ids'].to(device)
            attention_mask = batch['attention_mask'].to(device)
            labels = batch['label'].to(device)

            # Forward pass
            outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
            loss = outputs.loss

            # Backward pass and optimization
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            total_loss += loss.item()

        average_loss = total_loss / len(dataloader_train)
        print(f'Training Epoch {epoch + 1}/{num_epochs}, Average Loss: {average_loss}')

        # Evaluate on the test set
        model.eval()
        correct = 0
        total = 0

        with torch.no_grad():
            for batch in tqdm(dataloader_test, desc=f'Testing Epoch {epoch + 1}/{num_epochs}'):
                input_ids = batch['input_ids'].to(device)
                attention_mask = batch['attention_mask'].to(device)
                labels = batch['label'].to(device)

                outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
                _, predicted = torch.max(outputs.logits, 1)

                total += labels.size(0)
                correct += (predicted == labels).sum().item()

        accuracy = correct / total
        print(f'Testing Epoch {epoch + 1}/{num_epochs}, Accuracy: {accuracy}')

        model.save_pretrained(f'bert/three-labels/model-{epoch}')
        tokenizer.save_pretrained(f'bert/three-labels/tokenizer-{epoch}')

    # Save the fine-tuned model
    model.save_pretrained('bert/three-labels/model-final')
    tokenizer.save_pretrained('bert/three-labels/tokenizer-final')

