import os

from flask import Flask, request
from ingredient_parser import parse_ingredient

app = Flask(__name__)

@app.route('/breakdown_ingredient', methods=['GET'])
def breakdown_ingredient():
    ingredient = request.args.get('ingredient')
    if not ingredient:
        return {"error": "No ingredient provided"}, 400
    parsed_ingredient = parse_ingredient(ingredient)

    response = {
        "original": ingredient,
        "ingredient": parsed_ingredient.name,
        "quantity": parsed_ingredient.amount,
    }

    return response

if __name__ == "__main__":
    app.run(
        host=os.environ.get("FLASK_HOST", "0.0.0.0"),  # nosec B104
        port=int(os.environ.get("FLASK_PORT", 5000))
    )