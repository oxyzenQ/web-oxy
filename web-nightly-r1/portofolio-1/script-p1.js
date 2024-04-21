const card = document.querySelector('#card');
const cardHeader = card.querySelector('header');
const resetBtn = document.querySelector('.reset');
let clicked = false;
let startTop = card.offsetTop;
let startLeft = card.offsetLeft;
let offsetX, offsetY;

cardHeader.addEventListener('mousedown', (e) => {
    clicked = true;
    offsetX = e.clientX - card.offsetLeft;
    offsetY = e.clientY - card.offsetTop;
});

document.addEventListener('mouseup', () => {
    clicked = false;
});

document.addEventListener('mousemove', (e) => {
    if (!clicked) return;
    const { clientX, clientY } = e;
    card.style.left = `${e.clientX - offsetX}px`;
    card.style.top = `${e.clientY - offsetY}px`;
});

resetBtn.addEventListener('click', (e) => {
    e.preventDefault();
    resetPosition();
});

function resetPosition() {
    const parent = card.parentElement;
    const parentWidth = parent.offsetWidth;
    const parentHeight = parent.offsetHeight;
    const cardWidth = card.offsetWidth;
    const cardHeight = card.offsetHeight;
    const centerLeft = (parentWidth - cardWidth) / 2;
    const centerTop = (parentHeight - cardHeight) / 2;

    // Remove any existing transition class
    card.classList.remove('reset-animation');

    // Force reflow before adding the class again
    void card.offsetWidth;

    // Add CSS class for smooth transition
    card.classList.add('reset-animation');

    card.style.left = `${centerLeft}px`;
    card.style.top = `${centerTop}px`;
}