import os

import spacy
from flask import Flask, request, render_template_string
from random_word import RandomWords

app = Flask(__name__)
nlp = spacy.load("en_core_web_md")
random_words = RandomWords()


@app.route("/", methods=["GET"])
def hello():
    print(f"Received request: {request.url}")
    return render_template_string("Word Engine API")


@app.route("/similarity/<word>/<other_word>", methods=["GET"])
def similarity(word, other_word):
    print(f"Received request: {request.url}")

    nlp_word = nlp(word)
    nlp_other_word = nlp(other_word)
    score = nlp_word.similarity(nlp_other_word)

    return render_template_string(str(score))


@app.route("/random", methods=["GET"])
def random():
    print(f"Received request: {request.url}")
    return render_template_string(random_words.get_random_word())


if __name__ == "__main__":
    host = os.getenv("FLASK_HOST")
    if host is None:
        raise Exception("FLASK_HOST environment variable is not set")
    port = os.getenv("FLASK_PORT")
    if port is None:
        raise Exception("FLASK_PORT environment variable is not set")
    app.run(host=host, port=5000)
