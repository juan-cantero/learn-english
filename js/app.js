/**
 * Learn English with TV Shows
 * Interactive Learning App
 * Episode: The Pitt S01E01 - 7:00 A.M.
 */

// ============================================
// STATE MANAGEMENT
// ============================================
const state = {
    progress: {
        vocabulary: 0,
        grammar: 0,
        exercises: 0,
        total: 0
    },
    quiz: {
        currentQuestion: 0,
        answers: [],
        score: 0
    },
    matching: {
        selectedTerm: null,
        matches: []
    }
};

// Quiz Questions Data
const quizQuestions = [
    {
        question: "What does Robby mean when he says the ER is \"the job that keeps on giving\"?",
        options: [
            "The job provides many rewards and benefits",
            "The job continuously causes stress and problems (sarcastic)",
            "The job offers many opportunities for promotion",
            "The job is very generous with vacation time"
        ],
        correct: 1
    },
    {
        question: "What is \"triage\" in a hospital context?",
        options: [
            "A type of medical equipment",
            "A surgical procedure",
            "Prioritizing patients by the severity of their condition",
            "The process of discharging patients"
        ],
        correct: 2
    },
    {
        question: "When Dana says \"Just so you know, Robby's working today,\" what is she doing?",
        options: [
            "Making a complaint",
            "Giving a friendly warning or heads-up",
            "Asking a question",
            "Making a joke"
        ],
        correct: 1
    },
    {
        question: "What does \"AMA\" mean in a hospital?",
        options: [
            "American Medical Association",
            "Automated Medical Assessment",
            "Against Medical Advice (leaving without doctor approval)",
            "Advanced Medical Assistance"
        ],
        correct: 2
    },
    {
        question: "In the sentence \"They're always threatening to shut us down,\" why does Robby use 'always' + present continuous?",
        options: [
            "To describe a routine action",
            "To express annoyance about repeated behavior",
            "To talk about the future",
            "To describe something happening right now"
        ],
        correct: 1
    }
];

// ============================================
// INITIALIZATION
// ============================================
document.addEventListener('DOMContentLoaded', () => {
    initVocabularyFilter();
    initExerciseTabs();
    initFillInBlanks();
    initMatching();
    initQuiz();
    initProgressTracking();
    initSmoothScroll();
});

// ============================================
// VOCABULARY FILTER
// ============================================
function initVocabularyFilter() {
    const categoryButtons = document.querySelectorAll('.category-btn');
    const vocabCards = document.querySelectorAll('.vocab-card');

    categoryButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // Update active button
            categoryButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // Filter cards
            const category = btn.dataset.category;
            vocabCards.forEach(card => {
                if (card.dataset.category === category) {
                    card.style.display = 'block';
                    card.style.animation = 'fadeIn 0.3s ease';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });

    // Add fadeIn animation
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }
    `;
    document.head.appendChild(style);
}

// ============================================
// EXERCISE TABS
// ============================================
function initExerciseTabs() {
    const tabs = document.querySelectorAll('.exercise-tab');
    const contents = document.querySelectorAll('.exercise-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const exerciseType = tab.dataset.exercise;

            // Update active tab
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // Show corresponding content
            contents.forEach(content => {
                if (content.id === exerciseType) {
                    content.classList.add('active');
                } else {
                    content.classList.remove('active');
                }
            });
        });
    });
}

// ============================================
// FILL IN THE BLANKS
// ============================================
function initFillInBlanks() {
    const items = document.querySelectorAll('.fill-blank-item');

    items.forEach(item => {
        const input = item.querySelector('.fb-input');
        const checkBtn = item.querySelector('.check-btn');
        const feedback = item.querySelector('.fb-feedback');
        const correctAnswer = item.dataset.answer.toLowerCase();

        // Check on button click
        checkBtn.addEventListener('click', () => {
            checkAnswer(input, correctAnswer, feedback);
        });

        // Check on Enter key
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                checkAnswer(input, correctAnswer, feedback);
            }
        });
    });
}

function checkAnswer(input, correctAnswer, feedback) {
    const userAnswer = input.value.trim().toLowerCase();

    if (userAnswer === correctAnswer) {
        input.classList.remove('incorrect');
        input.classList.add('correct');
        feedback.textContent = '✓ Correct!';
        feedback.className = 'fb-feedback correct';
        updateProgress('exercises', 1);
    } else {
        input.classList.remove('correct');
        input.classList.add('incorrect');
        feedback.textContent = `✗ Try again. Hint: ${correctAnswer.charAt(0)}...`;
        feedback.className = 'fb-feedback incorrect';
    }
}

// ============================================
// MATCHING EXERCISE
// ============================================
function initMatching() {
    const terms = document.querySelectorAll('.match-term');
    const definitions = document.querySelectorAll('.match-def');
    const checkBtn = document.querySelector('.check-matching-btn');
    const resultDiv = document.querySelector('.matching-result');

    let selectedTerm = null;
    let matches = new Map();

    // Term click handler
    terms.forEach(term => {
        term.addEventListener('click', () => {
            // If already matched, ignore
            if (term.classList.contains('matched')) return;

            // Deselect previous
            terms.forEach(t => t.classList.remove('selected'));
            term.classList.add('selected');
            selectedTerm = term;
        });
    });

    // Definition click handler
    definitions.forEach(def => {
        def.addEventListener('click', () => {
            if (!selectedTerm || def.classList.contains('matched')) return;

            // Store match
            matches.set(selectedTerm.dataset.match, def.dataset.match);

            // Visual feedback
            selectedTerm.classList.remove('selected');
            selectedTerm.style.opacity = '0.6';
            def.style.opacity = '0.6';

            selectedTerm = null;
        });
    });

    // Check answers
    checkBtn.addEventListener('click', () => {
        let correct = 0;
        const total = terms.length;

        terms.forEach(term => {
            const termMatch = term.dataset.match;
            const defMatch = matches.get(termMatch);

            if (termMatch === defMatch) {
                term.classList.add('matched');
                // Find corresponding definition
                definitions.forEach(def => {
                    if (def.dataset.match === termMatch) {
                        def.classList.add('matched');
                    }
                });
                correct++;
            }
        });

        // Reset opacity
        terms.forEach(t => t.style.opacity = '1');
        definitions.forEach(d => d.style.opacity = '1');

        // Show result
        resultDiv.innerHTML = `<span style="color: ${correct === total ? 'var(--success)' : 'var(--warning)'}">
            ${correct}/${total} correct!
            ${correct === total ? ' Perfect!' : ' Try unmatched pairs again.'}
        </span>`;

        if (correct === total) {
            updateProgress('exercises', 5);
        }
    });
}

// ============================================
// QUIZ
// ============================================
function initQuiz() {
    const questionText = document.querySelector('.question-text');
    const optionsContainer = document.querySelector('.quiz-options');
    const prevBtn = document.getElementById('prevQuestion');
    const nextBtn = document.getElementById('nextQuestion');
    const currentQSpan = document.getElementById('currentQ');
    const totalQSpan = document.getElementById('totalQ');
    const progressFill = document.getElementById('quizProgressFill');
    const resultDiv = document.getElementById('quizResult');
    const quizQuestion = document.getElementById('quizQuestion');

    let currentQuestion = 0;
    let userAnswers = new Array(quizQuestions.length).fill(null);

    // Initialize
    totalQSpan.textContent = quizQuestions.length;
    renderQuestion();

    function renderQuestion() {
        const q = quizQuestions[currentQuestion];
        questionText.textContent = q.question;

        optionsContainer.innerHTML = q.options.map((option, index) => `
            <label class="quiz-option ${userAnswers[currentQuestion] === index ? 'selected' : ''}">
                <input type="radio" name="quiz" value="${index}"
                    ${userAnswers[currentQuestion] === index ? 'checked' : ''}>
                <span class="option-text">${option}</span>
            </label>
        `).join('');

        // Add click handlers
        const options = optionsContainer.querySelectorAll('.quiz-option');
        options.forEach((option, index) => {
            option.addEventListener('click', () => {
                options.forEach(o => o.classList.remove('selected'));
                option.classList.add('selected');
                userAnswers[currentQuestion] = index;
            });
        });

        // Update UI
        currentQSpan.textContent = currentQuestion + 1;
        progressFill.style.width = `${((currentQuestion + 1) / quizQuestions.length) * 100}%`;
        prevBtn.disabled = currentQuestion === 0;
        nextBtn.textContent = currentQuestion === quizQuestions.length - 1 ? 'Finish' : 'Next';
    }

    // Navigation
    prevBtn.addEventListener('click', () => {
        if (currentQuestion > 0) {
            currentQuestion--;
            renderQuestion();
        }
    });

    nextBtn.addEventListener('click', () => {
        if (currentQuestion < quizQuestions.length - 1) {
            currentQuestion++;
            renderQuestion();
        } else {
            // Show results
            showResults();
        }
    });

    function showResults() {
        let score = 0;
        userAnswers.forEach((answer, index) => {
            if (answer === quizQuestions[index].correct) {
                score++;
            }
        });

        quizQuestion.style.display = 'none';
        document.querySelector('.quiz-actions').style.display = 'none';
        document.querySelector('.quiz-progress').style.display = 'none';
        resultDiv.style.display = 'block';

        document.getElementById('scoreNumber').textContent = score;

        const messages = [
            "Keep practicing! Watch the episode again.",
            "Getting there! Review the expressions.",
            "Good job! You're learning fast.",
            "Great work! Almost perfect!",
            "Perfect score! You mastered this episode!"
        ];
        document.getElementById('resultMessage').textContent = messages[score];

        updateProgress('exercises', score * 2);
    }

    // Retry
    document.getElementById('retryQuiz').addEventListener('click', () => {
        currentQuestion = 0;
        userAnswers = new Array(quizQuestions.length).fill(null);
        quizQuestion.style.display = 'block';
        document.querySelector('.quiz-actions').style.display = 'flex';
        document.querySelector('.quiz-progress').style.display = 'flex';
        resultDiv.style.display = 'none';
        renderQuestion();
    });
}

// ============================================
// PROGRESS TRACKING
// ============================================
function initProgressTracking() {
    // Load saved progress
    const saved = localStorage.getItem('thepitt-s01e01-progress');
    if (saved) {
        Object.assign(state.progress, JSON.parse(saved));
        updateProgressBar();
    }

    // Track section views
    const sections = document.querySelectorAll('.section');
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const sectionId = entry.target.id;
                if (sectionId === 'vocabulary' && state.progress.vocabulary === 0) {
                    updateProgress('vocabulary', 10);
                } else if (sectionId === 'grammar' && state.progress.grammar === 0) {
                    updateProgress('grammar', 10);
                }
            }
        });
    }, { threshold: 0.3 });

    sections.forEach(section => observer.observe(section));
}

function updateProgress(category, points) {
    state.progress[category] = Math.min((state.progress[category] || 0) + points, 100);
    state.progress.total = Math.round(
        (state.progress.vocabulary + state.progress.grammar + state.progress.exercises) / 3
    );

    // Save to localStorage
    localStorage.setItem('thepitt-s01e01-progress', JSON.stringify(state.progress));

    updateProgressBar();
}

function updateProgressBar() {
    const progressFill = document.getElementById('progressFill');
    const progressValue = document.getElementById('progressValue');

    if (progressFill && progressValue) {
        progressFill.style.width = `${state.progress.total}%`;
        progressValue.textContent = `${state.progress.total}%`;
    }
}

// ============================================
// SMOOTH SCROLL
// ============================================
function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                const headerOffset = 80;
                const elementPosition = target.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// ============================================
// AUDIO PRONUNCIATION (placeholder)
// ============================================
document.querySelectorAll('.audio-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const word = btn.dataset.word;
        // In a real app, this would play audio
        // For now, use Web Speech API as fallback
        if ('speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance(word);
            utterance.lang = 'en-US';
            utterance.rate = 0.8;
            speechSynthesis.speak(utterance);
        }
    });
});

// ============================================
// KEYBOARD SHORTCUTS
// ============================================
document.addEventListener('keydown', (e) => {
    // Press 1-4 to navigate sections
    if (e.key >= '1' && e.key <= '4' && !e.ctrlKey && !e.metaKey) {
        const sections = ['vocabulary', 'grammar', 'expressions', 'exercises'];
        const target = document.getElementById(sections[parseInt(e.key) - 1]);
        if (target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }
});

console.log('🎬 Learn English with The Pitt - Episode 1 loaded!');
console.log('💡 Tip: Press 1-4 to navigate between sections');
