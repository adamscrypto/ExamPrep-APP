#!/usr/bin/env python3
import re
import os

def load_clean_from_file(path):
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
        m_diff = re.search(r'difficulty\s*=\s*Difficulty\.([A-Z]+)', b)
        
        if m_q and m_a and m_b and m_c and m_d and m_ans:
            clean_qs.append({
                'q': qtext,
                'a': m_a.group(1).strip(),
                'b': m_b.group(1).strip(),
                'c': m_c.group(1).strip(),
                'd': m_d.group(1).strip(),
                'ans': m_ans.group(1).strip(),
                'topic': m_top.group(1).strip() if m_top else 'General',
                'exp': m_exp.group(1).strip() if m_exp else 'Standard curriculum rationale.',
                'diff': m_diff.group(1).strip() if m_diff else 'MEDIUM'
            })
    return clean_qs

print("Science loader module ready")
