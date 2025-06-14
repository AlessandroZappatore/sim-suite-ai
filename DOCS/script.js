// DOM Elements
const hamburger = document.querySelector('.hamburger');
const navMenu = document.querySelector('.nav-menu');
const navLinks = document.querySelectorAll('.nav-link');

// Mobile Navigation Toggle
hamburger.addEventListener('click', () => {
    navMenu.classList.toggle('active');
    hamburger.classList.toggle('active');
});

// Close mobile menu when clicking on a nav link
navLinks.forEach(link => {
    link.addEventListener('click', () => {
        navMenu.classList.remove('active');
        hamburger.classList.remove('active');
    });
});

// Smooth scrolling for navigation links
navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const targetId = link.getAttribute('href');
        const targetSection = document.querySelector(targetId);
        
        if (targetSection) {
            const navHeight = document.querySelector('.navbar').offsetHeight;
            const targetPosition = targetSection.offsetTop - navHeight;
            
            window.scrollTo({
                top: targetPosition,
                behavior: 'smooth'
            });
        }
    });
});

// Navbar scroll effect
let lastScrollTop = 0;
const navbar = document.querySelector('.navbar');

window.addEventListener('scroll', () => {
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    
    // Add/remove scrolled class for styling
    if (scrollTop > 50) {
        navbar.classList.add('scrolled');
    } else {
        navbar.classList.remove('scrolled');
    }
    
    // Hide/show navbar on scroll
    if (scrollTop > lastScrollTop && scrollTop > 100) {
        navbar.style.transform = 'translateY(-100%)';
    } else {
        navbar.style.transform = 'translateY(0)';
    }
    
    lastScrollTop = scrollTop;
});

// Intersection Observer for animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('animate-in');
        }
    });
}, observerOptions);

// Observe elements for animation
const animateElements = document.querySelectorAll('.overview-card, .tech-card, .ai-feature-card, .feature-card, .setup-step, .benefit');
animateElements.forEach(el => {
    observer.observe(el);
});

// Hero title animation (without typing effect to preserve HTML)
const heroTitle = document.querySelector('.hero-title');
if (heroTitle) {
    // Simply add a fade-in animation class
    setTimeout(() => {
        heroTitle.classList.add('animate-in');
    }, 500);
}

// Parallax effect for hero background orbs
window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const orbs = document.querySelectorAll('.gradient-orb');
    
    orbs.forEach((orb, index) => {
        const speed = 0.1 + (index * 0.05);
        orb.style.transform = `translateY(${scrolled * speed}px)`;
    });
});

// Counter animation for statistics (if any)
const counters = document.querySelectorAll('.counter');
const counterObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const counter = entry.target;
            const target = parseInt(counter.getAttribute('data-target'));
            const increment = target / 100;
            let current = 0;
            
            const updateCounter = () => {
                current += increment;
                counter.textContent = Math.floor(current);
                
                if (current < target) {
                    requestAnimationFrame(updateCounter);
                } else {
                    counter.textContent = target;
                }
            };
            
            updateCounter();
            counterObserver.unobserve(counter);
        }
    });
}, { threshold: 0.5 });

counters.forEach(counter => {
    counterObserver.observe(counter);
});

// Copy to clipboard functionality for code blocks
const codeBlocks = document.querySelectorAll('.code-block');
codeBlocks.forEach(block => {
    const copyButton = document.createElement('button');
    copyButton.className = 'copy-btn';
    copyButton.innerHTML = '<i class="fas fa-copy"></i>';
    copyButton.setAttribute('title', 'Copia il codice');
    
    block.style.position = 'relative';
    block.appendChild(copyButton);
    
    copyButton.addEventListener('click', () => {
        const code = block.querySelector('code').textContent;
        navigator.clipboard.writeText(code).then(() => {
            copyButton.innerHTML = '<i class="fas fa-check"></i>';
            copyButton.style.color = '#00ff88';
            
            setTimeout(() => {
                copyButton.innerHTML = '<i class="fas fa-copy"></i>';
                copyButton.style.color = '';
            }, 2000);
        });
    });
});

// Floating action button for GitHub
const createFloatingButton = () => {
    const floatingBtn = document.createElement('a');
    floatingBtn.href = '#setup';
    floatingBtn.className = 'floating-github-btn';
    floatingBtn.innerHTML = '<i class="fab fa-github"></i>';
    floatingBtn.setAttribute('title', 'Vai al GitHub');
    
    document.body.appendChild(floatingBtn);
    
    // Show/hide based on scroll position
    window.addEventListener('scroll', () => {
        if (window.pageYOffset > 300) {
            floatingBtn.classList.add('show');
        } else {
            floatingBtn.classList.remove('show');
        }
    });
};

// Theme toggle functionality (optional)
const createThemeToggle = () => {
    const themeToggle = document.createElement('button');
    themeToggle.className = 'theme-toggle';
    themeToggle.innerHTML = '<i class="fas fa-moon"></i>';
    themeToggle.setAttribute('title', 'Cambia tema');
    
    document.querySelector('.nav-container').appendChild(themeToggle);
    
    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('dark-theme');
        const isDark = document.body.classList.contains('dark-theme');
        themeToggle.innerHTML = isDark ? '<i class="fas fa-sun"></i>' : '<i class="fas fa-moon"></i>';
        
        // Save preference
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
    });
    
    // Load saved theme
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        document.body.classList.add('dark-theme');
        themeToggle.innerHTML = '<i class="fas fa-sun"></i>';
    }
};

// Progressive loading for images
const lazyImages = document.querySelectorAll('img[data-src]');
const imageObserver = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            img.classList.remove('lazy');
            imageObserver.unobserve(img);
        }
    });
});

lazyImages.forEach(img => {
    imageObserver.observe(img);
});

// Particles animation for hero section
const createParticles = () => {
    const particlesContainer = document.querySelector('.hero-bg');
    if (!particlesContainer) return;
    
    const particleCount = 50;
    
    for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        particle.style.cssText = `
            position: absolute;
            width: 2px;
            height: 2px;
            background: rgba(0, 102, 255, 0.3);
            border-radius: 50%;
            animation: float-particle ${Math.random() * 10 + 5}s infinite linear;
            left: ${Math.random() * 100}%;
            top: ${Math.random() * 100}%;
            animation-delay: ${Math.random() * 5}s;
        `;
        
        particlesContainer.appendChild(particle);
    }
};

// Keyboard navigation
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        navMenu.classList.remove('active');
        hamburger.classList.remove('active');
    }
});

// Initialize all features
document.addEventListener('DOMContentLoaded', () => {
    createFloatingButton();
    createParticles();
    
    // Add animate-in class to elements
    setTimeout(() => {
        const elements = document.querySelectorAll('.hero-content, .section-header');
        elements.forEach((el, index) => {
            setTimeout(() => {
                el.classList.add('animate-in');
            }, index * 200);
        });
    }, 500);
});

// Performance optimization
const debounce = (func, wait) => {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};

// Debounced scroll handler
const debouncedScrollHandler = debounce(() => {
    // Additional scroll-based functionality can be added here
}, 10);

window.addEventListener('scroll', debouncedScrollHandler);

// Add smooth reveal animations
const revealElements = document.querySelectorAll('.overview-card, .tech-card, .ai-feature-card');
const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry, index) => {
        if (entry.isIntersecting) {
            setTimeout(() => {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }, index * 100);
            revealObserver.unobserve(entry.target);
        }
    });
}, { threshold: 0.1 });

revealElements.forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'all 0.6s ease-out';
    revealObserver.observe(el);
});

// Error handling
window.addEventListener('error', (e) => {
    console.warn('Si è verificato un errore:', e.error);
});

// Service worker registration (for PWA capabilities)
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(registration => {
                console.log('SW registered: ', registration);
            })
            .catch(registrationError => {
                console.log('SW registration failed: ', registrationError);
            });
    });
}

// Interactive Demo Functionality
class SimSuiteAIDemo {
    constructor() {
        this.baseURL = 'http://localhost:8001';
        this.init();
    }

    init() {
        this.setupTabNavigation();
        this.setupForms();
    }

    setupTabNavigation() {
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTab = button.getAttribute('data-tab');
                
                // Remove active class from all buttons and contents
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Add active class to clicked button and corresponding content
                button.classList.add('active');
                document.getElementById(`${targetTab}-tab`).classList.add('active');
            });
        });
    }

    setupForms() {
        // Scenario Form
        const scenarioForm = document.getElementById('scenario-form');
        if (scenarioForm) {
            scenarioForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.generateScenario(scenarioForm);
            });
        }

        // Materials Form
        const materialsForm = document.getElementById('materials-form');
        if (materialsForm) {
            materialsForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.generateMaterials(materialsForm);
            });
        }

        // Exams Form
        const examsForm = document.getElementById('exams-form');
        if (examsForm) {
            examsForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.generateExams(examsForm);
            });
        }

        // Reports Form
        const reportsForm = document.getElementById('reports-form');
        if (reportsForm) {
            reportsForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.generateReports(reportsForm);
            });
        }
    }

    async generateScenario(form) {
        const formData = new FormData(form);
        const submitButton = form.querySelector('button[type="submit"]');
        const resultDiv = document.getElementById('scenario-result');
        const contentDiv = document.getElementById('scenario-content');

        // Prepare request data
        const requestData = {
            description: formData.get('description'),
            scenario_type: formData.get('scenario_type'),
            target: formData.get('target') || null,
            difficulty: formData.get('difficulty')
        };

        this.showLoading(submitButton, 'Generando scenario...');
        this.hideResult(resultDiv);

        try {
            const response = await fetch(`${this.baseURL}/scenarios/generate-scenario`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayScenarioResult(data, contentDiv);
            this.showResult(resultDiv);
            
        } catch (error) {
            this.showError(contentDiv, `Errore nella generazione dello scenario: ${error.message}`);
            this.showResult(resultDiv);
        } finally {
            this.hideLoading(submitButton, 'Genera Scenario');
        }
    }

    async generateMaterials(form) {
        const formData = new FormData(form);
        const submitButton = form.querySelector('button[type="submit"]');
        const resultDiv = document.getElementById('materials-result');
        const contentDiv = document.getElementById('materials-content');

        const requestData = {
            scenario_description: formData.get('scenario_description'),
            patient_type: formData.get('patient_type'),
            target_audience: formData.get('target_audience'),
            objective_exam: formData.get('objective_exam') || null
        };

        this.showLoading(submitButton, 'Generando materiali...');
        this.hideResult(resultDiv);

        try {
            const response = await fetch(`${this.baseURL}/materials/generate-materials`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayMaterialsResult(data, contentDiv);
            this.showResult(resultDiv);
            
        } catch (error) {
            this.showError(contentDiv, `Errore nella generazione dei materiali: ${error.message}`);
            this.showResult(resultDiv);
        } finally {
            this.hideLoading(submitButton, 'Genera Materiali');
        }
    }

    async generateExams(form) {
        const formData = new FormData(form);
        const submitButton = form.querySelector('button[type="submit"]');
        const resultDiv = document.getElementById('exams-result');
        const contentDiv = document.getElementById('exams-content');

        const requestData = {
            scenario_description: formData.get('scenario_description'),
            patient_type: formData.get('patient_type')
        };

        this.showLoading(submitButton, 'Generando esami...');
        this.hideResult(resultDiv);

        try {
            const response = await fetch(`${this.baseURL}/exams/generate-lab-exams`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayExamsResult(data, contentDiv);
            this.showResult(resultDiv);
            
        } catch (error) {
            this.showError(contentDiv, `Errore nella generazione degli esami: ${error.message}`);
            this.showResult(resultDiv);
        } finally {
            this.hideLoading(submitButton, 'Genera Esami');
        }
    }

    async generateReports(form) {
        const formData = new FormData(form);
        const submitButton = form.querySelector('button[type="submit"]');
        const resultDiv = document.getElementById('reports-result');
        const contentDiv = document.getElementById('reports-content');

        const requestData = {
            scenario_description: formData.get('scenario_description'),
            exam_type: formData.get('exam_type'),
            patient_type: formData.get('patient_type')
        };

        this.showLoading(submitButton, 'Generando referto...');
        this.hideResult(resultDiv);

        try {
            const response = await fetch(`${this.baseURL}/reports/generate-medical-report`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayReportsResult(data, contentDiv);
            this.showResult(resultDiv);
            
        } catch (error) {
            this.showError(contentDiv, `Errore nella generazione del referto: ${error.message}`);
            this.showResult(resultDiv);
        } finally {
            this.hideLoading(submitButton, 'Genera Referto');
        }
    }

    displayScenarioResult(data, contentDiv) {
        const html = `
            <div class="scenario-result-content">
                <h5><i class="fas fa-file-medical"></i> ${data.title || 'Scenario Generato'}</h5>
                <div class="scenario-details">
                    <p><strong>Descrizione:</strong> ${data.description || 'N/A'}</p>
                    <p><strong>Durata:</strong> ${data.duration || 'N/A'}</p>
                    <p><strong>Difficoltà:</strong> ${data.difficulty || 'N/A'}</p>
                    ${data.objectives ? `<p><strong>Obiettivi:</strong> ${data.objectives}</p>` : ''}
                    ${data.scenario_content ? `<div class="scenario-content"><h6>Contenuto Scenario:</h6><pre>${JSON.stringify(data.scenario_content, null, 2)}</pre></div>` : ''}
                </div>
            </div>
        `;
        contentDiv.innerHTML = html;
    }

    displayMaterialsResult(data, contentDiv) {
        if (!Array.isArray(data) || data.length === 0) {
            contentDiv.innerHTML = '<p>Nessun materiale generato.</p>';
            return;
        }

        const materialsHTML = data.map(material => `
            <div class="material-item">
                <div class="material-name">${material.name || 'Materiale'}</div>
                <div class="material-description">${material.description || 'Nessuna descrizione disponibile'}</div>
            </div>
        `).join('');

        contentDiv.innerHTML = `
            <div class="materials-list">
                ${materialsHTML}
            </div>
        `;
    }

    displayExamsResult(data, contentDiv) {
        if (!data.exams || !Array.isArray(data.exams) || data.exams.length === 0) {
            contentDiv.innerHTML = '<p>Nessun esame generato.</p>';
            return;
        }

        const examsHTML = data.exams.map(exam => `
            <div class="lab-exam-section">
                <div class="lab-exam-title">
                    <i class="fas fa-flask"></i>
                    ${exam.name || 'Esame di Laboratorio'}
                </div>
                ${exam.values ? `
                    <div class="lab-values">
                        ${Object.entries(exam.values).map(([key, value]) => `
                            <div class="lab-value">
                                <span class="lab-value-name">${key}</span>
                                <span class="lab-value-result">${value}</span>
                            </div>
                        `).join('')}
                    </div>
                ` : ''}
                ${exam.report ? `
                    <div class="lab-report">
                        <div class="lab-report-title">Referto:</div>
                        <p>${exam.report}</p>
                    </div>
                ` : ''}
            </div>
        `).join('');

        contentDiv.innerHTML = `
            <div class="lab-exams-container">
                ${examsHTML}
                ${data.summary ? `
                    <div class="lab-summary">
                        <h6><i class="fas fa-clipboard-list"></i> Sommario</h6>
                        <p>${data.summary}</p>
                    </div>
                ` : ''}
            </div>
        `;
    }

    displayReportsResult(data, contentDiv) {
        const html = `
            <div class="medical-report-content">
                ${data.exam_type ? `<h6><i class="fas fa-file-medical-alt"></i> ${data.exam_type}</h6>` : ''}
                ${data.patient_data ? `
                    <div class="patient-info">
                        <h6>Dati Paziente:</h6>
                        <pre>${JSON.stringify(data.patient_data, null, 2)}</pre>
                    </div>
                ` : ''}
                ${data.report ? `
                    <div class="report-content">
                        <h6>Referto:</h6>
                        <div class="report-text">${data.report}</div>
                    </div>
                ` : ''}
                ${data.conclusions ? `
                    <div class="report-conclusions">
                        <h6>Conclusioni:</h6>
                        <p>${data.conclusions}</p>
                    </div>
                ` : ''}
                ${data.recommendations ? `
                    <div class="report-recommendations">
                        <h6>Raccomandazioni:</h6>
                        <p>${data.recommendations}</p>
                    </div>
                ` : ''}
            </div>
        `;
        contentDiv.innerHTML = html;
    }

    showLoading(button, text) {
        button.disabled = true;
        button.innerHTML = `<span class="loading-spinner"></span>${text}`;
    }

    hideLoading(button, originalText) {
        button.disabled = false;
        button.innerHTML = `<i class="fas fa-magic"></i>${originalText}`;
    }

    showResult(resultDiv) {
        resultDiv.style.display = 'block';
        resultDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    hideResult(resultDiv) {
        resultDiv.style.display = 'none';
    }

    showError(contentDiv, message) {
        contentDiv.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-triangle"></i>
                ${message}
            </div>
            <div class="error-help">
                <p><strong>Suggerimenti:</strong></p>
                <ul>
                    <li>Verifica che il backend sia in esecuzione su <code>localhost:8001</code></li>
                    <li>Controlla la console del browser per errori dettagliati</li>
                    <li>Assicurati che le API keys siano configurate correttamente</li>
                </ul>
            </div>
        `;
    }
}

// Video Autoplay on Viewport Entry
class VideoAutoplayManager {
    constructor() {
        this.videos = document.querySelectorAll('.auto-play-video');
        this.init();
    }

    init() {
        // Create intersection observer for video autoplay
        const videoObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                const video = entry.target;
                
                if (entry.isIntersecting) {
                    // Video entered viewport
                    this.playVideo(video);
                } else {
                    // Video left viewport
                    this.pauseVideo(video);
                }
            });
        }, {
            threshold: 0.5, // Video needs to be 50% visible
            rootMargin: '0px 0px -50px 0px' // Trigger slightly before fully visible
        });

        // Observe all auto-play videos
        this.videos.forEach(video => {
            videoObserver.observe(video);
            
            // Add event listeners for user interaction
            this.setupVideoListeners(video);
        });
    }

    playVideo(video) {
        // Only autoplay if video is not already playing and user hasn't interacted
        if (video.paused && !video.dataset.userInteracted) {
            video.play().catch(error => {
                console.log('Autoplay prevented:', error);
                // Autoplay was prevented, usually due to browser policy
                // The video will still have controls for manual play
            });
        }
    }

    pauseVideo(video) {
        // Only pause if user hasn't manually started the video
        if (!video.paused && !video.dataset.userInteracted) {
            video.pause();
        }
    }

    setupVideoListeners(video) {
        // Track user interaction to prevent auto-pause
        video.addEventListener('play', () => {
            if (!video.dataset.autoPlaying) {
                video.dataset.userInteracted = 'true';
            }
        });

        video.addEventListener('pause', () => {
            if (!video.dataset.autoPlaying) {
                video.dataset.userInteracted = 'true';
            }
        });

        // Reset user interaction when video ends
        video.addEventListener('ended', () => {
            video.dataset.userInteracted = 'false';
        });

        // Handle video loading
        video.addEventListener('loadeddata', () => {
            console.log('Video loaded:', video.src);
        });

        // Handle video errors
        video.addEventListener('error', (e) => {
            console.error('Video error:', e);
        });
    }
}

// Initialize demo when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Interactive Demo
    new SimSuiteAIDemo();
    
    // Initialize Video Autoplay
    new VideoAutoplayManager();
});
