package com.success.examprep.data.local.questions

import com.success.examprep.data.model.Difficulty
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.OptionLetter
import com.success.examprep.data.model.Question
import com.success.examprep.data.model.SubjectType

/**
 * Dedicated repository data source for NECO SSCE questions.
 * Distinct original questions specifically aligned with NECO examination standards.
 */
object NecoQuestions {

    val allQuestions: List<Question> by lazy {
        mathematicsQuestions + physicsQuestions + chemistryQuestions + biologyQuestions + englishQuestions +
        com.success.examprep.data.local.questions.subjects.NecoMathematicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoPhysicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoChemistryQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoBiologyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoEnglishQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoGovernmentQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoCrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoIrsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoLiteratureQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoCommerceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoEconomicsQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoGeographyQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoAgriculturalScienceQuestions.questions +
        com.success.examprep.data.local.questions.subjects.NecoCivicEducationQuestions.questions
    }

    val mathematicsQuestions = listOf(
        Question(
            id = 3001,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Algebra",
            questionText = "Simplify the surd expression: √75 - √12 + √27.",
            optionA = "4√3",
            optionB = "5√3",
            optionC = "6√3",
            optionD = "7√3",
            correctAnswer = OptionLetter.C,
            explanation = "√75 = √(25 × 3) = 5√3.\n√12 = √(4 × 3) = 2√3.\n√27 = √(9 × 3) = 3√3.\n5√3 - 2√3 + 3√3 = (5 - 2 + 3)√3 = 6√3.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 3002,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Simultaneous Equations",
            questionText = "Solve for x and y: 3x - 2y = 12 and x + 2y = 4.",
            optionA = "x = 4, y = 0",
            optionB = "x = 3, y = -1",
            optionC = "x = 2, y = -3",
            optionD = "x = 5, y = 1",
            correctAnswer = OptionLetter.A,
            explanation = "Adding the two equations:\n(3x + x) + (-2y + 2y) = 12 + 4 => 4x = 16 => x = 4.\nSubstitute into x + 2y = 4 => 4 + 2y = 4 => 2y = 0 => y = 0.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3003,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Quadratic Equations",
            questionText = "Find the sum and product of the roots of the equation 2x² - 8x + 5 = 0.",
            optionA = "Sum = 4, Product = 2.5",
            optionB = "Sum = -4, Product = 2.5",
            optionC = "Sum = 8, Product = 5",
            optionD = "Sum = -8, Product = -5",
            correctAnswer = OptionLetter.A,
            explanation = "For ax² + bx + c = 0:\nSum of roots = -b/a = -(-8)/2 = 4.\nProduct of roots = c/a = 5/2 = 2.5.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 3004,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Geometry",
            questionText = "Find the total surface area of a closed cylinder of radius 7 cm and height 10 cm. [Take π = 22/7]",
            optionA = "440 cm²",
            optionB = "594 cm²",
            optionC = "748 cm²",
            optionD = "896 cm²",
            correctAnswer = OptionLetter.C,
            explanation = "Total Surface Area = 2πr(r + h) = 2 × (22/7) × 7 × (7 + 10) = 44 × 17 = 748 cm².",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 3005,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Trigonometry",
            questionText = "If tan θ = 3/4 and θ is an acute angle, find the value of cos θ.",
            optionA = "3/5",
            optionB = "4/5",
            optionC = "5/4",
            optionD = "5/3",
            correctAnswer = OptionLetter.B,
            explanation = "Opposite = 3, Adjacent = 4.\nHypotenuse = √(3² + 4²) = 5.\ncos θ = Adjacent / Hypotenuse = 4/5.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3006,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2023,
            topic = "Statistics",
            questionText = "Calculate the standard deviation of the numbers: 2, 4, 6, 8, 10.",
            optionA = "√8 ≈ 2.83",
            optionB = "√10 ≈ 3.16",
            optionC = "4.00",
            optionD = "5.00",
            correctAnswer = OptionLetter.A,
            explanation = "Mean = (2+4+6+8+10)/5 = 6.\nDeviations: -4, -2, 0, 2, 4.\nSquared deviations: 16, 4, 0, 4, 16. Sum = 40.\nVariance = 40/5 = 8. Standard deviation = √8 ≈ 2.83.",
            difficulty = Difficulty.HARD,
            isFree = true
        ),
        Question(
            id = 3007,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Probability",
            questionText = "Two fair coins are tossed simultaneously. What is the probability of obtaining at least one Head?",
            optionA = "1/4",
            optionB = "1/2",
            optionC = "3/4",
            optionD = "1",
            correctAnswer = OptionLetter.C,
            explanation = "Sample space = {HH, HT, TH, TT} (4 outcomes).\nOutcomes with at least one head = {HH, HT, TH} (3 outcomes).\nProbability = 3/4.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3008,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2022,
            topic = "Indices",
            questionText = "Solve for x in the equation: 2^(x + 3) = 64.",
            optionA = "2",
            optionB = "3",
            optionC = "4",
            optionD = "5",
            correctAnswer = OptionLetter.B,
            explanation = "64 = 2⁶.\n2^(x + 3) = 2⁶ => x + 3 = 6 => x = 3.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3009,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2025,
            topic = "Logarithms",
            questionText = "Evaluate without tables: log₂ 32 - log₂ 4.",
            optionA = "2",
            optionB = "3",
            optionC = "4",
            optionD = "8",
            correctAnswer = OptionLetter.B,
            explanation = "log₂ 32 = 5, log₂ 4 = 2.\n5 - 2 = 3. (Or log₂(32/4) = log₂ 8 = 3).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3010,
            examType = ExamType.NECO,
            subject = SubjectType.MATHEMATICS,
            year = 2024,
            topic = "Sequence and Series",
            questionText = "Find the sum of the first 5 terms of the geometric progression (G.P.): 2, 6, 18, ...",
            optionA = "162",
            optionB = "242",
            optionC = "256",
            optionD = "486",
            correctAnswer = OptionLetter.B,
            explanation = "First term a = 2, common ratio r = 6/2 = 3.\nS₅ = a(r⁵ - 1)/(r - 1) = 2(3⁵ - 1)/(3 - 1) = 2(243 - 1)/2 = 242.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )

    val physicsQuestions = listOf(
        Question(
            id = 3101,
            examType = ExamType.NECO,
            subject = SubjectType.PHYSICS,
            year = 2025,
            topic = "Optics",
            questionText = "The focal length of a concave mirror is 15 cm. What is its radius of curvature?",
            optionA = "7.5 cm",
            optionB = "15 cm",
            optionC = "30 cm",
            optionD = "45 cm",
            correctAnswer = OptionLetter.C,
            explanation = "Radius of curvature r = 2f = 2 × 15 = 30 cm.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3102,
            examType = ExamType.NECO,
            subject = SubjectType.PHYSICS,
            year = 2024,
            topic = "Force",
            questionText = "Which principle states that an upthrust exerted on a submerged body equals the weight of fluid displaced?",
            optionA = "Hooke's Principle",
            optionB = "Pascal's Principle",
            optionC = "Archimedes' Principle",
            optionD = "Bernoulli's Principle",
            correctAnswer = OptionLetter.C,
            explanation = "Archimedes' Principle governs buoyant upthrust and displaced fluid weights.",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )

    val chemistryQuestions = listOf(
        Question(
            id = 3201,
            examType = ExamType.NECO,
            subject = SubjectType.CHEMISTRY,
            year = 2025,
            topic = "Chemical Bonding",
            questionText = "Which type of chemical bond is present in an Oxygen gas molecule (O₂)?",
            optionA = "Single covalent bond",
            optionB = "Double covalent bond",
            optionC = "Triple covalent bond",
            optionD = "Ionic bond",
            correctAnswer = OptionLetter.B,
            explanation = "Two oxygen atoms share two pairs of valence electrons, forming a double covalent bond (O=O).",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3202,
            examType = ExamType.NECO,
            subject = SubjectType.CHEMISTRY,
            year = 2024,
            topic = "Acids Bases and Salts",
            questionText = "Water hardness that can be removed simply by boiling is caused by the presence of:",
            optionA = "Calcium sulphate",
            optionB = "Magnesium sulphate",
            optionC = "Calcium hydrogentrioxocarbonate(IV)",
            optionD = "Sodium chloride",
            correctAnswer = OptionLetter.C,
            explanation = "Temporary hardness is caused by dissolved calcium or magnesium hydrogentrioxocarbonate(IV) which decomposes upon heating.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        )
    )

    val biologyQuestions = listOf(
        Question(
            id = 3301,
            examType = ExamType.NECO,
            subject = SubjectType.BIOLOGY,
            year = 2025,
            topic = "Evolution",
            questionText = "The theory of evolution by natural selection was formulated by:",
            optionA = "Gregor Mendel",
            optionB = "Charles Darwin",
            optionC = "Jean-Baptiste Lamarck",
            optionD = "Louis Pasteur",
            correctAnswer = OptionLetter.B,
            explanation = "Charles Darwin authored 'On the Origin of Species', establishing natural selection.",
            difficulty = Difficulty.EASY,
            isFree = true
        ),
        Question(
            id = 3302,
            examType = ExamType.NECO,
            subject = SubjectType.BIOLOGY,
            year = 2024,
            topic = "Reproduction",
            questionText = "The transfer of pollen grains from the anther to the stigma of a flower is called:",
            optionA = "Fertilization",
            optionB = "Pollination",
            optionC = "Germination",
            optionD = "Transpiration",
            correctAnswer = OptionLetter.B,
            explanation = "Pollination is the mechanical transfer of pollen from the male anther to the receptive female stigma.",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )

    val englishQuestions = listOf(
        Question(
            id = 3401,
            examType = ExamType.NECO,
            subject = SubjectType.ENGLISH,
            year = 2025,
            topic = "Lexis and Structure",
            questionText = "Fill the gap: The police officer asked the driver to produce his driver's _____.",
            optionA = "lisence",
            optionB = "license",
            optionC = "licence",
            optionD = "lisense",
            correctAnswer = OptionLetter.C,
            explanation = "In British English (followed in Nigeria), the noun is spelled 'licence', while 'license' is the verb.",
            difficulty = Difficulty.MEDIUM,
            isFree = true
        ),
        Question(
            id = 3402,
            examType = ExamType.NECO,
            subject = SubjectType.ENGLISH,
            year = 2024,
            topic = "Figures of Speech",
            questionText = "Name the figure of speech in: 'The camel is the ship of the desert.'",
            optionA = "Metaphor",
            optionB = "Simile",
            optionC = "Hyperbole",
            optionD = "Irony",
            correctAnswer = OptionLetter.A,
            explanation = "An implied comparison without the use of 'as' or 'like' is a metaphor.",
            difficulty = Difficulty.EASY,
            isFree = true
        )
    )
}
