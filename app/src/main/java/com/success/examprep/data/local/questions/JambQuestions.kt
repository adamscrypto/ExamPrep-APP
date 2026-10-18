package com.success.examprep.data.local.questions

import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType

/**
 * Dedicated repository data source for JAMB UTME questions.
 */
object JambQuestions {

    val allQuestions: List<Question> by lazy {
        mathematicsQuestions + physicsQuestions + chemistryQuestions + biologyQuestions + englishQuestions +
        com.success.examprep.data.local.questions.subjects.JambMathematicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambPhysicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambChemistryQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambBiologyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambEnglishQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambGovernmentQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambCrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambIrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambLiteratureQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambCommerceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambEconomicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambGeographyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambAgriculturalScienceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.JambCivicEducationQuestions.questions
    }

    val mathematicsQuestions = listOf(
        Question(
            id = 1001,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Algebra",
            questionText = "If 3x - 7 = 2x + 8, find the value of 2x - 5.",
            optionA = "20",
            optionB = "25",
            optionC = "30",
            optionD = "35",
            correctAnswer = OptionLetter.B,
            explanation = "Collect like terms from 3x - 7 = 2x + 8:\n3x - 2x = 8 + 7 => x = 15.\nSubstitute x = 15 into 2x - 5: 2(15) - 5 = 30 - 5 = 25.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1002,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Number Bases",
            questionText = "Convert the binary number 11011₂ to base 10.",
            optionA = "25",
            optionB = "27",
            optionC = "29",
            optionD = "31",
            correctAnswer = OptionLetter.B,
            explanation = "11011₂ = (1 × 2⁴) + (1 × 2³) + (0 × 2²) + (1 × 2¹) + (1 × 2⁰) = 16 + 8 + 0 + 2 + 1 = 27₁₀.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1003,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Indices",
            questionText = "Evaluate the expression: (81)^(3/4) × (27)^(-1/3).",
            optionA = "3",
            optionB = "6",
            optionC = "9",
            optionD = "12",
            correctAnswer = OptionLetter.C,
            explanation = "(81)^(3/4) = (3⁴)^(3/4) = 3³ = 27.\n(27)^(-1/3) = (3³)^(-1/3) = 3⁻¹ = 1/3.\n27 × (1/3) = 9.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1004,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Sets",
            questionText = "In a class of 40 students, 24 study Biology, 18 study Chemistry, and 6 study neither. How many students study both subjects?",
            optionA = "6",
            optionB = "8",
            optionC = "10",
            optionD = "12",
            correctAnswer = OptionLetter.B,
            explanation = "Students taking at least one subject = 40 - 6 = 34.\nn(B ∪ C) = n(B) + n(C) - n(B ∩ C) => 34 = 24 + 18 - x => x = 42 - 34 = 8.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1005,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Logarithms",
            questionText = "If log₁₀ 2 = 0.3010 and log₁₀ 3 = 0.4771, find the value of log₁₀ 18.",
            optionA = "1.0791",
            optionB = "1.2552",
            optionC = "1.5562",
            optionD = "1.8060",
            correctAnswer = OptionLetter.B,
            explanation = "18 = 2 × 3².\nlog₁₀ 18 = log₁₀ 2 + 2 × log₁₀ 3 = 0.3010 + 2(0.4771) = 0.3010 + 0.9542 = 1.2552.",
            difficulty = Difficulty.HARD,
            isFree = true
        ),
        Question(
            id = 1006,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Geometry",
            questionText = "The interior angles of a triangle are in the ratio 2 : 3 : 5. What is the magnitude of the largest angle?",
            optionA = "72°",
            optionB = "90°",
            optionC = "100°",
            optionD = "108°",
            correctAnswer = OptionLetter.B,
            explanation = "Sum of angles in a triangle = 180°.\nTotal parts = 2 + 3 + 5 = 10.\nLargest angle = (5 / 10) × 180° = 90°.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1007,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Statistics",
            questionText = "Find the median of the following set of test scores: 14, 9, 21, 7, 18, 12, 16.",
            optionA = "12",
            optionB = "14",
            optionC = "15",
            optionD = "16",
            correctAnswer = OptionLetter.B,
            explanation = "Ordered scores: 7, 9, 12, 14, 16, 18, 21.\nSince n = 7 (odd), the median is the 4th score: 14.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1008,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Probability",
            questionText = "A fair six-sided die is rolled once. What is the probability of obtaining a prime number?",
            optionA = "1/6",
            optionB = "1/3",
            optionC = "1/2",
            optionD = "2/3",
            correctAnswer = OptionLetter.C,
            explanation = "Sample space = {1, 2, 3, 4, 5, 6}. Primes = {2, 3, 5} (3 primes).\nProbability = 3/6 = 1/2.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1009,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Quadratic Equations",
            questionText = "Determine the roots of the quadratic equation: x² - 7x + 12 = 0.",
            optionA = "-3 and -4",
            optionB = "3 and 4",
            optionC = "-3 and 4",
            optionD = "2 and 6",
            correctAnswer = OptionLetter.B,
            explanation = "Factoring gives (x - 3)(x - 4) = 0 => x = 3 or x = 4.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1010,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Simultaneous Equations",
            questionText = "Solve for x and y: 2x + y = 7 and x - y = 2.",
            optionA = "x = 2, y = 3",
            optionB = "x = 3, y = 1",
            optionC = "x = 4, y = -1",
            optionD = "x = 5, y = -3",
            correctAnswer = OptionLetter.B,
            explanation = "Adding both equations: (2x + x) + (y - y) = 7 + 2 => 3x = 9 => x = 3.\nSubstitute into x - y = 2 => 3 - y = 2 => y = 1.",
            difficulty = Difficulty.HARD,
            isFree = true
        ),
        Question(
            id = 1011,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Arithmetic",
            questionText = "Calculate the simple interest on a principal of ₦15,000 invested for 3 years at 8% per annum.",
            optionA = "₦2,800",
            optionB = "₦3,200",
            optionC = "₦3,600",
            optionD = "₦4,000",
            correctAnswer = OptionLetter.C,
            explanation = "I = (P × R × T) / 100 = (15,000 × 8 × 3) / 100 = ₦3,600.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1012,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Number Bases",
            questionText = "If 23₄ + 12₄ = x₄, find x.",
            optionA = "35",
            optionB = "41",
            optionC = "101",
            optionD = "111",
            correctAnswer = OptionLetter.C,
            explanation = "In base 4: 3 + 2 = 5 = 1 group of 4 + 1 remainder. Carry 1. Then 2 + 1 + 1 = 4 = 1 group of 4 + 0 remainder. Result = 101₄.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1013,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Algebra",
            questionText = "Make 'r' the subject of the formula: V = πr²h.",
            optionA = "r = √(V / πh)",
            optionB = "r = V / (πh)",
            optionC = "r = √(Vπh)",
            optionD = "r = (V / πh)²",
            correctAnswer = OptionLetter.A,
            explanation = "Divide both sides by πh: r² = V / (πh). Taking square roots: r = √(V / πh).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1014,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Variation",
            questionText = "y varies inversely as x. If y = 6 when x = 4, find y when x = 8.",
            optionA = "2",
            optionB = "3",
            optionC = "4",
            optionD = "12",
            correctAnswer = OptionLetter.B,
            explanation = "y = k/x => k = y × x = 6 × 4 = 24. When x = 8: y = 24 / 8 = 3.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1015,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Sequence and Series",
            questionText = "The 3rd term of an A.P. is 10 and the 6th term is 19. Find the first term.",
            optionA = "2",
            optionB = "4",
            optionC = "5",
            optionD = "7",
            correctAnswer = OptionLetter.B,
            explanation = "T₃ = a + 2d = 10; T₆ = a + 5d = 19. Subtracting gives 3d = 9 => d = 3. a = 10 - 2(3) = 4.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1016,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Trigonometry",
            questionText = "If sin θ = 5/13 and θ is acute, find the value of tan θ.",
            optionA = "5/12",
            optionB = "12/13",
            optionC = "12/5",
            optionD = "13/12",
            correctAnswer = OptionLetter.A,
            explanation = "Opposite = 5, Hypotenuse = 13. Adjacent = √(13² - 5²) = 12. tan θ = 5/12.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1017,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Inequalities",
            questionText = "Solve the inequality: 3(x - 2) < 2x + 5.",
            optionA = "x < 11",
            optionB = "x > 11",
            optionC = "x < 7",
            optionD = "x > -1",
            correctAnswer = OptionLetter.A,
            explanation = "3x - 6 < 2x + 5 => 3x - 2x < 5 + 6 => x < 11.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1018,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Statistics",
            questionText = "Find the mode of the numbers: 3, 5, 7, 5, 2, 8, 5, 9.",
            optionA = "3",
            optionB = "5",
            optionC = "7",
            optionD = "8",
            correctAnswer = OptionLetter.B,
            explanation = "The number 5 appears 3 times, which is more frequent than any other score.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1019,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Matrices",
            questionText = "Find the determinant of the 2×2 matrix: [ [4, 2], [3, 5] ].",
            optionA = "10",
            optionB = "14",
            optionC = "18",
            optionD = "26",
            correctAnswer = OptionLetter.B,
            explanation = "det = (4 × 5) - (2 × 3) = 20 - 6 = 14.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1020,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Differentiation",
            questionText = "Find the derivative dy/dx of the curve y = 3x⁴ - 5x² + 7.",
            optionA = "12x³ - 10x",
            optionB = "12x³ - 10x + 7",
            optionC = "7x³ - 10x",
            optionD = "12x⁴ - 5x",
            correctAnswer = OptionLetter.A,
            explanation = "d/dx(3x⁴) = 12x³, d/dx(-5x²) = -10x, d/dx(7) = 0. Result = 12x³ - 10x.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        // --- QUESTIONS 21..25: LOCKED DEMONSTRATION QUESTIONS ---
        Question(
            id = 1021,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Integration",
            questionText = "Evaluate the definite integral ∫ from 0 to 2 of (3x² + 2) dx.",
            optionA = "10",
            optionB = "12",
            optionC = "14",
            optionD = "16",
            correctAnswer = OptionLetter.B,
            explanation = "[x³ + 2x] from 0 to 2 = (2³ + 2(2)) - 0 = 8 + 4 = 12.",
            difficulty = Difficulty.HARD,
            isFree = false
        ),
        Question(
            id = 1022,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Coordinate Geometry",
            questionText = "Find the gradient (slope) of the line passing through points (2, 3) and (6, 11).",
            optionA = "1",
            optionB = "2",
            optionC = "3",
            optionD = "4",
            correctAnswer = OptionLetter.B,
            explanation = "m = (y₂ - y₁) / (x₂ - x₁) = (11 - 3) / (6 - 2) = 8 / 4 = 2.",
            difficulty = Difficulty.MEDIUM,
            isFree = false
        ),
        Question(
            id = 1023,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Binomial Theorem",
            questionText = "Find the coefficient of x³ in the expansion of (1 + 2x)⁵.",
            optionA = "40",
            optionB = "80",
            optionC = "120",
            optionD = "160",
            correctAnswer = OptionLetter.B,
            explanation = "Term = ⁵C₃ × 1² × (2x)³ = 10 × 1 × 8x³ = 80x³. Coefficient is 80.",
            difficulty = Difficulty.HARD,
            isFree = false
        ),
        Question(
            id = 1024,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Permutation and Combination",
            questionText = "In how many ways can a committee of 3 students be selected from a class of 8 students?",
            optionA = "24",
            optionB = "56",
            optionC = "112",
            optionD = "336",
            correctAnswer = OptionLetter.B,
            explanation = "⁸C₃ = 8! / (3! × 5!) = (8 × 7 × 6) / (3 × 2 × 1) = 56.",
            difficulty = Difficulty.MEDIUM,
            isFree = false
        ),
        Question(
            id = 1025,
            examType = ExamType.JAMB,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Modular Arithmetic",
            questionText = "Find the simplest positive value of x such that 17 ≡ x (mod 5).",
            optionA = "1",
            optionB = "2",
            optionC = "3",
            optionD = "4",
            correctAnswer = OptionLetter.B,
            explanation = "17 divided by 5 gives quotient 3 with remainder 2. Thus 17 ≡ 2 (mod 5).",
            difficulty = Difficulty.EASY,
            isFree = false
        )
    )

    val physicsQuestions = listOf(
        Question(
            id = 1101,
            examType = ExamType.JAMB,
            subject = SubjectType.PHYSICS,
            year = 2025,
            topic = "Motion",
            questionText = "A body moving with an initial velocity of 5 m/s accelerates uniformly at 2 m/s² for 10 seconds. Find the distance traveled.",
            optionA = "100 m",
            optionB = "125 m",
            optionC = "150 m",
            optionD = "175 m",
            correctAnswer = OptionLetter.C,
            explanation = "s = ut + 0.5at² = (5 × 10) + 0.5(2 × 10²) = 50 + 100 = 150 m.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 1102,
            examType = ExamType.JAMB,
            subject = SubjectType.PHYSICS,
            year = 2024,
            topic = "Work Energy and Power",
            questionText = "Calculate the work done when a force of 50 N moves an object through a distance of 4 m in the direction of the force.",
            optionA = "12.5 J",
            optionB = "46 J",
            optionC = "200 J",
            optionD = "250 J",
            correctAnswer = OptionLetter.C,
            explanation = "Work done = Force × Distance = 50 N × 4 m = 200 Joules.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1103,
            examType = ExamType.JAMB,
            subject = SubjectType.PHYSICS,
            year = 2023,
            topic = "Electricity",
            questionText = "Three resistors of resistance 2Ω, 3Ω, and 6Ω are connected in parallel. What is their effective resistance?",
            optionA = "1 Ω",
            optionB = "2 Ω",
            optionC = "3 Ω",
            optionD = "11 Ω",
            correctAnswer = OptionLetter.A,
            explanation = "1/R = 1/2 + 1/3 + 1/6 = (3 + 2 + 1)/6 = 6/6 = 1 => R = 1 Ω.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )

    val chemistryQuestions = listOf(
        Question(
            id = 1201,
            examType = ExamType.JAMB,
            subject = SubjectType.CHEMISTRY,
            year = 2025,
            topic = "Atomic Structure",
            questionText = "An atom has 11 protons, 12 neutrons, and 11 electrons. What is its mass number?",
            optionA = "11",
            optionB = "12",
            optionC = "22",
            optionD = "23",
            correctAnswer = OptionLetter.D,
            explanation = "Mass number = Protons + Neutrons = 11 + 12 = 23.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1202,
            examType = ExamType.JAMB,
            subject = SubjectType.CHEMISTRY,
            year = 2024,
            topic = "Acids Bases and Salts",
            questionText = "Which of the following compounds turns blue litmus paper red?",
            optionA = "NaOH",
            optionB = "HCl",
            optionC = "NaCl",
            optionD = "NH₄OH",
            correctAnswer = OptionLetter.B,
            explanation = "Acids like hydrochloric acid (HCl) turn blue litmus paper red.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1203,
            examType = ExamType.JAMB,
            subject = SubjectType.CHEMISTRY,
            year = 2023,
            topic = "Stoichiometry",
            questionText = "Calculate the molar mass of Calcium Carbonate (CaCO₃). [Ca=40, C=12, O=16]",
            optionA = "68 g/mol",
            optionB = "84 g/mol",
            optionC = "100 g/mol",
            optionD = "120 g/mol",
            correctAnswer = OptionLetter.C,
            explanation = "Molar mass = 40 + 12 + (3 × 16) = 40 + 12 + 48 = 100 g/mol.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )

    val biologyQuestions = listOf(
        Question(
            id = 1301,
            examType = ExamType.JAMB,
            subject = SubjectType.BIOLOGY,
            year = 2025,
            topic = "Cell Biology",
            questionText = "Which cellular organelle is responsible for the synthesis of proteins?",
            optionA = "Mitochondrion",
            optionB = "Ribosome",
            optionC = "Golgi body",
            optionD = "Vacuole",
            correctAnswer = OptionLetter.B,
            explanation = "Ribosomes are the cellular machinery responsible for protein synthesis (translation).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1302,
            examType = ExamType.JAMB,
            subject = SubjectType.BIOLOGY,
            year = 2024,
            topic = "Genetics",
            questionText = "If a homozygous tall plant (TT) is crossed with a homozygous short plant (tt), what percentage of the F1 offspring will be tall?",
            optionA = "25%",
            optionB = "50%",
            optionC = "75%",
            optionD = "100%",
            correctAnswer = OptionLetter.D,
            explanation = "All F1 offspring will have the heterozygous genotype (Tt), expressing the dominant tall phenotype (100%).",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )

    val englishQuestions = listOf(
        Question(
            id = 1401,
            examType = ExamType.JAMB,
            subject = SubjectType.ENGLISH,
            year = 2025,
            topic = "Lexis and Structure",
            questionText = "Choose the option nearest in meaning to the capitalized word: The principal's speech was METICULOUSLY prepared.",
            optionA = "Carelessly",
            optionB = "Thoroughly",
            optionC = "Hastily",
            optionD = "Rudely",
            correctAnswer = OptionLetter.B,
            explanation = "Meticulous means showing great care, precision, and thoroughness.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 1402,
            examType = ExamType.JAMB,
            subject = SubjectType.ENGLISH,
            year = 2024,
            topic = "Concord",
            questionText = "Neither the headmaster nor the teachers _____ invited to the conference.",
            optionA = "was",
            optionB = "were",
            optionC = "is",
            optionD = "are",
            correctAnswer = OptionLetter.B,
            explanation = "When subjects are joined by 'neither... nor', the verb agrees with the closer subject ('teachers' -> plural past 'were').",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )
}
