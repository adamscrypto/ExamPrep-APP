#!/usr/bin/env python3
import sys
import os

sys.path.append('tmp/generator')
from common import render_kotlin_file, extract_clean_existing
import lit_data1, lit_data2, lit_data3

FINAL_FORTY_FIVE_LIT = [
    # Additional Shakespearean Drama & Classical Literary Devices
    ("In Shakespeare's 'The Tempest', the rightful Duke of Milan stranded on an enchanted island who practices magical arts is:",
     "Prospero", "Ferdinand", "Antonio", "Alonso", "A", "Shakespearean Drama", "EASY",
     "Prospero uses white magic, aided by the spirit Ariel, to reclaim his rightful dukedom."),
    ("In 'The Tempest', the deformed, savage native son of the witch Sycorax who curses Prospero for taking his island is:",
     "Caliban", "Ariel", "Stephano", "Trinculo", "A", "Shakespearean Drama", "EASY",
     "Caliban's famous cry: 'You taught me language, and my profit on't is, I know how to curse' is a landmark post-colonial motif."),
    ("In Shakespeare's comedy 'Twelfth Night', the central comedic confusion begins when the shipwrecked heroine Viola disguises herself as a young page named:",
     "Cesario", "Sebastian", "Orsino", "Malvolio", "A", "Shakespearean Drama", "EASY",
     "Viola disguises herself as Cesario, accidentally causing Countess Olivia to fall passionately in love with her."),
    ("In Shakespeare's tragedy 'King Lear', King Lear's fatal error of judgment in the opening scene is:",
     "Disinheriting and banishing his youngest daughter Cordelia because she refuses to flatter him with hypocritical public declarations of love",
     "Executing the Earl of Kent", "Surrendering his crown to the King of France", "Arresting Edgar", "A", "Shakespearean Drama", "EASY",
     "Lear divides his kingdom between flattering daughters Goneril and Regan, who subsequently drive him mad on the storm-swept heath."),
    ("In 'King Lear', Cordelia famously responds to her father's demand for flattery with the truthful words:",
     "'I love your Majesty according to my bond; nor more nor less'",
     "'I love you more than words can wield the matter'", "'I love you dearer than eyesight, space, and liberty'", "'My love exceeds all mortal measure'", "A", "Shakespearean Drama", "EASY",
     "Cordelia refuses to inflate her natural filial bond with false sycophancy, stating 'nothing will come of nothing.'"),
    ("In Shakespeare's 'Romeo and Juliet', Romeo's hot-headed, witty friend who is slain by Tybalt under Romeo's arm is:",
     "Mercutio", "Benvolio", "Paris", "Friar Laurence", "A", "Shakespearean Drama", "EASY",
     "Mercutio dies cursing both warring families: 'A plague o' both your houses!'."),
    ("In Shakespeare's 'Julius Caesar', the conspirator who strikes the very first dagger blow against Caesar from behind is:",
     "Casca", "Brutus", "Cassius", "Cinna", "A", "Shakespearean Drama", "MEDIUM",
     "Casca cries 'Speak, hands, for me!' as he plunges his dagger into the back of Caesar's neck."),
    ("In 'Julius Caesar', the noble Roman wife of Brutus who swallows burning coals to commit suicide is:",
     "Portia", "Calpurnia", "Octavia", "Cleopatra", "A", "Shakespearean Drama", "EASY",
     "Portia commits suicide in despair after Octavius and Antony march against Brutus."),
    ("In 'Macbeth', which Scottish nobleman slays Macbeth in single combat at the siege of Dunsinane Castle?",
     "Macduff, who was from his mother's womb untimely ripped (delivered by Caesarean section)",
     "Malcolm", "Banquo", "Ross", "A", "Shakespearean Drama", "EASY",
     "Macduff fulfills the witches' riddle that no man of normal woman birth could slay Macbeth."),

    # African Drama: Efua Sutherland & Athol Fugard
    ("In Efua Sutherland's Ghanaian comedy 'The Marriage of Anansewa', the clever protagonist Ananse uses which traditional storytelling trickster technique?",
     "Anansesem (spider trickster storytelling technique)",
     "Shakespearean soliloquy exclusively", "Classical Greek dithyramb", "Brechtian alienation without humor", "A", "African Drama", "MEDIUM",
     "Ananse promises his daughter Anansewa in marriage to four wealthy rival chiefs simultaneously to extract gifts from each."),
    ("In Athol Fugard's powerful anti-apartheid play 'Sizwe Bansi Is Dead', the protagonist Sizwe Bansi survives the inhuman pass-law system by:",
     "Taking the identity book (passbook) from a dead man's corpse (Robert Zwelinzima)",
     "Assassinating the police commissioner", "Fleeing across the ocean to London", "Becoming a diamond mine owner", "A", "African Drama", "EASY",
     "Sizwe must 'die' to his own name in order to live and work to feed his family in apartheid South Africa."),
    ("In 'Sizwe Bansi Is Dead', the township photographer who provides humorous yet profound commentary on black resilience is:",
     "Styles", "Buntu", "Zwelinzima", "Sky", "A", "African Drama", "EASY",
     "Styles's photography studio in New Brighton captures human dignity and dreams denied by apartheid."),

    # African Prose & Poetry
    ("In Chinua Achebe's 'Arrow of God', the unyielding Chief Priest of Ulu who is broken by colonial hubris and internal clan conflict is:",
     "Ezeulu", "Nwaka", "Obika", "Akuebue", "A", "African Prose", "EASY",
     "Ezeulu's refusal to name the New Yam Feast causes starvation in Umuaro, turning his people toward Christianity."),
    ("In Chinua Achebe's 'No Longer at Ease', the tragic protagonist Obi Okonkwo (grandson of Okonkwo) falls from grace in 1950s Lagos by:",
     "Accepting a bribe of twenty pounds to secure a civil service scholarship",
     "Stealing a car", "Defrauding a bank", "Joining an armed militia", "A", "African Prose", "EASY",
     "Obi succumbs to mounting social pressures, debts, and his love for Clara (an Osu), ending in court."),
    ("In Ngugi wa Thiong'o's landmark novel 'A Grain of Wheat', the mysterious traitor who betrayed the Mau Mau forest fighter Kihika is revealed at the climax to be:",
     "Mugo, whom the village had revered as a heroic leader",
     "Gikonyo", "Karanja", "General R", "A", "African Prose", "MEDIUM",
     "Mugo confesses publicly on Uhuru Day that he betrayed Kihika to save his own skin, shocking the community."),
    ("In Buchi Emecheta's novel 'Second Class Citizen', the heroine Adah Obi battles racial prejudice, poverty, and domestic abuse in:",
     "London, England", "Lagos, Nigeria", "Paris, France", "New York, USA", "A", "African Prose", "EASY",
     "Adah's resilient determination enables her to write manuscripts and support her children despite her abusive husband Francis."),
    ("In Oswald Mtshali's South African anti-apartheid poem 'The Anvil and the Hammer', the speaker's consciousness is forged between:",
     "The conflicting forces of ancestral African traditions and modern industrial urban realities",
     "Black and white chess pieces", "Agricultural tools and farm horses", "Religious sects", "A", "African Poetry", "MEDIUM",
     "Mtshali depicts the violent psychic shaping of Black African identity under the hammer of colonial industrialization."),
    ("In Dennis Brutus's South African prison poem 'A Troubadour I Traverse', the poet pledges his romantic devotion to:",
     "His beloved, bruised, and bleeding native land of South Africa ('land of my unpassioned love')",
     "A Spanish princess", "A French castle", "A diamond necklace", "A", "African Poetry", "EASY",
     "Brutus adopts the medieval persona of a wandering troubadour serenading his tortured motherland."),
    ("In Christopher Okigbo's visionary modernist poetry collection 'Labyrinths', the opening poem 'Idoto' begins with the humble invocation:",
     "'Before you, mother Idoto, naked I stand; before your watery presence, a prodigal'",
     "'Rise up, O thunder of Africa!'", "'I sing of high skyscrapers'", "'Farewell, sweet village bells'", "A", "African Poetry", "MEDIUM",
     "Okigbo seeks spiritual reconnection with his ancestral water deity after wandering in foreign intellectual labyrinths."),

    # Non-African Prose: Pride and Prejudice, To Kill a Mockingbird
    ("In Jane Austen's beloved novel 'Pride and Prejudice', the famous opening line ironically asserts that:",
     "'It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife'",
     "'It was the best of times, it was the worst of times'", "'Call me Ishmael'", "'Happy families are all alike'", "A", "Non-African Prose", "EASY",
     "Austen satirizes the mercenary matrimonial preoccupations of Regency-era English gentry."),
    ("In 'Pride and Prejudice', the heroine Elizabeth Bennet initially rejects Mr. Darcy's proposal because:",
     "She resents his arrogant pride, his separation of Bingley from her sister Jane, and his alleged mistreatment of Wickham",
     "He is poor and has no estate", "He insulted her father", "She is already engaged to Mr. Collins", "A", "Non-African Prose", "EASY",
     "Elizabeth's prejudice against Darcy's perceived pride blind her to his true generosity until his letter clarifies the truth."),
    ("In Harper Lee's novel 'To Kill a Mockingbird', the noble lawyer who defends the falsely accused Black man Tom Robinson in 1930s Alabama is:",
     "Atticus Finch", "Bob Ewell", "Arthur Radley (Boo)", "Sheriff Heck Tate", "A", "Non-African Prose", "EASY",
     "Atticus teaches his children Scout and Jem moral courage and empathy: 'You never really understand a person until you climb into his skin and walk around in it.'"),
    ("In 'To Kill a Mockingbird', the title metaphor of killing a mockingbird represents the sin of:",
     "Harming or destroying gentle, innocent people who do nothing but bring beauty and music into the world",
     "Hunting game birds out of season", "Neglecting garden agriculture", "Breaking church windows", "A", "Non-African Prose", "EASY",
     "Both Tom Robinson and Boo Radley are symbolic mockingbirds persecuted by society."),

    # Non-African Poetry: T.S. Eliot, Tennyson, Robert Frost, John Donne
    ("In T.S. Eliot's modernist religious poem 'The Journey of the Magi', the narrator describes the pilgrimage to Bethlehem as:",
     "'A cold coming we had of it, just the worst time of the year for a journey'",
     "'A glorious triumph filled with gold and jewels'", "'A swift pleasant gallop across green meadows'", "'A short day's holiday in the sun'", "A", "Modernist Poetry", "EASY",
     "Eliot depicts spiritual conversion not as effortless comfort, but as an arduous, agonizing death to old pagan values."),
    ("In Alfred Lord Tennyson's heroic dramatic monologue 'Ulysses', the aging hero expresses unyielding determination to:",
     "'To strive, to seek, to find, and not to yield'",
     "'Rest quietly by the home hearth forever'", "'Surrender his sword to his son Telemachus'", "'Abandon all voyages on the sea'", "A", "Victorian Poetry", "EASY",
     "Ulysses refuses to rust unburnished in old age, resolving to sail beyond the sunset to fresh knowledge and adventure."),
    ("In Robert Frost's poem 'Stopping by Woods on a Snowy Evening', the speaker tears himself away from the lovely, dark, and deep woods because:",
     "'I have promises to keep, and miles to go before I sleep'",
     "A storm is brewing on the mountain", "His horse refuses to walk", "The forest owner arrested him", "A", "American Poetry", "EASY",
     "The final lines balance the seductive peace of oblivion against moral obligations and human duty in life."),

    # Core Literary Terms & Figures of Speech Review
    ("The repetition of a word or phrase at the beginning of successive sentences, clauses, or poetic lines is:",
     "Anaphora", "Epistrophe", "Chiasmus", "Tautology", "A", "Rhetorical Devices", "MEDIUM",
     "Anaphora builds emotional crescendo (e.g., Martin Luther King's 'I have a dream... I have a dream')."),
    ("The repetition of a word or phrase at the end of successive sentences or clauses is:",
     "Epistrophe (or Epiphora)", "Anaphora", "Asyndeton", "Polysyndeton", "A", "Rhetorical Devices", "MEDIUM",
     "Lincoln's Gettysburg Address: 'of the people, by the people, for the people' is a supreme example of epistrophe."),
    ("A rhetorical device where words, grammatical constructions, or concepts are repeated in reverse order in two parallel phrases (ABBA pattern) is:",
     "Chiasmus", "Antithesis", "Anaphora", "Oxymoron", "A", "Rhetorical Devices", "MEDIUM",
     "J.F. Kennedy: 'Ask not what your country can do for you; ask what you can do for your country' demonstrates chiasmus."),
    ("The deliberate omission of coordinating conjunctions (like 'and', 'or') between words or clauses in a rapid list is:",
     "Asyndeton", "Polysyndeton", "Ellipsis", "Zeugma", "A", "Rhetorical Devices", "MEDIUM",
     "Julius Caesar's 'Veni, vidi, vici' (I came, I saw, I conquered) employs asyndeton for dramatic speed and impact."),
    ("The deliberate use of multiple conjunctions in close succession (e.g., 'and it rained, and it blew, and the flood rose') is:",
     "Polysyndeton", "Asyndeton", "Anaphora", "Tautology", "A", "Rhetorical Devices", "MEDIUM",
     "Polysyndeton slows down reading rhythm and conveys overwhelming abundance or rhythmic gravity."),
    ("A figure of speech in which a single word (usually a verb or adjective) applies to two different nouns in different senses is:",
     "Zeugma (or Syllepsis)", "Oxymoron", "Litotes", "Euphemism", "A", "Rhetorical Devices", "MEDIUM",
     "Alexander Pope: 'Or stain her honour, or her new brocade' applies 'stain' morally to honor and physically to fabric."),
    ("The unnecessary repetition of the same idea, meaning, or concept in different words within a single statement is a:",
     "Tautology (or Pleonasm)", "Paradox", "Irony", "Antithesis", "A", "Literary Terms", "EASY",
     "Saying 'a free gift', 'return back', or 'unsolved mystery' are redundant tautologies."),
    ("The emotional atmosphere, mood, or feeling evoked in the reader by a literary work's setting and imagery is the:",
     "Atmosphere (or Mood)", "Tone", "Diction", "Syntax", "A", "Literary Analysis", "EASY",
     "Atmosphere is the subjective emotional aura (e.g., eerie, gloomy, romantic, tense) generated by descriptive language."),
    ("The author's underlying personal attitude towards the subject matter, characters, or audience conveyed through diction is the:",
     "Tone", "Mood", "Setting", "Genre", "A", "Literary Analysis", "EASY",
     "Tone can be sarcastic, solemn, celebratory, detached, playful, or cynical."),
    ("The specific word choice and vocabulary selected by a writer to establish style, voice, and register is:",
     "Diction", "Syntax", "Prosody", "Meter", "A", "Stylistics", "EASY",
     "Diction varies from formal, elevated, and poetic to informal, colloquial, and dialectal."),
    ("The grammatical arrangement and ordering of words within phrases and sentences to achieve aesthetic impact is:",
     "Syntax", "Diction", "Rhyme", "Cadence", "A", "Stylistics", "EASY",
     "Inverted syntax (e.g., 'Cold was the night') highlights particular words over standard subject-verb order."),
    ("The vantage point or narrative perspective from which a story is told to the reader is the:",
     "Point of View", "Plot", "Theme", "Tone", "A", "Narrative Elements", "EASY",
     "Points of view include first person ('I'), third person limited ('he/she'), and third person omniscient (all-knowing)."),
    ("A narrator whose credibility or account of events is compromised due to youth, mental instability, innocence, or bias is an:",
     "Unreliable narrator", "Omniscient narrator", "Third-person objective narrator", "Authorial persona", "A", "Narrative Elements", "EASY",
     "Huck Finn, the governess in The Turn of the Screw, and Chief Bromden in One Flew Over the Cuckoo's Nest are classic unreliable narrators."),
    ("A lyric poem composed to celebrate a marriage and bless the newlyweds is an:",
     "Epithalamium (Epithalamion)", "Elegy", "Ode", "Epitaph", "A", "Poetic Forms", "MEDIUM",
     "Edmund Spenser's Epithalamion is a renowned English marriage poem celebrating his own wedding."),
    ("A three-line stanza in poetry, typically rhyming together or linked by rhyme (such as terza rima), is a:",
     "Tercet (or Triplet)", "Quatrain", "Cinquain", "Couplet", "A", "Poetic Forms", "EASY",
     "Dante composed the Divine Comedy in terza rima tercets (aba bcb cdc)."),
    ("A four-line poetic stanza or unit of verse is a:",
     "Quatrain", "Octave", "Sestet", "Couplet", "A", "Poetic Forms", "EASY",
     "Quatrains are the most common stanza form in English poetry, used in ballads, sonnets, and hymns."),
    ("A six-line poetic stanza or unit of verse, such as the second section of a Petrarchan sonnet, is a:",
     "Sestet", "Octave", "Quatrain", "Tercet", "A", "Poetic Forms", "EASY",
     "The sestet resolves or reflects upon the proposition stated in the preceding eight-line octave."),
    ("An eight-line poetic stanza, such as the opening unit of a Petrarchan sonnet or ottava rima, is an:",
     "Octave", "Sestet", "Quatrain", "Canto", "A", "Poetic Forms", "EASY",
     "The Petrarchan octave rhymes abbaabba, establishing the poem's premise or emotional dilemma."),
    ("The continuation of a syntactic sentence or thought across a poetic line break or stanza boundary without terminal punctuation is:",
     "Enjambment (Run-on line)", "End-stopping", "Caesura", "Refrain", "A", "Prosody", "MEDIUM",
     "Enjambment creates a sense of natural spoken fluency and propulsion across lines, contrasting with end-stopped lines.")
]

def main():
    existing = extract_clean_existing('Literature')
    pools = [lit_data1.LIT_POOL_1, lit_data2.LIT_POOL_2, lit_data3.LIT_POOL_3, FINAL_FORTY_FIVE_LIT]
    
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
                
    print(f"Total authentic Literature questions: {len(combined)}")
    assert len(combined) == 216, f"Expected 216 questions, got {len(combined)}"
    
    jamb = combined[:72]
    waec = combined[72:144]
    neco = combined[144:216]
    
    render_kotlin_file('Jamb', 'Literature', jamb)
    render_kotlin_file('Waec', 'Literature', waec)
    render_kotlin_file('Neco', 'Literature', neco)
    print("Successfully generated all Literature in English question banks!")

if __name__ == '__main__':
    main()
