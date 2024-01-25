# LZSS Compression/Decompression with GUI using JavaFX

## Overview

This project implements LZSS compression and decompression with a graphical user interface (GUI) using JavaFX. LZSS is a lossless data compression algorithm that replaces repeated occurrences of data with references to a single copy of that data.

## Table of Contents

- [Introduction](#lzss-compressiondecompression-with-gui-using-javafx)
- [Overview](#overview)
- [Table of Contents](#table-of-contents)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Features](#features)
- [Screenshots](#screenshots)
- [Contributors](#contributors)
- [License](#license)

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or later
- JavaFX

### Installation

1. Open the project in your favorite Java IDE.

2. Build and run the `Main` class to start the application.

## Usage

1. Launch the application.

2. Choose the compression or decompression tab.

3. For compression:
   - Select the input file.
   - Set compression parameters (Search Window Size, Look-Ahead Buffer Size, Min Match Size).
   - Click "Compress" to generate the compressed file.

4. For decompression:
   - Select the compressed file.
   - Click "Decompress" to generate the decompressed file.

## Features

- **LZSS Compression/Decompression:** Utilizes the LZSS algorithm to compress and decompress data.
- **GUI with JavaFX:** Provides a user-friendly graphical interface for ease of use.
- **Adjustable Compression Parameters:** Allows users to set parameters such as Search Window Size, Look-Ahead Buffer Size, and Min Match Size.

## Screenshots

Included in the repository Root.

## Contributors

- Rami Abo Rabia
- Itamar Abir

## License

This project is licensed under the [MIT License](LICENSE).
