from transformers import BertForSequenceClassification, BertTokenizer
import torch
import torch.nn.functional as F

# Load the fine-tuned model and tokenizer
model_path = "bert/three-labels/model-0"
tokenizer_path = "bert/three-labels/tokenizer-0"

model = BertForSequenceClassification.from_pretrained(model_path)
tokenizer = BertTokenizer.from_pretrained(tokenizer_path)

# Example text to classify
new_text = ["This recipe is a great way to use up leftover chicken. It is easy to prepare and very tasty as well. I usually serve it with Spanish rice and Mexican-style canned corn.",
            "100 cups tomato soup",
            "mix the soup and the soap into a fine paste",
            "Mutton curry also referred to as goat curry is a meat curry with deep flavours.",
            "combine 2 cups of flour, 2 tablespoons of sugar, 1 teaspoon of salt, and 1 teaspoon of baking powder in a bowl",
            "Almost killed me but it was worth it :) 10/10 would recommend"]
for t in new_text:
    # Tokenize the text
    inputs = tokenizer(t, return_tensors="pt")

    # Make a prediction
    with torch.no_grad():
        outputs = model(**inputs)

    # Apply softmax to get probabilities
    probs = F.softmax(outputs.logits, dim=1)

    # Get the predicted class
    predicted_class = torch.argmax(probs).item()

    # Display the result
    print(f"Text: {t}")
    print(f"Predicted class: {predicted_class}")

    # Convert probabilities to percentages
    probs_percentage = (probs * 100).squeeze().tolist()
    print(f"Probabilities: {probs_percentage}")