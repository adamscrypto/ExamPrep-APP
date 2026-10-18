# English curator
from curator.curate_sciences import load_clean

def build_english_bank():
    base = load_clean('app/src/main/java/com/success/examprep/data/local/questions/subjects/JambEnglishQuestions.kt')
    seen = {re_clean(x["q"]) for x in base}
    items = list(base)
    return items

def re_clean(text):
    import re
    return re.sub(r'\s+', ' ', text).lower().strip()
