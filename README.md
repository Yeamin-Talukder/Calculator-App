# Calculator

A beautifully designed, feature-rich Android calculator application built natively with Kotlin. 

This Calculator goes beyond simple arithmetic, offering a comprehensive suite of tools including a gesture-activated scientific panel, calculation history, an Age Calculator, and a BMI Calculator—all wrapped in a highly polished, edge-to-edge modern user interface.

## 🚀 Tech Stack

- **Platform:** Android
- **Language:** Kotlin
- **UI Framework:** Android View System (XML)
- **Build System:** Gradle (Kotlin DSL)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## ✨ Key Features

### 1. Smart UI & Navigation
* **Edge-to-Edge Display:** Utilizes the entire screen beautifully, dynamically drawing beneath the system status and navigation bars.
* **Day/Night Theme Support:** 
  * *Dark Mode:* Deep blacks with high-contrast, vibrant accents for comfortable nighttime viewing.
  * *Light Mode:* A modern, premium "pastel glass" aesthetic with soft off-white backgrounds and vivid button colors.
* **Haptic Feedback:** Native tactile vibrations on key presses (toggleable in Settings).
* **Gesture Controls:** Simply swipe **Up** or **Down** on the number pad to reveal or hide the advanced scientific panel.

### 2. Powerful Calculation Tools
* **Basic Arithmetic:** Addition, subtraction, multiplication, division.
* **Scientific Panel:** 
  * Trigonometry (`sin`, `cos`, `tan`)
  * Logarithms (`log`, `ln`)
  * Exponents & Roots (`^`, `x²`, `√`)
  * Constants (`π`, `e`)
* **Advanced Mathematical Functions:**
  * **GCD & LCM:** Calculate Greatest Common Divisor and Least Common Multiple of two numbers.
  * **PRIME:** Check if a number is a prime number.
  * **BIN:** Instantly convert decimal numbers to binary.
  * **DEG:** Convert radians to degrees.
  * **MOD:** Modulo operation.
  * **Factorial (`n!`):** Compute the factorial of an integer.

### 3. History Tape
* **Live Tracking:** Every equals (`=`) press logs the expression and result.
* **Scrollable List:** Smooth, scrollbar-free interface that automatically tracks to the newest entry.
* **Smart Clear:** Clear the entire history with a single tap (includes a safety confirmation dialog to prevent accidental deletion).

### 4. Extra Tools
Accessible via the hamburger menu:
* **Age Calculator:** Input your date of birth to instantly receive your exact age in years, months, and days.
* **BMI Calculator:** Input your weight (kg) and height (cm) to calculate your Body Mass Index, complete with health classifications (Underweight, Normal, Overweight, Obese).

## 🛠️ Building the App

1. Clone or download the repository.
2. Open the project in **Android Studio**.
3. Allow Gradle to sync and download the required dependencies.
4. Run the app on an Android Emulator or a physical device connected via USB/Wireless Debugging.

```bash
# To build an APK from the command line:
./gradlew assembleDebug
```

## 👤 Developer Information

Developed by **MD YEAMIN TALUKDER**.
Visit my GitHub Profile: [Yeamin-Talukder](https://github.com/Yeamin-Talukder)
