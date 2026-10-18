#!/usr/bin/env python3
import sys
import os

sys.path.append('tmp/generator')
from common import render_kotlin_file, extract_clean_existing
import geo_data1, geo_data2, geo_data3, geo_data4

FINAL_TWENTY_EIGHT_GEO = [
    ("The natural deep-water bay in southeastern Nigeria that serves as the administrative capital and port of Cross River State is located on the estuary of the:",
     "Calabar River (Cross River estuary)", "Ogun River", "Sokoto River", "Hadejia River", "A", "Ports of Nigeria", "EASY",
     "Calabar Port possesses a protected estuarine anchorage that served as Nigeria's historic eastern colonial capital."),
    ("The highest mountain peak in West Africa, an active stratovolcano rising to 4,095 meters near the Atlantic coast of Cameroon, is:",
     "Mount Cameroon (Fako)", "Mount Nimba", "Loma Mountains", "Tibesti Mountains", "A", "Relief of West Africa", "EASY",
     "Mount Cameroon experiences massive orographic precipitation (Debundscha receives over 10,000 mm of rain annually)."),
    ("The primary reason why the Guinea Coast of West Africa experiences two distinct rainfall peaks (bimodal rainfall distribution) is:",
     "The double northward and southward seasonal passage of the overhead sun and the ITCZ across equatorial latitudes",
     "The sudden melting of polar ice caps twice per year",
     "The occurrence of two solar eclipses each year",
     "The seasonal reversal of Pacific ocean currents", "A", "West African Climatology", "MEDIUM",
     "As the ITCZ advances north in June and retreats south in September, equatorial coastal regions receive two distinct rainy seasons separated by the August break."),
    ("The short, brief dry spell that interrupts the heavy rainy season in southern Nigeria during late July and August is popularly called the:",
     "August Break (Little Dry Season)", "Harmattan", "Monsoon surge", "Equinoctial gale", "A", "Nigerian Climatology", "EASY",
     "The August break results from atmospheric stability when the Atlantic subtropical anticyclone strengthens temporarily."),
    ("The largest urban metropolitan city in Nigeria and the primary economic hub of West Africa is:",
     "Lagos", "Kano", "Ibadan", "Abuja", "A", "Urban Geography of Nigeria", "EASY",
     "Lagos generates over 30% of Nigeria's Gross Domestic Product and hosts the nation's principal maritime ports."),
    ("The Federal Capital Territory (FCT) of Nigeria, officially chosen in 1976 and inaugurated in 1991 to replace Lagos as the national capital, is:",
     "Abuja", "Kaduna", "Lokoja", "Enugu", "A", "Settlement Geography", "EASY",
     "Abuja was selected due to its central geographic location, neutral ethnic neutrality, pleasant climate, and ample room for planned urban expansion."),
    ("The large sedimentary rock formation in northwestern Nigeria drained by the River Rima and River Sokoto that contains gypsum and phosphate deposits is the:",
     "Sokoto Basin (Iullemmeden Basin)", "Chad Basin", "Benue Trough", "Dahomey Embayment", "A", "Geology of Nigeria", "MEDIUM",
     "The Sokoto Basin consists of Cretaceous and Tertiary marine and continental sediments supporting limestone and gypsum mining."),
    ("The linear tectonic rift depression extending from the Niger Delta northeastward towards the Chad Basin, filled with Cretaceous sediments and lead-zinc ores, is the:",
     "Benue Trough", "Bida Basin", "Sokoto Basin", "Jos Plateau", "A", "Geology of Nigeria", "MEDIUM",
     "The Benue Trough originated as an aborted aulacogen arm of the South Atlantic triple junction during the breakup of Gondwana."),
    ("The prominent inselberg formation in Niger State near the boundary with Abuja, known for its massive monolithic granite outcrop, is:",
     "Zuma Rock", "Olumo Rock", "Aso Rock", "Wase Rock", "A", "Nigerian Geomorphology", "EASY",
     "Zuma Rock is a towering monolithic granitic inselberg rising over 300 meters above the surrounding savanna plains."),
    ("The sacred historical granitic outcrop located in Abeokuta, Ogun State, that served as a natural defensive fortress for the Egba people during 19th-century wars, is:",
     "Olumo Rock", "Zuma Rock", "Aso Rock", "Idanre Hills", "A", "Nigerian Geomorphology", "EASY",
     "Olumo Rock features natural caves and shelters used by historical Egba refugees during inter-tribal warfare."),
    ("The spectacular UNESCO World Heritage cultural landscape and ancient mountainous kingdom characterized by precipitous granite domes in Ondo State is:",
     "Idanre Hills (Oke Idanre)", "Chappal Waddi", "Obudu Plateau", "Shere Hills", "A", "Nigerian Geomorphology", "EASY",
     "Idanre Hills rise 900 meters above sea level, accessed by 660 steps leading to the ancient hilltop settlement."),
    ("The picturesque montane plateau resort located in Cross River State near the Cameroon border, known for its temperate climate, cattle ranch, and cable car, is:",
     "Obudu Mountain Resort (Obudu Plateau)", "Mambilla Plateau", "Jos Plateau", "Biu Plateau", "A", "Nigerian Tourism", "EASY",
     "Rising over 1,500 meters, Obudu Plateau enjoys cool temperatures (~15°C to 22°C) in contrast to coastal heat."),
    ("The method of representing relief on a topographic map using fine lines drawn parallel to the direction of maximum water flow (slope) is:",
     "Hachuring (Hachures)", "Contouring", "Layer tinting", "Hill shading", "A", "Cartography", "MEDIUM",
     "Thick, closely spaced hachures indicate steep cliffs, while thin, widely spaced lines denote gentle inclines."),
    ("The method of showing relief on maps by coloring different elevation bands with progressive tints (e.g., green for lowlands, brown for mountains) is:",
     "Layer tinting (Hypsometric tinting)", "Hachures", "Form lines", "Spot heights", "A", "Cartography", "EASY",
     "Hypsometric tinting provides intuitive visual comprehension of macro-relief across regional maps."),
    ("The precise height of a specific ground point above mean sea level recorded on a map adjacent to a small dot is a:",
     "Spot height", "Bench mark", "Trigonometrical station", "Contour line", "A", "Cartography", "EASY",
     "Spot heights provide exact elevations for road junctions, mountain peaks, and depressions."),
    ("A permanent surveyor's stone or brass plaque cut into a wall or pillar showing the exact surveyed height above sea level is a:",
     "Bench mark (BM)", "Spot height", "Trig point", "Form line", "A", "Cartography", "EASY",
     "Surveyors use bench marks as verified datum points to establish local leveling lines."),
    ("The ratio of the map distance to the corresponding ground distance expressed in fraction form with a numerator of 1 (such as 1:50,000) is the:",
     "Representative Fraction (RF)", "Statement scale", "Linear graphic scale", "Areal scale", "A", "Map Scales", "EASY",
     "An RF of 1:50,000 signifies that 1 unit on the map represents 50,000 identical units on the actual ground."),
    ("If a map with a scale of 1:50,000 is reduced to half its original linear size, the new scale of the reduced map will be:",
     "1:100,000", "1:25,000", "1:200,000", "1:10,000", "A", "Map Scales", "MEDIUM",
     "Reducing map dimensions by half doubles the denominator of the RF scale, making it 1:100,000."),
    ("The bearing of a geographic point B measured from point A is 120°. What is the reciprocal back bearing of A measured from B?",
     "300° (120° + 180°)", "60°", "240°", "210°", "A", "Map Reading", "EASY",
     "When forward bearing is less than 180°, add 180° to obtain the reciprocal back bearing."),
    ("The compass direction located midway between South and South-West on a 16-point magnetic compass is:",
     "South-South-West (SSW)", "West-South-West (WSW)", "South-East (SE)", "South-South-East (SSE)", "A", "Navigation", "EASY",
     "The 16-point compass subdivides the 360° horizon into 22.5° intervals: S (180°) -> SSW (202.5°) -> SW (225°)."),
    ("The agricultural farming system where livestock rearing is integrated with the cultivation of food and cash crops on the same commercial farm is:",
     "Mixed farming", "Monoculture", "Nomadic herding", "Shifting cultivation", "A", "Agricultural Systems", "EASY",
     "In mixed farming, crop residues feed livestock, and livestock manure fertilizes arable crop fields."),
    ("The seasonal migration of pastoral herdsmen (such as Fulani pastoralists) with their cattle between highlands in wet seasons and river valleys in dry seasons is:",
     "Transhumance", "Urbanization", "Emigration", "Commuting", "A", "Pastoralism", "EASY",
     "Transhumance represents an ecologically rational response to seasonal variations in water and forage availability."),
    ("The specialized branch of geography that analyzes the spatial distribution of human diseases, health outcomes, and healthcare facilities is:",
     "Medical (Health) Geography", "Biogeography", "Geomorphology", "Climatology", "A", "Human Geography", "EASY",
     "Medical geographers map disease vectors (e.g., malaria mosquito breeding habitats, tsetse belts) to design epidemiological interventions."),
    ("The geographic boundary enclosing the tributary drainage area that contributes all surface runoff into a single main river system is the:",
     "Watershed (Drainage basin / Catchment area)", "Floodplain", "Estuary", "Continental shelf", "A", "Hydrology", "EASY",
     "The watershed boundary ridge (interfluve) divides precipitation drainage between adjacent river basins."),
    ("The prominent inland delta in West Africa where the River Niger splits into an intricate network of channels, lakes, and marshes in Mali is the:",
     "Macina (Inner Niger Delta)", "Niger Delta", "Sudd", "Okavango Delta", "A", "African Rivers", "MEDIUM",
     "The Inner Niger Delta in Mali supports extensive seasonal rice cultivation and flood-recession pasture for pastoralists."),
    ("The shallow, swampy inland wetland in South Sudan formed where the White Nile spreads across an immense flat clay basin is the:",
     "The Sudd", "Lake Chad", "Lake Victoria", "Okavango Delta", "A", "African Wetlands", "EASY",
     "The Sudd is one of the world's largest freshwater wetlands, covered with floating papyrus reeds that impede navigation."),
    ("The prominent salt lake located in the Afar Depression of Djibouti, recognized as the lowest land elevation point in Africa (-155 meters), is:",
     "Lake Assal", "Lake Turkana", "Lake Natron", "Lake Magadi", "A", "African Geomorphology", "MEDIUM",
     "Lake Assal lies 155 meters below sea level in the hyper-arid Afar triple junction rift depression."),
    ("The atmospheric effect in large industrial metropolises where tall concrete buildings, asphalt roads, and vehicle emissions cause central urban temperatures to be higher than surrounding rural suburbs is the:",
     "Urban Heat Island (UHI) effect", "Greenhouse effect", "Thermal inversion", "Coriolis deflection", "A", "Urban Climatology", "EASY",
     "Thermal mass in concrete, anthropogenic waste heat, and reduced vegetative evapotranspiration create urban heat islands.")
]

def main():
    existing = extract_clean_existing('Geography')
    pools = [geo_data1.GEO_POOL_1, geo_data2.GEO_POOL_2, geo_data3.GEO_POOL_3, geo_data4.GEO_POOL_4, FINAL_TWENTY_EIGHT_GEO]
    
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
                
    print(f"Total authentic Geography questions: {len(combined)}")
    assert len(combined) == 216, f"Expected 216 questions, got {len(combined)}"
    
    jamb = combined[:72]
    waec = combined[72:144]
    neco = combined[144:216]
    
    render_kotlin_file('Jamb', 'Geography', jamb)
    render_kotlin_file('Waec', 'Geography', waec)
    render_kotlin_file('Neco', 'Geography', neco)
    print("Successfully generated all Geography question banks!")

if __name__ == '__main__':
    main()
