# 🎲 Line of Three

A modern Android strategy board game built with **Jetpack Compose**, **Material 3**, and pure Kotlin state management.

---

## 📌 Overview

**Line of Three** is a two-player turn-based strategy board game inspired by traditional alignment and mill games (such as Nine Men's Morris and Three Men's Morris variations). Players alternate placing and sliding pieces across a custom 16-node board grid to form lines of three ("mills"). Completing a line grants the ability to strike and remove an opponent's piece from the board.

---

## ✨ Features

- 🧠 **Pure Tactical Gameplay**: Zero luck— victory relies entirely on positioning, strategy, and foresight.
- 🎨 **Jetpack Compose Canvas Graphics**: Custom vector drawing with smooth selection highlights and node state animations.
- 🌓 **Flexible Themes**: Complete support for Light Mode, Dark Mode, System Default, and custom Board Styles (*Classic* & *Midnight*).
- 🔊 **Rich Audio & Haptic Feedback**: Tactile vibration patterns and sound effects for piece placement, moves, line completions, and captures.
- 📱 **Adaptive & Responsive Layout**: Tailored support for portrait and landscape orientations across phones and tablets.
- 📊 **Game Statistics**: Tracks games played, Player 1 wins, Player 2 wins, and maximum lines formed using **Jetpack DataStore Preferences**.
- 💡 **Help & Assistance**: Interactive rules guide with visual board diagrams and optional legal move highlights.

---

## 🎮 How to Play

### 1. Board & Pieces
- **The Board**: A 16-node grid arranged in an outer square and inner square joined by 8 connecting spokes.
- **The Players**:
  - **Player 1**: Copper · Diamond Token
  - **Player 2**: Teal · Ring Token
- **Starting Setup**: Each player starts with **6 pieces**.

### 2. Game Phases

#### **Phase 1: Placement**
- Players take turns placing one piece onto any empty board intersection.
- Forming a line of three during placement allows you to remove one opponent piece.

#### **Phase 2: Movement**
- Once all pieces are placed on the board, players take turns moving one piece along a connected segment to an adjacent empty node.
- No jumping over pieces is allowed.

### 3. Forming Lines & Capturing
- **Line of Three**: Aligning 3 of your pieces along a straight line segment forms a line.
- **Capturing**: When a line is completed, you select and remove one opponent piece.
- **Protection Rule**: Opponent pieces inside completed lines are protected from removal unless *all* of their pieces on the board are part of completed lines.
- **Re-activating Lines**: A line that already exists does not grant removal rights repeatedly. You must break the line by moving a piece away, then reform it on a subsequent turn to capture again.

### 4. Winning the Game
A player wins when:
1. The opponent is reduced to **fewer than 3 pieces** (making it impossible to form a line).
2. The opponent is **completely blocked** and has no legal moves remaining on their turn.

---

## 🛠️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **Architecture**: Clean Architecture / Unidirectional Data Flow (MVI)
  - `GameEngine`: Pure, immutable state transformation logic with zero side effects.
  - `LineOfThreeState`: Complete snapshot model representing board nodes, phases, selections, and win conditions.
  - `GameViewModel` & `AppViewModel`: Manage UI state and handle player interactions via coroutine flows.
- **Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore) for persistent settings and game statistics.
- **Audio & Haptics**: Custom Android `SoundPool` manager and system `Vibrator` feedback pipeline.

---

## 📁 Project Structure

```
LineofThree/
├── app/src/main/java/com/example/lineofthree/
│   ├── data/                 # Data models, AppSettings, SettingsRepository (DataStore)
│   ├── feedback/             # Audio effects and Haptic feedback controller
│   ├── game/                 # Pure GameEngine, BoardRules, and LineOfThreeState models
│   ├── ui/
│   │   ├── board/            # Custom Canvas drawing for Game Board & preview diagrams
│   │   ├── components/       # Reusable Compose buttons, cards, and tokens
│   │   ├── screens/          # HomeScreen, GameScreen, HowToPlayScreen, SettingsScreen, ResultScreen
│   │   └── theme/            # Color palettes, typography, and Material 3 theme definitions
│   └── MainActivity.kt       # Application entry point and navigation setup
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Ladybug (2024.2.1) or newer
- **JDK**: 17
- **Android SDK**: Min SDK 24 (Android 7.0) / Target SDK 35

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Line-of-Three.git
   cd Line-of-Three
   ```

2. Open the project in **Android Studio**.

3. Build and run on an emulator or connected Android device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
