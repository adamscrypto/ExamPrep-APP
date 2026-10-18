from curator.data_sciences import load_clean

bio_base = load_clean('app/src/main/java/com/success/examprep/data/local/questions/subjects/JambBiologyQuestions.kt')
seen_bio = {q['q'].strip().lower() for q in bio_base}
bio_all = list(bio_base)

# We will import the pools from our tested definition
import subprocess
