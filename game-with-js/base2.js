//by oxyrezz
const cvs = document.querySelector("#canvas1");
const ctx = cvs.getContext('2d');

// Menyiapkan gambar mobil
const car = new Image();
car.src = "assets/carspeed.png";
car.width = 24; // Mengatur lebar mobil menjadi 24px
car.height = 24; // Mengatur tinggi mobil menjadi 24px

// Default posisi mobil
let carX = 10;
let carY = 120;

// Kecepatan mobil
let carSpeed = 2;
let carSpeedIncrement = 0.5; // Increment untuk mengatur kecepatan mobil

// Menyiapkan gambar alien
const obstacle = new Image();
obstacle.src = "assets/alien.png";
obstacle.width = 24; // Mengatur lebar alien menjadi 24px
obstacle.height = 24; // Mengatur tinggi alien menjadi 24px

let obs = [];

// Default skor
let score = 0;

// Maksimum skor yang diinginkan
const maxScore = 50; // Maksimum skor

// Jumlah alien yang ingin ditampilkan
const numAliens = 5;

// Set font pixelade 8 bit
ctx.font = "Pixelade";
ctx.textAlign = "center"; // Pusatkan teks secara horizontal

// Variabel untuk menandakan apakah permainan sedang berlangsung atau tidak
let gameRunning = false;

// Fungsi untuk membuat alien dengan posisi acak
const createRandomAliens = () => {
  obs = [];
  for (let i = 0; i < numAliens; i++) {
    obs.push({
      x: cvs.width + Math.random() * 500, // Posisi awal alien di luar layar kanan
      y: Math.random() * (cvs.height - obstacle.height) // Posisi alien secara acak di kanvas
    });
  }
};

// Fungsi untuk menggambar mobil
const drawCar = () => {
  ctx.drawImage(car, carX, carY, car.width, car.height);
};

// Fungsi untuk menggambar alien
const drawAliens = () => {
  obs.forEach(alien => {
    ctx.drawImage(obstacle, alien.x, alien.y, obstacle.width, obstacle.height);
  });
};

// Fungsi untuk mengecek tabrakan antara mobil dan alien
const checkCollision = () => {
  obs.forEach(alien => {
    if (
      carX + car.width >= alien.x &&
      carX <= alien.x + obstacle.width &&
      carY + car.height >= alien.y &&
      carY <= alien.y + obstacle.height
    ) {
      // Memainkan suara ketika mobil menabrak alien
      const loseSound = document.getElementById("loseSound");
      loseSound.play();

      // Menunda alert untuk memberikan waktu suara diputar
      setTimeout(() => {
        alert("Game Over!");
      }, 100); // Sesuaikan dengan durasi suara

      // Mengatur ulang skor dan posisi mobil dan alien
      score = 0;
      carX = 10;
      carY = 120;
      createRandomAliens();
    }
  });
};

// Fungsi untuk mengupdate skor
const updateScore = () => {
  ctx.fillStyle = "#fff";
  ctx.fillText(`Skor : ${score}`, cvs.width / 2, 15); // Pusatkan teks secara horizontal
};

// Fungsi untuk mengupdate posisi alien
const updateAliensPosition = () => {
  obs.forEach(alien => {
    alien.x -= carSpeed;
    if (alien.x + obstacle.width <= 0) {
      // Alien keluar dari layar, posisi direset dan skor ditambah
      alien.x = cvs.width + Math.random() * 500;
      alien.y = Math.random() * (cvs.height - obstacle.height);
      score += 3;

      //play sound
      const successSound = document.getElementById("successSound");
      successSound.play();

      if (score > maxScore) {
        alert("Congrats you win!");
        // Reset skor dan posisi mobil dan alien
        score = 0;
        carX = 10;
        carY = 120;
        createRandomAliens();
      }
    }
  });
};

// Fungsi utama permainan
const main = () => {
  if (gameRunning) {
    ctx.clearRect(0, 0, cvs.width, cvs.height);
    drawCar();
    drawAliens();
    checkCollision();
    updateScore();
    updateAliensPosition();
    requestAnimationFrame(main);
  } else {
    // Layar kosong, gambar pesan selamat datang
    ctx.fillStyle = "white";
    if (document.activeElement.id === "pauseButton") {
      // Hanya menampilkan teks "need coffe?" jika tombol "Pause" diklik
      ctx.fillText("Need coffee?", cvs.width / 2, cvs.height / 2); // Pusatkan teks secara horizontal dan vertikal
    } else {
      //menampilkan text jika tidak aktif
      ctx.fillText("Welcome, just simple game.", cvs.width / 5 + 90, cvs.height / 2 - 20); // Pusatkan teks secara horizontal dan vertikal
      ctx.fillText("Let's press the some option buttons below.", cvs.width / 5 + 90, cvs.height / 2 + 20); // Pusatkan teks secara horizontal dan vertikal
    }
  }
};

// Menerima input keyboard dari pengguna
document.addEventListener('keydown', function(event) {
  switch (event.keyCode) {
    case 37: // Tombol kiri
      if (carX > 0) {
        carX -= carSpeed;
      }
      break;
    case 38: // Tombol atas
      if (carY > 0) {
        carY -= carSpeed;
      }
      break;
    case 39: // Tombol kanan
      if (carX + car.width < cvs.width) {
        carX += carSpeed;
      }
      break;
    case 40: // Tombol bawah
      if (carY + car.height < cvs.height) {
        carY += carSpeed;
      }
      break;
  }
});

// Menangani klik tombol "Start"
document.getElementById('startButton').addEventListener('click', function() {
  if (!gameRunning) {
    gameRunning = true;
    createRandomAliens();
    main();
  }
});

// Menangani klik tombol "Reset"
document.getElementById('resetButton').addEventListener('click', function() {
  score = 0;
  carX = 10;
  carY = 120;
  createRandomAliens();
  ctx.clearRect(0, 0, cvs.width, cvs.height);
  gameRunning = false;
});

// Menangani klik tombol "Pause"
document.getElementById('pauseButton').addEventListener('click', function() {
  if (gameRunning) { // Hanya menjalankan fungsi pause jika permainan sedang berlangsung
    gameRunning = false; // Menetapkan gameRunning ke false untuk menjeda permainan
  } else { // Jika permainan sedang dijeda
    gameRunning = true; // Menetapkan gameRunning ke true untuk melanjutkan permainan
    main(); // Melanjutkan permainan
  }
});


// Inisialisasi permainan
createRandomAliens();
main();