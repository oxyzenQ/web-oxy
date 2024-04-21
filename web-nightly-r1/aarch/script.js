particlesJS("particles", {
        particles: {
            number: {
                value: 105,
                density: {
                    enable: true,
                    value_area: 900
                }
            },
            color: {
                value: "#00ff00"
            },
            shape: {
                type: "circle",
                stroke: {
                     width: 6,
                color: "#00ff00"
                }
            },
            opacity: {
                value: 1,
                random: true,
                animation: {
                    enable: true,
                    speed: 1,
                    opacity_min: 0,
                    sync: false
                }
            },
            size: {
                value: 3,
                random: true
            },
            line_linked: {
                enable: true,
                distance: 150,
                color: "#ffffff",
                opacity: 0.6,
                width: 2
            },
            move: {
                enable: true,
                speed: 2,
                direction: "none",
                random: true,
                straight: false,
                out_mode: "out",
                bounce: false,
            }
        },
        interactivity: {
            detectsOn: "canvas",
            events: {
                onHover: {
                    enable: true,
                    mode: "push"
                },
                onClick: {
                    enable: true,
                    mode: "push"
                },
                resize: true
            },
            modes: {
                repulse: {
                    distance: 100,
                    duration: 0.4
                },
                push: {
                    particles_nb: 4
                }
            }
        },
        retina_detect: true
    });
    
    function scrollToTop() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }
    
    window.addEventListener('scroll', function() {
        var scrollTopButton = document.querySelector('.scroll-top');
        if(this.window.pageYOffset > 200) {
            scrollTopButton.style.display = 'block';
        } else {
            scrollTopButton.style.display = 'none';
        }
    });

    window.addEventListener('scroll', function() {
        var scrollTopButton = document.querySelector('.scroll-top');
        if (window.pageYOffset > 200) {
            scrollTopButton.style.display = 'block';
        } else {
            scrollTopButton.style.display = 'none';
        }
    });
    
    window.addEventListener('scroll', function() {
        var scrollTopButton = document.querySelector('.scroll-top');
        var scrollTopIcon = scrollTopButton.querySelector('i');
        if (window.pageYOffset > 200) {
            scrollTopIcon.classList.remove('up-arrow');
        } else {
            scrollTopIcon.classList.add('up-arrow');
        }
    });
    
    
    function updateScroll() {
        var scrollTopButton = document.querySelector('.scroll-top');
        var scrollTopIcon = scrollTopButton.querySelector('i');
        if (window.pageYOffset > 200) {
            scrollTopButton.style.display = 'block';
            scrollTopIcon.classList.remove('up-arrow');
        } else {
            scrollTopButton.style.display = 'none';
            scrollTopIcon.classList.add('up-arrow');
        }
    }
    
    window.addEventListener('scroll', updateScroll);
    window.addEventListener('resize', updateScroll);

    
    var timeout;
    
    window.addEventListener('scroll', function() {
        var blogButton = document.getElementById('blogButton');
        clearTimeout(timeout);
        
        if (window.pageYOffset > 200) {
            blogButton.style.display = 'block';
            timeout = setTimeout(function() {
                blogButton.style.display = 'none';
            }, 4000);
        } else {
            blogButton.style.display = 'none';
        }
    });
    