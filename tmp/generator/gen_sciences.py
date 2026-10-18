#!/usr/bin/env python3
import sys
sys.path.append('/tmp/generator')
from common import extract_clean_existing, render_kotlin_file

def process_sciences():
    # Mathematics
    math_qs = extract_clean_existing('Mathematics')
    print(f"Loaded {len(math_qs)} clean Math questions")
    assert len(math_qs) >= 236
    render_kotlin_file('Jamb', 'Mathematics', math_qs[0:80])
    render_kotlin_file('Waec', 'Mathematics', math_qs[80:160])
    render_kotlin_file('Neco', 'Mathematics', math_qs[160:236])
    
    # Physics
    phy_qs = extract_clean_existing('Physics')
    print(f"Loaded {len(phy_qs)} clean Physics questions")
    assert len(phy_qs) >= 215
    render_kotlin_file('Jamb', 'Physics', phy_qs[0:72])
    render_kotlin_file('Waec', 'Physics', phy_qs[72:144])
    render_kotlin_file('Neco', 'Physics', phy_qs[144:215])

    # Chemistry
    chem_qs = extract_clean_existing('Chemistry')
    print(f"Loaded {len(chem_qs)} clean Chemistry questions")
    assert len(chem_qs) >= 215
    render_kotlin_file('Jamb', 'Chemistry', chem_qs[0:72])
    render_kotlin_file('Waec', 'Chemistry', chem_qs[72:144])
    render_kotlin_file('Neco', 'Chemistry', chem_qs[144:215])

if __name__ == '__main__':
    process_sciences()
