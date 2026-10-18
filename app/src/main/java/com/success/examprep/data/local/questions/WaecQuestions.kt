package com.success.examprep.data.local.questions

import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType

/**
 * Dedicated repository data source for WAEC WASSCE questions.
 * Distinct original questions specifically curated for WAEC standard format.
 */
object WaecQuestions {

    val allQuestions: List<Question> by lazy {
        mathematicsQuestions + physicsQuestions + chemistryQuestions + biologyQuestions + englishQuestions +
        com.success.examprep.data.local.questions.subjects.WaecMathematicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecPhysicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecChemistryQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecBiologyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecEnglishQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecGovernmentQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecCrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecIrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecLiteratureQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecCommerceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecEconomicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecGeographyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecAgriculturalScienceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.WaecCivicEducationQuestions.questions
    }

    val mathematicsQuestions = listOf(
        Question(
            id = 2001,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Plane Geometry",
            questionText = "In a circle with radius 7 cm, calculate the length of an arc that subtends an angle of 60° at the centre. [Take π = 22/7]",
            optionA = "5.33 cm",
            optionB = "7.33 cm",
            optionC = "11.00 cm",
            optionD = "14.67 cm",
            correctAnswer = OptionLetter.B,
            explanation = "Arc length L = (θ/360) × 2πr = (60/360) × 2 × (22/7) × 7 = (1/6) × 44 = 7.33 cm.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 2002,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Quadratic Equations",
            questionText = "Find the value of k for which the quadratic equation x² - 6x + k = 0 has equal roots.",
            optionA = "3",
            optionB = "6",
            optionC = "9",
            optionD = "12",
            correctAnswer = OptionLetter.C,
            explanation = "For equal roots, the discriminant b² - 4ac = 0.\n(-6)² - 4(1)(k) = 0 => 36 - 4k = 0 => 4k = 36 => k = 9.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 2003,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Trigonometry",
            questionText = "A ladder 10 m long leans against a vertical wall. If the angle between the ladder and the ground is 30°, how high up the wall does the ladder reach?",
            optionA = "5.0 m",
            optionB = "7.5 m",
            optionC = "8.66 m",
            optionD = "10.0 m",
            correctAnswer = OptionLetter.A,
            explanation = "sin(30°) = Opposite / Hypotenuse = Height / 10.\nHeight = 10 × sin(30°) = 10 × 0.5 = 5.0 m.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2004,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Sequence and Series",
            questionText = "Find the 8th term of the arithmetic progression (A.P.): 3, 7, 11, 15, ...",
            optionA = "27",
            optionB = "31",
            optionC = "35",
            optionD = "39",
            correctAnswer = OptionLetter.B,
            explanation = "First term a = 3, common difference d = 7 - 3 = 4.\nT₈ = a + (8 - 1)d = 3 + 7(4) = 3 + 28 = 31.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2005,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Statistics",
            questionText = "The mean of the numbers 4, 7, x, 11, and 13 is 9. Find the value of x.",
            optionA = "8",
            optionB = "9",
            optionC = "10",
            optionD = "12",
            correctAnswer = OptionLetter.C,
            explanation = "Mean = (4 + 7 + x + 11 + 13)/5 = 9.\n(35 + x)/5 = 9 => 35 + x = 45 => x = 10.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2006,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Circle Theorems",
            questionText = "In a circle, an angle inscribed in a semi-circle is always equal to:",
            optionA = "45°",
            optionB = "60°",
            optionC = "90°",
            optionD = "180°",
            correctAnswer = OptionLetter.C,
            explanation = "By Thales' circle theorem, any angle subtended by a diameter at the circumference is a right angle (90°).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2007,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Probability",
            questionText = "A bag contains 5 red balls and 3 green balls. If one ball is drawn at random, what is the probability that it is green?",
            optionA = "3/8",
            optionB = "5/8",
            optionC = "1/2",
            optionD = "3/5",
            correctAnswer = OptionLetter.A,
            explanation = "Total balls = 5 + 3 = 8. Number of green balls = 3.\nProbability = 3/8.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2008,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Algebra",
            questionText = "Factorize completely the expression: 4x² - 9y².",
            optionA = "(2x - 3y)²",
            optionB = "(2x + 3y)(2x - 3y)",
            optionC = "(4x + 9y)(x - y)",
            optionD = "2(x - 3y)(x + 3y)",
            correctAnswer = OptionLetter.B,
            explanation = "Using the difference of two squares formula: a² - b² = (a + b)(a - b).\n(2x)² - (3y)² = (2x + 3y)(2x - 3y).",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 2009,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Bearings",
            questionText = "What is the three-figure bearing of South-West (SW)?",
            optionA = "045°",
            optionB = "135°",
            optionC = "225°",
            optionD = "315°",
            correctAnswer = OptionLetter.C,
            explanation = "Measured clockwise from North (000°): East = 090°, South = 180°, South-West = 180° + 45° = 225°.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 2010,
            examType = ExamType.WAEC,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Logarithms",
            questionText = "Solve for x if log₃(2x - 1) = 2.",
            optionA = "4",
            optionB = "5",
            optionC = "6",
            optionD = "7",
            correctAnswer = OptionLetter.B,
            explanation = "Converting logarithmic form to exponential form: 2x - 1 = 3² = 9.\n2x = 10 => x = 5.",
            difficulty = Difficulty.HARD,
            isFree = true
        )
    )

    val physicsQuestions = listOf(
        Question(
            id = 2101,
            examType = ExamType.WAEC,
            subject = SubjectType.PHYSICS,
            year = 2025,
            topic = "Waves",
            questionText = "A wave has a frequency of 50 Hz and a wavelength of 6 m. Calculate its velocity.",
            optionA = "8.3 m/s",
            optionB = "44 m/s",
            optionC = "300 m/s",
            optionD = "600 m/s",
            correctAnswer = OptionLetter.C,
            explanation = "Wave velocity v = f × λ = 50 Hz × 6 m = 300 m/s.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2102,
            examType = ExamType.WAEC,
            subject = SubjectType.PHYSICS,
            year = 2024,
            topic = "Heat",
            questionText = "The heat required to raise the temperature of 1 kg of a substance by 1 Kelvin is called its:",
            optionA = "Heat capacity",
            optionB = "Specific heat capacity",
            optionC = "Latent heat of fusion",
            optionD = "Specific latent heat",
            correctAnswer = OptionLetter.B,
            explanation = "Specific heat capacity is the quantity of heat per unit mass per degree temperature change.",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )

    val chemistryQuestions = listOf(
        Question(
            id = 2201,
            examType = ExamType.WAEC,
            subject = SubjectType.CHEMISTRY,
            year = 2025,
            topic = "Periodic Table",
            questionText = "Elements in Group 17 (Group 7) of the periodic table are collectively called:",
            optionA = "Alkali metals",
            optionB = "Noble gases",
            optionC = "Halogens",
            optionD = "Lanthanides",
            correctAnswer = OptionLetter.C,
            explanation = "Group 17 elements (Fluorine, Chlorine, Bromine, Iodine) are known as halogens (salt formers).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 2202,
            examType = ExamType.WAEC,
            subject = SubjectType.CHEMISTRY,
            year = 2024,
            topic = "Organic Chemistry",
            questionText = "What is the IUPAC name for CH₃-CH₂-OH?",
            optionA = "Methanol",
            optionB = "Ethanol",
            optionC = "Propanol",
            optionD = "Ethanoic acid",
            correctAnswer = OptionLetter.B,
            explanation = "A two-carbon alkane derivative with a hydroxyl (-OH) group is ethanol.",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )

    val biologyQuestions = listOf(
        Question(
            id = 2301,
            examType = ExamType.WAEC,
            subject = SubjectType.BIOLOGY,
            year = 2025,
            topic = "Nutrition",
            questionText = "Which test reagent produces a brick-red precipitate when heated with reducing sugars?",
            optionA = "Biuret reagent",
            optionB = "Iodine solution",
            optionC = "Benedict's solution",
            optionD = "Sudan III",
            correctAnswer = OptionLetter.C,
            explanation = "Benedict's solution is reduced from blue copper(II) to brick-red copper(I) oxide by reducing sugars.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 2302,
            examType = ExamType.WAEC,
            subject = SubjectType.BIOLOGY,
            year = 2024,
            topic = "Ecology",
            questionText = "In a terrestrial food chain: Grass → Grasshopper → Toad → Snake, which organism is the primary consumer?",
            optionA = "Grass",
            optionB = "Grasshopper",
            optionC = "Toad",
            optionD = "Snake",
            correctAnswer = OptionLetter.B,
            explanation = "The grasshopper directly feeds on the primary producer (grass), making it the primary consumer (herbivore).",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )

    val englishQuestions = listOf(
        Question(
            id = 2401,
            examType = ExamType.WAEC,
            subject = SubjectType.ENGLISH,
            year = 2025,
            topic = "Antonyms",
            questionText = "Select the option most nearly opposite in meaning to the word underlined: The candidate showed a TEMERITY that surprised the panel.",
            optionA = "Audacity",
            optionB = "Timidity",
            optionC = "Confidence",
            optionD = "Rudeness",
            correctAnswer = OptionLetter.B,
            explanation = "Temerity means excessive boldness or rashness. Its exact opposite is timidity or shyness.",
            difficulty = Difficulty.HARD,
            isFree = true
        ),
        Question(
            id = 2402,
            examType = ExamType.WAEC,
            subject = SubjectType.ENGLISH,
            year = 2024,
            topic = "Question Tags",
            questionText = "Choose the correct tag: You have not done the assignment, _____?",
            optionA = "haven't you",
            optionB = "have you",
            optionC = "did you",
            optionD = "is it",
            correctAnswer = OptionLetter.B,
            explanation = "A negative main statement ('have not') takes a positive question tag ('have you?').",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )
}
