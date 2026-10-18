#!/usr/bin/env python3
"""
Modular builder for the 14 subjects in ExamPrep CBT.
Ensures 216 unique questions per subject (72 JAMB, 72 WAEC, 72 NECO).
Zero duplicates, zero synthetic tags.
"""

import re
import os

def load_existing_clean(subject):
    path = f"app/src/main/java/com/success/examprep/data/local/questions/subjects/Jamb{subject}Questions.kt"
    if not os.path.exists(path):
        return []
    with open(path) as f:
        content = f.read()
    parts = content.split("Question(")
    clean_qs = []
    seen = set()
    for b in parts[1:]:
        m_q = re.search(r'questionText\s*=\s*\"([^\"]+)\"', b)
        if not m_q:
            continue
        qtext = m_q.group(1).strip()
        # Filter synthetic tags
        if re.search(r'Query\s*\d+|Item\s*\d+|Record\s*#\d+|Oversight\s*#\d+|#\d+\)', qtext):
            continue
        norm = qtext.lower().strip()
        if norm in seen:
            continue
        seen.add(norm)
        
        m_a = re.search(r'optionA\s*=\s*\"([^\"]+)\"', b)
        m_b = re.search(r'optionB\s*=\s*\"([^\"]+)\"', b)
        m_c = re.search(r'optionC\s*=\s*\"([^\"]+)\"', b)
        m_d = re.search(r'optionD\s*=\s*\"([^\"]+)\"', b)
        m_ans = re.search(r'correctAnswer\s*=\s*OptionLetter\.([A-D])', b)
        m_top = re.search(r'topic\s*=\s*\"([^\"]+)\"', b)
        m_exp = re.search(r'explanation\s*=\s*\"([^\"]+)\"', b)
        
        if m_q and m_a and m_b and m_c and m_d and m_ans:
            clean_qs.append({
                'q': qtext,
                'a': m_a.group(1).strip(),
                'b': m_b.group(1).strip(),
                'c': m_c.group(1).strip(),
                'd': m_d.group(1).strip(),
                'ans': m_ans.group(1).strip(),
                'topic': m_top.group(1).strip() if m_top else 'General',
                'exp': m_exp.group(1).strip() if m_exp else 'Standard curriculum rationale.'
            })
    return clean_qs

print("Helper ready")
