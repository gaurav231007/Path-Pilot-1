package com.example.data.db

import com.example.data.dao.PathPilotDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExamEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.StudentDNAEntity
import com.example.data.entity.SubjectEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.firstOrNull

object DatabaseInitializer {
    suspend fun seedInitialData(dao: PathPilotDao) {
        val existingExams = dao.getAllExams().firstOrNull()
        if (!existingExams.isNullOrEmpty()) {
            return // Database already seeded
        }

        // 1. Seed Users
        val studentId = dao.insertUser(
            UserEntity(
                name = "Aarav Sharma",
                email = "aarav.sharma@pathpilot.edu",
                passwordHash = "argon2_demo_secure_hash",
                role = "STUDENT",
                targetExam = "JEE"
            )
        ).toInt()

        dao.insertUser(
            UserEntity(
                name = "Dr. Vikram Seth",
                email = "admin@pathpilot.edu",
                passwordHash = "admin_master_secure_hash",
                role = "ADMIN",
                targetExam = "ALL"
            )
        )

        // 2. Seed Exams
        val jeeId = dao.insertExam(
            ExamEntity(
                code = "JEE",
                name = "JEE Main & Advanced",
                description = "Premier engineering entrance assessment targeting IITs, NITs, and IIITs.",
                totalMarks = 300,
                targetDurationMins = 180
            )
        ).toInt()

        val neetId = dao.insertExam(
            ExamEntity(
                code = "NEET",
                name = "NEET UG Medical",
                description = "National medical entrance testing Biology, Chemistry, and Physics for MBBS/BDS.",
                totalMarks = 720,
                targetDurationMins = 200
            )
        ).toInt()

        val upscId = dao.insertExam(
            ExamEntity(
                code = "UPSC",
                name = "UPSC Civil Services (CSE)",
                description = "India's prestigious administrative service examination for IAS, IPS, and IFS.",
                totalMarks = 400,
                targetDurationMins = 240
            )
        ).toInt()

        val sscId = dao.insertExam(
            ExamEntity(
                code = "SSC",
                name = "SSC CGL / CHSL",
                description = "Staff Selection Commission recruitment for central government gazetted and executive posts.",
                totalMarks = 200,
                targetDurationMins = 60
            )
        ).toInt()

        // 3. Seed Subjects for JEE
        val physicsSubId = dao.insertSubject(
            SubjectEntity(examId = jeeId, name = "Physics", weightagePercent = 33)
        ).toInt()
        val chemSubId = dao.insertSubject(
            SubjectEntity(examId = jeeId, name = "Chemistry", weightagePercent = 33)
        ).toInt()
        val mathSubId = dao.insertSubject(
            SubjectEntity(examId = jeeId, name = "Mathematics", weightagePercent = 34)
        ).toInt()

        // Seed Subjects for NEET
        val bioSubId = dao.insertSubject(
            SubjectEntity(examId = neetId, name = "Biology", weightagePercent = 50)
        ).toInt()

        // Seed Subjects for UPSC
        val politySubId = dao.insertSubject(
            SubjectEntity(examId = upscId, name = "Indian Polity", weightagePercent = 30)
        ).toInt()

        // 4. Seed Chapters
        val electrostaticsChId = dao.insertChapter(
            ChapterEntity(
                subjectId = physicsSubId,
                name = "Electrostatics & Capacitance",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 35
            )
        ).toInt()

        val thermodynamicsChId = dao.insertChapter(
            ChapterEntity(
                subjectId = physicsSubId,
                name = "Thermodynamics & Heat",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 30
            )
        ).toInt()

        val kinematicsChId = dao.insertChapter(
            ChapterEntity(
                subjectId = physicsSubId,
                name = "Kinematics & Dynamics",
                weightage = "MEDIUM",
                highPriority = false,
                estimatedMinsToMaster = 25
            )
        ).toInt()

        val chemBondingChId = dao.insertChapter(
            ChapterEntity(
                subjectId = chemSubId,
                name = "Chemical Bonding & Molecular Structure",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 30
            )
        ).toInt()

        val organicReactionChId = dao.insertChapter(
            ChapterEntity(
                subjectId = chemSubId,
                name = "Organic Reaction Mechanisms",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 40
            )
        ).toInt()

        val calculusChId = dao.insertChapter(
            ChapterEntity(
                subjectId = mathSubId,
                name = "Differential & Integral Calculus",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 45
            )
        ).toInt()

        val vectorsChId = dao.insertChapter(
            ChapterEntity(
                subjectId = mathSubId,
                name = "Vectors and 3D Geometry",
                weightage = "MEDIUM",
                highPriority = false,
                estimatedMinsToMaster = 25
            )
        ).toInt()

        val geneticsChId = dao.insertChapter(
            ChapterEntity(
                subjectId = bioSubId,
                name = "Principles of Genetics & Inheritance",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 35
            )
        ).toInt()

        val constitutionChId = dao.insertChapter(
            ChapterEntity(
                subjectId = politySubId,
                name = "Constitutional Framework & Fundamental Rights",
                weightage = "HIGH",
                highPriority = true,
                estimatedMinsToMaster = 40
            )
        ).toInt()

        // 5. Seed Questions
        val questions = listOf(
            // Electrostatics (JEE)
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = electrostaticsChId,
                difficulty = "MEDIUM",
                question = "Two equal positive point charges +q are placed at points (0, a) and (0, -a). A point charge -Q with mass m is released from rest at (x, 0) where x << a. What type of motion does it execute?",
                optionA = "Simple Harmonic Motion with period proportional to a^(3/2)",
                optionB = "Uniform rectilinear acceleration away from origin",
                optionC = "Circular orbital motion around midpoint",
                optionD = "Exponential decay towards the positive charges",
                correctAnswer = "A",
                explanation = "For small displacements x << a, the restoring net electric force along x-axis is linear: F = - (2 k q Q / a^3) x. Since F is proportional to -x, the charge executes Simple Harmonic Motion.",
                totalAttempts = 28,
                correctAttempts = 11,
                averageTimeSec = 62,
                calculatedDifficulty = "HARD"
            ),
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = electrostaticsChId,
                difficulty = "EASY",
                question = "A parallel plate capacitor is charged and then disconnected from the battery. When a dielectric slab of dielectric constant K is inserted between the plates, what happens to the stored electrostatic energy?",
                optionA = "Increases by factor K",
                optionB = "Decreases by factor 1/K",
                optionC = "Remains unchanged because charge is conserved",
                optionD = "Becomes zero due to dielectric polarization",
                correctAnswer = "B",
                explanation = "Charge Q remains constant on isolated plates. Capacitance becomes C' = K * C. Stored energy U = Q^2 / (2C') = U_0 / K, so it decreases by factor 1/K.",
                totalAttempts = 42,
                correctAttempts = 31,
                averageTimeSec = 34,
                calculatedDifficulty = "EASY"
            ),
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = electrostaticsChId,
                difficulty = "HARD",
                question = "An electric dipole of moment p is placed in a non-uniform electric field E directed along the x-axis with a positive gradient dE/dx. The net force on the dipole is:",
                optionA = "Zero, dipoles only experience torque",
                optionB = "p * (dE/dx) along positive x direction",
                optionC = "p * (dE/dx)^2 perpendicular to field",
                optionD = "-(p * E) / x",
                correctAnswer = "B",
                explanation = "In an inhomogeneous field, net force F = p * (dE/dx) along the direction of increasing field gradient when dipole aligns with E.",
                totalAttempts = 35,
                correctAttempts = 14,
                averageTimeSec = 78,
                calculatedDifficulty = "HARD"
            ),

            // Thermodynamics (JEE)
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = thermodynamicsChId,
                difficulty = "MEDIUM",
                question = "A monoatomic ideal gas undergoes an adiabatic expansion where its volume doubles. The final pressure in terms of initial pressure P0 is:",
                optionA = "P0 / 2^(5/3)",
                optionB = "P0 / 2^(7/5)",
                optionC = "P0 / 4",
                optionD = "P0 * sqrt(2)",
                correctAnswer = "A",
                explanation = "For a monoatomic gas, gamma = 5/3. The adiabatic relation is P * V^gamma = constant. P1 * V1^(5/3) = P2 * (2*V1)^(5/3) => P2 = P0 / 2^(5/3) = P0 / 3.17.",
                totalAttempts = 50,
                correctAttempts = 33,
                averageTimeSec = 48,
                calculatedDifficulty = "MEDIUM"
            ),
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = thermodynamicsChId,
                difficulty = "EASY",
                question = "In an isothermal process of an ideal gas, which thermodynamic variable remains constant throughout?",
                optionA = "Internal energy and temperature",
                optionB = "Pressure and entropy",
                optionC = "Volume and heat content",
                optionD = "Enthalpy only",
                correctAnswer = "A",
                explanation = "An isothermal process occurs at constant temperature (dT = 0). For an ideal gas, internal energy depends solely on temperature, so delta U = 0.",
                totalAttempts = 60,
                correctAttempts = 52,
                averageTimeSec = 22,
                calculatedDifficulty = "EASY"
            ),

            // Kinematics (JEE)
            QuestionEntity(
                examId = jeeId,
                subjectId = physicsSubId,
                chapterId = kinematicsChId,
                difficulty = "EASY",
                question = "A projectile is launched from ground level with speed u at angle theta with horizontal. At the highest point of its trajectory, what is its velocity and acceleration?",
                optionA = "Velocity = u * cos(theta) horizontally; Acceleration = g downward",
                optionB = "Velocity = 0; Acceleration = 0",
                optionC = "Velocity = u * sin(theta); Acceleration = g upward",
                optionD = "Velocity = u; Acceleration = g * sin(theta)",
                correctAnswer = "A",
                explanation = "At the peak apex, vertical component of velocity vy = 0, leaving only horizontal vx = u * cos(theta). Gravitational acceleration g acts vertically downward at all points.",
                totalAttempts = 55,
                correctAttempts = 48,
                averageTimeSec = 25,
                calculatedDifficulty = "EASY"
            ),

            // Chemistry - Chemical Bonding (JEE)
            QuestionEntity(
                examId = jeeId,
                subjectId = chemSubId,
                chapterId = chemBondingChId,
                difficulty = "MEDIUM",
                question = "According to VSEPR theory and hybridization rules, what is the spatial geometry and lone pair count on central xenon in XeF4?",
                optionA = "Square planar with 2 lone pairs (sp3d2)",
                optionB = "Tetrahedral with 0 lone pairs (sp3)",
                optionC = "See-saw with 1 lone pair (sp3d)",
                optionD = "Octahedral with 0 lone pairs (sp3d2)",
                correctAnswer = "A",
                explanation = "Xenon has 8 valence electrons. 4 bond pairs with fluorine leave 4 non-bonding electrons (2 lone pairs). Steric number = 4 + 2 = 6 (sp3d2), with lone pairs occupying axial positions yielding square planar molecular geometry.",
                totalAttempts = 40,
                correctAttempts = 24,
                averageTimeSec = 38,
                calculatedDifficulty = "MEDIUM"
            ),
            QuestionEntity(
                examId = jeeId,
                subjectId = chemSubId,
                chapterId = organicReactionChId,
                difficulty = "HARD",
                question = "In an SN1 solvolysis of 2-bromo-3-methylbutane in aqueous ethanol, which carbocation rearrangement primarily governs the major product?",
                optionA = "1,2-hydride shift to form the more stable tertiary carbocation",
                optionB = "1,2-methyl shift to form a primary carbocation",
                optionC = "Ring opening mechanism",
                optionD = "No rearrangement occurs in polar protic media",
                correctAnswer = "A",
                explanation = "Initial ionization creates a 2-degree secondary carbocation. Rapid 1,2-hydride shift moves hydrogen with electrons to generate a tertiary carbocation, leading to 2-methylbutan-2-ol as major product.",
                totalAttempts = 30,
                correctAttempts = 12,
                averageTimeSec = 52,
                calculatedDifficulty = "HARD"
            ),

            // Mathematics - Calculus (JEE)
            QuestionEntity(
                examId = jeeId,
                subjectId = mathSubId,
                chapterId = calculusChId,
                difficulty = "MEDIUM",
                question = "Evaluate the definite integral: Integral from 0 to pi/2 of [sin^3(x) / (sin^3(x) + cos^3(x))] dx.",
                optionA = "pi / 4",
                optionB = "pi / 2",
                optionC = "1",
                optionD = "pi / 8",
                correctAnswer = "A",
                explanation = "Using the king property: I = integral f(a+b-x) dx. Replacing x with pi/2 - x switches sin and cos. Adding 2I = integral from 0 to pi/2 of 1 dx = pi/2 => I = pi/4.",
                totalAttempts = 45,
                correctAttempts = 32,
                averageTimeSec = 44,
                calculatedDifficulty = "MEDIUM"
            ),
            QuestionEntity(
                examId = jeeId,
                subjectId = mathSubId,
                chapterId = calculusChId,
                difficulty = "HARD",
                question = "If the function f(x) = x^3 - 3ax^2 + 3bx + 1 has a local maximum at x = -1 and local minimum at x = 3, what are the values of a and b?",
                optionA = "a = 1, b = -3",
                optionB = "a = -1, b = 3",
                optionC = "a = 2, b = -1",
                optionD = "a = 3, b = 1",
                correctAnswer = "A",
                explanation = "f'(x) = 3x^2 - 6ax + 3b = 0 at x = -1 and x = 3. Sum of roots = -1 + 3 = 2 => 6a/3 = 2a => a = 1. Product of roots = (-1)(3) = -3 => 3b/3 = b => b = -3.",
                totalAttempts = 38,
                correctAttempts = 19,
                averageTimeSec = 56,
                calculatedDifficulty = "MEDIUM"
            ),

            // Biology - Genetics (NEET)
            QuestionEntity(
                examId = neetId,
                subjectId = bioSubId,
                chapterId = geneticsChId,
                difficulty = "EASY",
                question = "In a Mendelian dihybrid cross between heterozygous round yellow pea seeds (RrYy), what fraction of progeny exhibits the recombinant phenotype round green?",
                optionA = "3 / 16",
                optionB = "9 / 16",
                optionC = "1 / 16",
                optionD = "6 / 16",
                correctAnswer = "A",
                explanation = "The classic dihybrid phenotypic ratio is 9 (Round Yellow) : 3 (Round Green) : 3 (Wrinkled Yellow) : 1 (Wrinkled Green). Hence, round green is 3/16.",
                totalAttempts = 70,
                correctAttempts = 58,
                averageTimeSec = 20,
                calculatedDifficulty = "EASY"
            ),

            // Indian Polity (UPSC)
            QuestionEntity(
                examId = upscId,
                subjectId = politySubId,
                chapterId = constitutionChId,
                difficulty = "MEDIUM",
                question = "Which writ issued by the Supreme Court under Article 32 translates literally to 'We Command' to enforce public official duty?",
                optionA = "Mandamus",
                optionB = "Habeas Corpus",
                optionC = "Certiorari",
                optionD = "Quo-Warranto",
                correctAnswer = "A",
                explanation = "Mandamus is a judicial remedy in the form of an order from a court to any government, subordinate court, corporation, or public authority to do or forbear from doing some specific act which that body is obliged under law to do.",
                totalAttempts = 50,
                correctAttempts = 39,
                averageTimeSec = 28,
                calculatedDifficulty = "EASY"
            )
        )

        dao.insertQuestions(questions)

        // 6. Seed Student DNA
        dao.insertOrUpdateDNA(
            StudentDNAEntity(
                userId = studentId,
                readinessScore = 72,
                accuracy = 68.4f,
                speedSec = 44.0f,
                weakTopics = "Electrostatics & Capacitance, Organic Reaction Mechanisms",
                strongTopics = "Kinematics & Dynamics, Thermodynamics & Heat",
                totalTestsCompleted = 3,
                totalQuestionsAnswered = 35
            )
        )

        // 7. Seed Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                adminId = 2,
                action = "SYSTEM_INITIALIZED",
                targetType = "CORE_DATABASE",
                details = "Initialized Path Pilot syllabus data, 4 exam frameworks, 8 core chapters, and seed question banks."
            )
        )
    }
}
