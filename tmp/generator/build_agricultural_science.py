#!/usr/bin/env python3
import sys
import os

sys.path.append('tmp/generator')
from common import render_kotlin_file, extract_clean_existing
import agric_data1
import agric_data2
import agric_data3
import agric_data4

FINAL_SEVEN_AGRIC = [
    ("The agricultural extension concept known as 'Training and Visit' (T&V) system is primarily characterized by:",
     "Regular, scheduled fortnightly visits by Village Extension Workers (VEWs) to contact farmers, coupled with systematic monthly training by subject matter specialists",
     "Broadcasting farming radio lectures once every calendar year",
     "Sending extension workers to live abroad permanently",
     "Distributing free agricultural textbooks to secondary schools only", "A", "Agricultural Extension", "MEDIUM",
     "The World Bank T&V system maintains a strict discipline of routine visits to progressive contact farmers who diffuse innovations to their neighbors."),
    ("In fish farming, the condition known as 'winter kill' or acute nocturnal fish mortality in overstocked ponds is caused by:",
     "Severe dissolved oxygen depletion in the early morning hours caused by overnight respiration of dense phytoplankton and fish",
     "Excessive accumulation of dissolved calcium carbonate",
     "Sudden evaporation of water from the pond surface",
     "Overfeeding of high-protein floating extruded pellets", "A", "Aquaculture", "MEDIUM",
     "Phytoplankton produce oxygen through photosynthesis only in sunlight; at night they consume dissolved oxygen, causing fatal hypoxia before dawn."),
    ("The method of estimating livestock liveweight on farms lacking livestock platform scales by measuring the animal's heart girth behind the shoulders with a graduated tape is:",
     "Weigh-band tape estimation (Shaeffer's formula)",
     "Water displacement volumetry",
     "Visual ocular estimation by three observers",
     "Bone density ultrasound scanning", "A", "Livestock Management", "EASY",
     "Heart girth circumference correlates strongly with live body mass across standard cattle, sheep, and swine breeds."),
    ("The primary reason for incorporating limestone or oyster shell grit into the commercial diets of laying hens is to provide:",
     "Dietary calcium essential for strong eggshell calcification and skeletal maintenance",
     "Crude fiber to stimulate intestinal peristalsis",
     "Digestible fat to enhance egg yolk pigmentation",
     "Trace vitamins to prevent rickets in day-old chicks", "A", "Poultry Nutrition", "EASY",
     "Eggshells consist of >95% calcium carbonate; insufficient dietary calcium results in soft-shelled eggs and bone osteoporosis."),
    ("The process of inducing sweating and moisture migration in freshly harvested seed yams or cassava roots to heal harvest wounds and thicken skin before storage is:",
     "Curing", "Dehydration", "Fumigation", "Blanching", "A", "Post-Harvest Management", "MEDIUM",
     "Curing at high relative humidity (85-90%) and warm temperature (28-32°C) stimulates suberization and wound periderm synthesis."),
    ("The physiological disorder of tomato fruits caused by localized calcium deficiency within developing fruit tissue during periods of fluctuating soil moisture is:",
     "Blossom-end rot", "Fruit splitting", "Catfacing", "Sunscald", "A", "Horticultural Pathology", "EASY",
     "Blossom-end rot manifests as a flat, dark leathery lesion at the distal floral end of the tomato fruit."),
    ("The chemical herbicide formulation category that kills all plant species upon direct contact without discriminating between weeds and cultivated crops is a:",
     "Non-selective (broad-spectrum) herbicide (e.g., Paraquat or Glyphosate)",
     "Selective post-emergence broadleaf herbicide",
     "Pre-emergence grass herbicide",
     "Growth-regulator systemic phenoxy herbicide", "A", "Weed Science", "EASY",
     "Non-selective herbicides like glyphosate kill any green plant tissue contacted and are used for pre-planting land clearing.")
]

def main():
    existing = extract_clean_existing('AgriculturalScience')
    pools = [agric_data1.AGRIC_POOL_1, agric_data2.AGRIC_POOL_2, agric_data3.AGRIC_POOL_3, agric_data4.AGRIC_POOL_4, FINAL_SEVEN_AGRIC]
    
    combined = []
    seen = set()
    for q in existing:
        norm = q['questionText'].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append(q)
            
    for p in pools:
        for item in p:
            norm = item[0].strip().lower()
            if norm not in seen:
                seen.add(norm)
                combined.append({
                    'questionText': item[0],
                    'optionA': item[1], 'optionB': item[2], 'optionC': item[3], 'optionD': item[4],
                    'correctAnswer': item[5], 'topic': item[6], 'difficulty': item[7],
                    'explanation': item[8], 'year': 2023
                })
                
    print(f"Total authentic Agricultural Science questions: {len(combined)}")
    assert len(combined) == 216, f"Expected 216 questions, got {len(combined)}"
    
    jamb = combined[:72]
    waec = combined[72:144]
    neco = combined[144:216]
    
    render_kotlin_file('Jamb', 'AgriculturalScience', jamb)
    render_kotlin_file('Waec', 'AgriculturalScience', waec)
    render_kotlin_file('Neco', 'AgriculturalScience', neco)
    print("Successfully generated all Agricultural Science question banks!")

if __name__ == '__main__':
    main()
