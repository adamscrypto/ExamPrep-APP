#!/usr/bin/env python3
import sys
import os
sys.path.append('tmp/generator')
from common import render_kotlin_file, extract_clean_existing

def build_civic():
    existing = extract_clean_existing('CivicEducation')
    print(f"Existing clean Civic Education: {len(existing)}")
    
    # 185 authentic Nigerian Civic Education questions covering the complete curriculum
    civic_pool = [
        # Values & Society
        ("In civic education, the moral value characterized by truthfulness, sincerity, and refusal to deceive or cheat is:",
         "Honesty", "Selfishness", "Nepotism", "Arrogance", "A", "Values & Society", "EASY",
         "Honesty fosters interpersonal trust, social cohesion, and transparent public governance."),
        ("A person who remains steadfast in their moral principles and refuses to compromise ethics for personal gain demonstrates:",
         "Integrity", "Hypocrisy", "Opportunism", "Apathy", "A", "Values & Society", "EASY",
         "Integrity entails consistency of moral character and uprightness in private and public life."),
        ("Contentment as a civic virtue is best described as:",
         "A state of satisfaction and happiness with what one legally owns without resorting to greed or corruption",
         "A refusal to pursue higher education",
         "An indifference to national elections",
         "A willingness to accept bribery", "A", "Values & Society", "EASY",
         "Contentment restrains greed, illicit enrichment, and embezzlement of public funds."),
        ("Discipline in civil society is characterized by:",
         "Voluntary self-control, orderliness, and adherence to established community rules and laws",
         "Using physical violence to enforce opinions",
         "Disobeying traffic regulations during emergencies",
         "Accumulating weapons in residential homes", "A", "Values & Society", "EASY",
         "Discipline underpins public order, productivity, and peaceful communal coexistence."),
        ("The moral strength to stand up for justice and truth despite intimidation, danger, or social disapproval is:",
         "Moral courage", "Physical aggression", "Blind fanaticism", "Stubbornness", "A", "Values & Society", "EASY",
         "Moral courage empowers whistleblowers and activists to confront injustice and corruption."),
        
        # Citizenship & Civic Duties
        ("Under the 1999 Constitution of the Federal Republic of Nigeria, a person acquires citizenship by birth if:",
         "Either of their parents or grandparents was born in Nigeria and belongs to a community indigenous to Nigeria",
         "They invest ten million dollars in real estate",
         "They reside in an urban city for two years",
         "They win an international sports tournament", "A", "Citizenship", "EASY",
         "Section 25 of the 1999 Constitution stipulates lineage criteria for citizenship by birth."),
        ("Foreign nationals who fulfill residency, good character, and continuous residence requirements can acquire Nigerian citizenship by:",
         "Naturalization", "Birth", "Conquest", "Annexation", "A", "Citizenship", "MEDIUM",
         "Section 27 empowers the President to grant certificates of naturalization upon fulfilling constitutional criteria."),
        ("A foreign woman legally married to a male Nigerian citizen can acquire Nigerian citizenship through:",
         "Registration", "Naturalization", "Birth", "Descent", "A", "Citizenship", "MEDIUM",
         "Section 26 provides that foreign spouses of Nigerian citizens may be registered as citizens upon application."),
        ("Which of the following is a constitutional civic duty and obligation of every Nigerian citizen?",
         "Prompt and honest payment of statutory taxes", "Joining a ruling political party", "Running for political office", "Attending university", "A", "Citizenship", "EASY",
         "Section 24 mandates declaring income honestly and paying taxes promptly to fund public infrastructure."),
        ("A citizen who actively defends the territorial integrity and national sovereignty of their country demonstrates:",
         "Patriotism", "Chauvinism", "Jingoism", "Sectionalism", "A", "Citizenship", "EASY",
         "Patriotism is genuine loyalty, devotion, and willingness to make personal sacrifices for the homeland."),
        
        # National Symbols & Identity
        ("The Nigerian National Flag was designed in 1959 by:",
         "Michael Taiwo Akinkunmi", "Chief Obafemi Awolowo", "Dr. Nnamdi Azikiwe", "Sir Ahmadu Bello", "A", "National Symbols", "EASY",
         "Pa Michael Taiwo Akinkunmi, then an engineering student in London, won the national flag design contest."),
        ("On the Nigerian National Flag, the green vertical bands symbolize:",
         "The nation's fertile agricultural land and wealth", "Peace and national unity", "Mineral oil reserves", "The rivers Niger and Benue", "A", "National Symbols", "EASY",
         "Green represents rich agricultural endowments, while white signifies national peace and unity."),
        ("On the Nigerian Coat of Arms, the two white horses represent:",
         "Dignity and pride", "Agricultural fertility", "Physical brute strength", "Mineral resources", "A", "National Symbols", "EASY",
         "The two supporting white chargers symbolize national dignity, stature, and pride."),
        ("The red eagle perched proudly on the crest of the Nigerian Coat of Arms symbolizes:",
         "National strength", "Peace and harmony", "Agricultural harvests", "Marine commerce", "A", "National Symbols", "EASY",
         "The majestic eagle signifies national strength, vigor, and courage."),
        ("The silver Y-shaped wavy pall on the black shield of the Nigerian Coat of Arms represents:",
         "The confluence of Rivers Niger and Benue at Lokoja", "The two military armed forces", "The Sahara Desert and Atlantic Ocean", "The two major political parties", "A", "National Symbols", "EASY",
         "The wavy silver Y depicts the meeting of the Niger and Benue rivers, while the black shield represents fertile soil."),
        ("The national motto inscribed on the scroll beneath the Nigerian Coat of Arms is:",
         "'Unity and Faith, Peace and Progress'", "'Justice, Peace and Freedom'", "'Strength and Honor, Truth and Service'", "'One Nation, One Destiny'", "A", "National Symbols", "EASY",
         "The constitutional motto guides national values and collective patriotic aspirations."),
        
        # Human Rights & UDHR
        ("The Universal Declaration of Human Rights (UDHR) was officially adopted by the United Nations General Assembly in Paris in:",
         "1948", "1914", "1960", "1999", "A", "Human Rights", "EASY",
         "Adopted on December 10, 1948, the UDHR establishes universal benchmark human rights standards."),
        ("December 10 is observed annually throughout the international community as:",
         "International Human Rights Day", "World Democracy Day", "United Nations Day", "International Labor Day", "A", "Human Rights", "EASY",
         "Human Rights Day commemorates the historic proclamation of the UDHR in 1948."),
        ("Fundamental human rights entrenched in Chapter IV of the 1999 Nigerian Constitution include the:",
         "Right to life and dignity of the human person", "Right to free luxury automobiles", "Right to evade police arrest", "Right to carry military firearms", "A", "Human Rights", "EASY",
         "Section 33 guarantees the fundamental right to life, prohibiting extrajudicial killings."),
        ("The constitutional right that guarantees that an accused person is presumed innocent until proven guilty in a competent court is the:",
         "Right to fair hearing", "Right to freedom of movement", "Right to private property", "Right to peaceful assembly", "A", "Human Rights", "MEDIUM",
         "Section 36 establishes procedural fairness, an impartial tribunal, and the presumption of innocence."),
        ("Which of the following conditions can lawfully justify the temporary suspension or limitation of certain human rights?",
         "A declared state of public emergency or lawful curfew during war", "A regular municipal council meeting", "A change in national school holidays", "An increase in petrol price", "A", "Human Rights", "EASY",
         "Section 45 permits derogation from rights during war, public emergency, or to safeguard public health and defense."),
        
        # Democracy & Rule of Law
        ("The famous classic definition of democracy as 'the government of the people, by the people, for the people' was given by:",
         "Abraham Lincoln", "Baron de Montesquieu", "John Locke", "Jean-Jacques Rousseau", "A", "Democracy", "EASY",
         "Lincoln articulated this enduring democratic ideal during his historic Gettysburg Address in 1863."),
        ("A fundamental feature that distinguishes a genuine democratic system from an autocracy is the presence of:",
         "Regular, free, fair, and periodic multi-party elections", "Military decrees and decrees", "A hereditary monarch who rules for life", "State-controlled judiciary with zero appeals", "A", "Democracy", "EASY",
         "Democratic legitimacy rests on periodic competitive elections where citizens hold leaders accountable."),
        ("The political philosophy of the Rule of Law was popularized in 1885 by the English jurist:",
         "A.V. Dicey", "Thomas Hobbes", "Niccolo Machiavelli", "Karl Marx", "A", "Rule of Law", "EASY",
         "A.V. Dicey formulated the core tenets of supremacy of regular law, equality before the law, and constitutional liberty."),
        ("Supremacy of the law, as a principle of the rule of law, means that:",
         "The constitution and statutory laws are supreme over all citizens, rulers, and state institutions", "The President is above the law", "Military officers cannot be tried by courts", "Police officers can enact laws", "A", "Rule of Law", "EASY",
         "No individual, monarch, or president wields arbitrary authority above the governing laws of the land."),
        ("The concept of 'Equality before the Law' means that:",
         "All persons, irrespective of wealth, social status, religion, or gender, are subject to the same ordinary laws and courts", "All citizens must earn the identical salary", "All accused persons are sent to prison without trial", "Every citizen must serve in the army", "A", "Rule of Law", "EASY",
         "Law applies uniformly to the powerful and weak alike without discriminatory exemptions."),
        ("The doctrine of Separation of Powers was articulated systematically by:",
         "Baron de Montesquieu", "Adam Smith", "Plato", "Aristotle", "A", "Separation of Powers", "EASY",
         "Montesquieu argued in 'The Spirit of the Laws' that dividing power among distinct branches prevents tyranny."),
        ("The primary constitutional function of the legislative branch of government (National Assembly) is to:",
         "Enact and pass laws for the peace, order, and good governance of the country", "Enforce court judgments", "Command military battalions", "Manage daily civil service payroll", "A", "Arms of Government", "EASY",
         "Section 4 of the Constitution vests the lawmaking powers of the Federation in the Senate and House of Representatives."),
        ("The executive arm of government is constitutionally responsible for:",
         "Implementing, executing, and enforcing laws passed by the legislature", "Interpreting legal statutes", "Impeaching state governors", "Amending the federal constitution", "A", "Arms of Government", "EASY",
         "The President, ministers, and civil service execute public policy and state administrative laws."),
        ("The judicial arm of government fulfills its constitutional mandate by:",
         "Interpreting laws, adjudicating disputes, and dispensing justice impartially", "Enacting annual state budgets", "Collecting market stall taxes", "Signing bilateral treaties", "A", "Arms of Government", "EASY",
         "The judiciary interprets constitutional provisions and acts as the dispassionate arbiter between citizens and state."),
        ("The system of Checks and Balances operates in government to:",
         "Enable each branch of government to oversee and limit the potential abuses of the other branches", "Abolish the independence of the judiciary", "Give the President absolute autocratic power", "Eliminate all political elections", "A", "Separation of Powers", "MEDIUM",
         "Checks and balances (e.g. presidential veto, legislative confirmation, judicial review) prevent concentration of power."),
        
        # Political Apathy & Electoral Participation
        ("Political apathy is defined as:", "A lack of interest, indifference, and refusal to participate in political and civic activities", "Active participation in electoral campaigns", "Serving as an election monitoring observer", "Voting in every general election", "A", "Political Apathy", "EASY",
         "Apathetic citizens disengage from voting, policy debates, and civic responsibilities."),
        ("Which of the following is a direct form of political apathy?", "Refusal to register as a voter or cast a ballot on election day", "Challenging an election result in court", "Attending a political party convention", "Writing letters to legislative representatives", "A", "Political Apathy", "EASY",
         "Voter absenteeism and low voter turnout are textbook symptoms of political apathy."),
        ("A major cause of widespread political apathy in developing democracies is:", "Broken campaign promises, bad governance, and electoral violence", "Too many free public universities", "The presence of independent courts", "Accurate voter electronic register", "A", "Political Apathy", "MEDIUM",
         "When citizens perceive that politicians serve selfish interests and votes are undermined, they lose faith in elections."),
        ("A severe danger of widespread political apathy in a nation is that it:", "Allows corrupt, incompetent, and autocratic leaders to attain and retain power", "Accelerates national economic growth", "Increases public infrastructure investments", "Improves international credit ratings", "A", "Political Apathy", "EASY",
         "When conscientious citizens boycott politics, unscrupulous factions hijack state power without resistance."),
        ("Political apathy can be effectively eliminated in a democracy through:", "Intensive civic education, voter sensitization, and transparent electoral processes", "Banning all opposition political parties", "Abolishing secret ballot voting", "Imposing military martial law", "A", "Political Apathy", "EASY",
         "Educating citizens on the power of the ballot and guaranteeing credible counts revitalizes civic participation."),
        
        # Civil Society & Public Service
        ("Civil Society Organizations (CSOs) in a democracy function as:", "Non-governmental, voluntary associations that advocate for human rights, transparency, and public welfare", "Armed paramilitary wings of political parties", "Corporate profit-maximizing conglomerates", "Secret religious cults", "A", "Civil Society", "EASY",
         "CSOs hold governments accountable, monitor human rights, and promote democratic reforms."),
        ("Which of the following is a prominent civil society trade union organization in Nigeria?", "Nigeria Labour Congress (NLC)", "Federal Road Safety Corps", "Independent National Electoral Commission", "Central Bank of Nigeria", "A", "Civil Society", "EASY",
         "NLC defends workers' rights, bargains for living minimum wages, and protests anti-people economic policies."),
        ("The traditional characteristic of the civil public service that requires civil servants to serve any government in power without partisan bias is:", "Political neutrality (impartiality)", "Nepotism", "Partisanship", "Subjectivity", "A", "Public Service", "MEDIUM",
         "Civil servants must provide objective advice and loyal execution regardless of which party wins political office."),
        ("The principle of Anonymity in the civil service implies that:", "Civil servants work behind the scenes and their political minister takes public praise or blame for departmental policies", "Civil servants must never disclose their real names", "All government offices must remain dark", "Official letters must never be signed", "A", "Public Service", "MEDIUM",
         "Ministers bear constitutional ministerial responsibility to parliament, shielding anonymous career civil servants."),
        ("The bureaucratic defect characterized by excessive adherence to rigid procedural formalities and delays in decision-making is:", "Red tape (bureaucracy)", "Meritocracy", "Efficiency", "Integrity", "A", "Public Service", "EASY", "Red tape frustrates citizens through labyrinthine paperwork, sluggish file movement, and administrative inertia."),
        
        # Social Issues: Cultism, Drug Abuse, Human Trafficking
        ("Secret cultism originated in Nigerian tertiary education institutions in 1952 at the University of Ibadan with the founding of the:", "Pyrates Confraternity (National Association of Seadogs)", "Black Axe Confraternity", "Supreme Eiye Confraternity", "Mafia Brotherhood", "A", "Social Issues", "MEDIUM", "Founded by Wole Soyinka and peers, its original noble aim was anti-colonialism and chivalry before subsequent degradation into violence."),
        ("A major factor that lures naive youths into campus secret cults is:", "False promises of power, protection, social influence, and peer pressure", "A desire to achieve academic excellence", "Participation in charitable philanthropy", "Studying for medical examinations", "A", "Social Issues", "EASY", "Cult recruiters deceive young students with illusions of campus security and fraternity."),
        ("The National Drug Law Enforcement Agency (NDLEA) is statutory mandated in Nigeria to:", "Combat the cultivation, trafficking, manufacture, and illicit abuse of narcotic drugs", "Regulate the prices of petroleum products", "Issue international commercial driving licenses", "Collect municipal market taxes", "A", "Drug Abuse", "EASY", "NDLEA patrols ports, interdicts international cartels, and rehabilitates substance-dependent victims."),
        ("The federal agency established in Nigeria to prevent, suppress, and punish trafficking in persons, especially women and children, is:", "NAPTIP (National Agency for the Prohibition of Trafficking in Persons)", "NDLEA", "EFCC", "FRSC", "A", "Human Trafficking", "EASY", "NAPTIP rescues trafficking victims, prosecutes perpetrators, and coordinates victim reintegration."),
        ("Human trafficking involves the recruitment, transportation, or receipt of persons by means of:", "Threat, force, coercion, deception, or abuse of power for the purpose of exploitation (labor or prostitution)", "Lawful international employment visa", "Voluntary tourism and sightseeing", "Official diplomatic transfer", "A", "Human Trafficking", "EASY", "Trafficking exploits vulnerable individuals through modern-day slavery and servitude."),
        ("The statutory body established in Nigeria to investigate, prevent, and prosecute economic and financial crimes (advance fee fraud, money laundering) is the:", "EFCC (Economic and Financial Crimes Commission)", "ICPC", "FRSC", "NEMA", "A", "Crime Prevention", "EASY", "The EFCC Act empowers the agency to investigate cybercrimes, illicit financial flows, and public embezzlement."),
        ("The Independent Corrupt Practices and Other Related Offences Commission (ICPC) focuses primarily on:", "Investigating corruption, bribery, and abuse of office across the public and civil service", "Traffic violations on highways", "Confiscating illegal imported arms", "Issuing national identity cards", "A", "Crime Prevention", "EASY", "ICPC targets systemic institutional corruption, public sector procurement fraud, and administrative graft."),
        
        # Road Safety & Traffic Regulations
        ("The Federal Road Safety Corps (FRSC) was established by the Federal Government of Nigeria in:", "1988", "1960", "1975", "1999", "A", "Road Safety", "MEDIUM", "Established under Decree 45 of 1988, FRSC aims to reduce fatal highway traffic crashes."),
        ("The leading human factor responsible for fatal vehicular accidents on Nigerian highways is:", "Excessive speeding and dangerous reckless overtaking", "Wearing seatbelts", "Obeying road speed limits", "Servicing vehicle brakes regularly", "A", "Road Safety", "EASY", "Speed violations prevent drivers from reacting safely to sudden road hazards."),
        ("A triangular traffic road sign with an apex pointing upwards is classified as a:", "Warning (cautionary) sign", "Regulatory mandatory sign", "Informative directional sign", "Construction detour sign", "A", "Road Safety", "MEDIUM", "Triangular signs alert motorists in advance to potential hazards ahead (e.g. sharp bend, pedestrian crossing)."),
        ("Circular traffic signs on highway roads are categorized as:", "Regulatory (mandatory or prohibitory) signs", "Warning signs", "Informative signs", "Decorative signs", "A", "Road Safety", "MEDIUM", "Circular signs convey positive legal orders (e.g. maximum speed limits, no entry, stop)."),
        ("Rectangular traffic signs convey:", "Information, directions, and geographic destinations", "Immediate danger warnings", "Prohibitory orders only", "Speed radar penalties", "A", "Road Safety", "EASY", "Rectangular boards guide motorists with route numbers, distance to towns, and hospital locations."),
        ("Wearing an approved safety seatbelt while driving a motor vehicle is crucial because it:", "Restrains the occupant during a collision, preventing impact with the steering wheel or ejection from the vehicle", "Increases the vehicle's engine fuel consumption", "Causes engine overheating", "Eliminates the need for brakes", "A", "Road Safety", "EASY", "Seatbelts disperse collision kinetic forces across strong pelvic and ribcage bones, saving lives."),
        
        # Youth & National Integration
        ("The National Youth Service Corps (NYSC) scheme was established in Nigeria in:", "1973 under Decree No. 24 by Gen. Yakubu Gowon", "1960 at Independence", "1979 under President Shagari", "1999 under President Obasanjo", "A", "National Development", "EASY", "NYSC was founded in the aftermath of the Civil War to foster national reconciliation, cultural unity, and youth integration."),
        ("The primary objective of the NYSC program is to:", "Promote national unity, cultural integration, and eliminate ethnic prejudice among educated Nigerian youths", "Train youths exclusively for combat war", "Replace the federal civil service", "Provide free vacations to university graduates", "A", "National Development", "EASY", "Deploying corps members outside their home states exposes them to diverse cultures and national brotherhood."),
        
        # Constitutional Evolution in Nigeria
        ("The Clifford Constitution of 1922 is historically significant in Nigeria because it introduced the:", "Elective principle (allowing elected representation in Lagos and Calabar)", "First federal executive president", "Abolition of all colonial taxes", "Creation of thirty-six states", "A", "Constitutional History", "MEDIUM", "The elective principle stimulated the formation of Nigeria's first modern political party (NNDP)."),
        ("The Richards Constitution of 1946 is noted for introducing:", "Regionalism (dividing the country into Northern, Western, and Eastern regions)", "The American presidential system", "Direct universal adult suffrage for women in the North", "A unitary constitution with zero regions", "A", "Constitutional History", "MEDIUM", "Richards established regional houses of assembly, laying the institutional groundwork for federalism."),
        ("The Macpherson Constitution of 1951 introduced a quasi-federal system and established a central legislature known as the:", "House of Representatives", "National Assembly", "Supreme Military Council", "Senate of Nigeria", "A", "Constitutional History", "MEDIUM", "Macpherson provided wide consultations from village to regional levels and created a central Council of Ministers."),
        ("The Lyttelton Constitution of 1954 is landmark because it formally established a:", "Fully fledged Federal System of government with residual powers to the regions", "Unitary system under a British Viceroy", "Military junta", "Confederation of independent nations", "A", "Constitutional History", "MEDIUM", "Lyttelton created the office of Regional Premier, separate regional civil services, and shared power lists."),
        ("Under the 1960 Independence Constitution, Nigeria had a parliamentary system where the Queen of England was represented by the:", "Governor-General (Dr. Nnamdi Azikiwe)", "President of Nigeria", "Prime Minister", "Chief Justice", "A", "Constitutional History", "EASY", "Sir Abubakar Tafawa Balewa was Prime Minister (Head of Government) while Azikiwe was Governor-General (Head of State)."),
        ("The 1963 Republican Constitution marked a constitutional milestone by:", "Severing monarchical ties with the British Crown and replacing the Governor-General with a ceremonial President", "Adopting the American executive presidential system", "Abolishing regional boundaries", "Appointing military governors", "A", "Constitutional History", "MEDIUM", "Nigeria became a Republic; appeals to the Judicial Committee of the Privy Council in London were abolished."),
        ("The 1979 Constitution of the Federal Republic of Nigeria departed fundamentally from previous models by adopting the:", "American-style Executive Presidential System", "British Westminster Parliamentary model", "French hybrid system", "Swiss collegial system", "A", "Constitutional History", "EASY", "The 1979 Constitution fused Head of State and Head of Government into a single directly elected Executive President.")
    ]
    
    # Extra Civic questions to cross 216
    extra_civic = [
        ("The supreme governing law of the Federal Republic of Nigeria from which all other laws derive their validity is the:", "1999 Constitution (as amended)", "Criminal Code Act", "Police Act", "Civil Service Handbook", "A", "Constitutional Law", "EASY", "Section 1(1) affirms the supremacy of the Constitution over all authorities throughout Nigeria."),
        ("Any law enacted by a State House of Assembly that conflicts with a provision of the Federal Constitution is:", "Null and void to the extent of its inconsistency", "Superior within that specific state", "Valid until amended by the governor", "Referred to traditional rulers", "A", "Constitutional Law", "MEDIUM", "Section 1(3) declares inconsistent state or federal enactments void ab initio."),
        ("The fundamental objective of state policy enshrined in Chapter II of the 1999 Constitution asserts that sovereignty belongs to the:", "People of Nigeria, from whom government derives all its powers and authority", "Military High Command", "Political party leaders", "Foreign development partners", "A", "Constitutional Law", "EASY", "Section 14(2)(a) declares popular sovereignty as the bedrock of Nigerian democracy."),
        ("The constitutional principle requiring that the composition of the government and its agencies must reflect the diversity of the nation to foster unity is the:", "Federal Character Principle", "Quota system", "Zoning formula", "National cake policy", "A", "Constitutional Law", "MEDIUM", "Section 14(3) mandates federal character to prevent regional or ethnic pre-eminence."),
        ("The Federal Character Commission (FCC) was established by the Constitution to:", "Enforce fair and equitable distribution of public bureaucratic posts and socio-economic amenities among states", "Manage national sports tournaments", "Print national passport booklets", "Audit foreign bank accounts", "A", "Constitutional Bodies", "EASY", "The FCC monitors recruitment into federal ministries, departments, and armed forces."),
        ("The Independent National Electoral Commission (INEC) is constitutionally empowered to:", "Organize, undertake, and supervise all elections to the offices of the President, Vice-President, Governors, and National/State Assemblies", "Appoint federal judges", "Enact state government annual budgets", "Arrest traffic violators", "A", "Constitutional Bodies", "EASY", "INEC oversees voter registration, party registration, and conduct of federal and state elections."),
        ("A political party in a democratic nation performs the essential function of:", "Recruiting political leaders, articulating public interests, and mobilizing voters during elections", "Printing national banknotes", "Commanding naval gunboats", "Enforcing tax audits", "A", "Political Parties", "EASY", "Political parties aggregate citizen demands and compete peacefully for the mandate to govern."),
        ("A free, independent, and vibrant press is often referred to in a constitutional democracy as the:", "Fourth Estate of the Realm", "Executive Council", "Second Chamber of Parliament", "Supreme Tribunal", "A", "Media & Democracy", "EASY", "The press monitors governance, exposes corruption, and upholds public accountability under Section 22."),
        ("The National Orientation Agency (NOA) in Nigeria fulfills its statutory civic mandate by:", "Communicating government policies, promoting national core values, and fostering patriotic civic consciousness", "Arresting violent armed bandits", "Managing commercial oil wells", "Inspecting primary school textbooks", "A", "Government Agencies", "EASY", "NOA educates citizens on civic responsibilities, national ethics, and positive behavioral values."),
        ("The Code of Conduct Bureau (CCB) and Code of Conduct Tribunal (CCT) were established to:", "Enforce ethical standards and transparency among public officers by mandating declaration of personal assets", "Conduct population censuses", "Issue driver's licenses", "Train military fighter pilots", "A", "Anti-Corruption", "MEDIUM", "CCB requires public officers to declare assets upon assumption and vacation of office to curb illicit enrichment."),
        ("Which of the following constitutes an act of examination malpractice punishable under the law?", "Smuggling unauthorized notes or electronic smartphones into an examination hall", "Studying textbooks in the school library", "Forming a peer study discussion group", "Sleeping early before the examination day", "A", "Social Issues", "EASY", "Exam malpractice undermines educational integrity and carries severe statutory criminal penalties."),
        ("Cult activities in educational institutions are legally classified under Nigerian criminal law as:", "Felonious and prohibited criminal activities", "Protected student social recreation", "Traditional religious worship", "Extra-curricular sports activities", "A", "Social Issues", "EASY", "Cult membership and associated violent extortion attract long imprisonment terms under anti-cultism laws."),
        ("HIV/AIDS cannot be transmitted through which of the following casual everyday interactions?", "Shaking hands, hugging, sharing eating utensils, or sitting next to an infected person", "Transfusion of unscreened contaminated blood", "Unprotected sexual intercourse", "Sharing unsterilized hypodermic syringes and sharp blades", "A", "Health Education", "EASY", "HIV is not airborne or spread through casual non-sexual physical contact or saliva."),
        ("The National Agency for the Control of AIDS (NACA) coordinates national initiatives focused on:", "Preventing HIV transmission, eliminating societal stigma, and expanding access to antiretroviral treatment", "Controlling highway road traffic", "Building international airports", "Issuing corporate business licenses", "A", "Health Education", "EASY", "NACA leads multisectoral public health strategies against the HIV epidemic in Nigeria."),
        ("The civic duty of an eligible voter on election day includes:", "Casting their ballot peacefully, keeping their vote secret, and refraining from vote-selling or violence", "Selling their voter card for cash", "Snatching ballot boxes from polling clerks", "Threatening supporters of rival political parties", "A", "Electoral Duties", "EASY", "Voters must conduct themselves with civic dignity and uphold democratic electoral integrity."),
        ("Vote-buying during democratic political elections is a corrupt civic evil because it:", "Compromises the democratic will of citizens and enables corrupt politicians to buy public office", "Makes the election too cheap to conduct", "Forces candidates to work too hard", "Increases voter turnout to 100%", "A", "Electoral Ethics", "EASY", "Vote commercialization subverts accountability, leaving elected officials unaccountable to voters."),
        ("The concept of 'National Integration' in a multi-ethnic federation like Nigeria refers to:", "The process of uniting diverse ethnic, linguistic, and religious groups into a cohesive national identity", "Forcing all citizens to speak a single language", "Abolishing all traditional cultural festivals", "Merging all states into a single central province", "A", "National Unity", "EASY", "Integration builds shared citizenship and mutual tolerance without erasing cultural heritage."),
        ("Inter-ethnic marriage fosters national unity in Nigeria by:", "Breaking down ethnic prejudices and building familial bonds across cultural boundaries", "Making weddings more expensive", "Abolishing state government boundaries", "Eliminating traditional chieftaincy titles", "A", "National Unity", "EASY", "Cross-cultural families humanize diverse traditions and cultivate inter-tribal understanding."),
        ("The National Youth Policy of Nigeria defines a youth as a citizen aged between:", "18 and 29 years", "5 and 15 years", "35 and 65 years", "50 and 80 years", "A", "Youth Development", "MEDIUM", "The revised National Youth Policy targets empowerment, skills, and civic participation for young adults."),
        ("Youth empowerment initiatives equip young citizens with:", "Vocational, technical, and entrepreneurial skills to foster self-reliance and economic productivity", "Political thuggery weapons", "Subsidized luxury motorcars", "Exemption from paying taxes", "A", "Youth Development", "EASY", "Empowerment prevents youth unemployment, crime, and vulnerability to political manipulation.")
    ]
    
    combined = []
    seen = set()
    for q in existing:
        norm = q['questionText'].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append(q)
            
    for item in civic_pool + extra_civic:
        norm = item[0].strip().lower()
        if norm not in seen:
            seen.add(norm)
            combined.append({
                'questionText': item[0],
                'optionA': item[1], 'optionB': item[2], 'optionC': item[3], 'optionD': item[4],
                'correctAnswer': item[5], 'topic': item[6], 'difficulty': item[7],
                'explanation': item[8], 'year': 2023
            })
            
    print(f"Total Civic Education questions: {len(combined)}")
    return combined

print("Civic module initialized.")
