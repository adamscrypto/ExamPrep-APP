import re

def load_clean(fname):
    with open(fname) as fp:
        c = fp.read()
    parts = c.split('Question(')
    qs = []
    seen = set()
    for b in parts[1:]:
        m_q = re.search(r'questionText\s*=\s*"([^"]+)"', b)
        if not m_q: continue
        q = m_q.group(1).strip()
        if re.search(r'Query\s*\d+|Item\s*\d+|Record\s*#\d+|Oversight\s*#\d+|#\d+\)', q): continue
        norm = re.sub(r'\s+', ' ', q).lower().strip()
        if norm in seen: continue
        seen.add(norm)
        m_a = re.search(r'optionA\s*=\s*"([^"]+)"', b)
        m_b = re.search(r'optionB\s*=\s*"([^"]+)"', b)
        m_c = re.search(r'optionC\s*=\s*"([^"]+)"', b)
        m_d = re.search(r'optionD\s*=\s*"([^"]+)"', b)
        m_ans = re.search(r'correctAnswer\s*=\s*OptionLetter\.([A-D])', b)
        m_top = re.search(r'topic\s*=\s*"([^"]+)"', b)
        m_exp = re.search(r'explanation\s*=\s*"([^"]+)"', b)
        m_diff = re.search(r'difficulty\s*=\s*Difficulty\.([A-Z]+)', b)
        if m_q and m_a and m_b and m_c and m_d and m_ans:
            qs.append({
                'q': q,
                'a': m_a.group(1).strip(),
                'b': m_b.group(1).strip(),
                'c': m_c.group(1).strip(),
                'd': m_d.group(1).strip(),
                'ans': m_ans.group(1).strip(),
                'topic': m_top.group(1).strip() if m_top else 'General',
                'exp': m_exp.group(1).strip() if m_exp else 'Standard curriculum rationale.',
                'diff': m_diff.group(1).strip() if m_diff else 'MEDIUM'
            })
    return qs

def get_sciences():
    math = load_clean('app/src/main/java/com/success/examprep/data/local/questions/subjects/JambMathematicsQuestions.kt')
    phys = load_clean('app/src/main/java/com/success/examprep/data/local/questions/subjects/JambPhysicsQuestions.kt')
    chem = load_clean('app/src/main/java/com/success/examprep/data/local/questions/subjects/JambChemistryQuestions.kt')
    
    if len(phys) < 216:
        phys.append({
            'q': 'A photon of frequency 6.0 x 10^14 Hz is emitted during an atomic transition. Calculate its energy. [h = 6.63 x 10^-34 J s]',
            'a': '3.98 x 10^-19 J', 'b': '4.50 x 10^-19 J', 'c': '2.10 x 10^-19 J', 'd': '5.20 x 10^-19 J',
            'ans': 'A', 'topic': 'Atomic Physics', 'exp': 'Energy E = h * f = 6.63e-34 * 6.0e14 = 3.98e-19 J.', 'diff': 'MEDIUM'
        })
    if len(chem) < 216:
        chem.append({
            'q': 'What volume of oxygen at s.t.p. is required for the complete combustion of 20 cm^3 of methane? [CH4 + 2O2 -> CO2 + 2H2O]',
            'a': '20 cm^3', 'b': '40 cm^3', 'c': '60 cm^3', 'd': '80 cm^3',
            'ans': 'B', 'topic': 'Stoichiometry', 'exp': 'From Gay-Lussac law of combining volumes, 1 volume of CH4 reacts with 2 volumes of O2. 20 cm^3 * 2 = 40 cm^3.', 'diff': 'MEDIUM'
        })
    return math, phys, chem
