# Ars Affinity Documentation

This is a simple React application for displaying and testing Ars Affinity assets.

## Development

To start the development server:

```bash
npm install
npm run dev
```

The app will be available at `http://localhost:5000`

## Features

- Displays the `affinity_bg.png` image at 2x size with pixel-perfect scaling
- Uses relative paths to reference assets from the main mod directory
- Clean, minimal interface for testing visual assets

## Image Rendering

The app uses CSS properties to ensure pixel-perfect scaling:
- `image-rendering: pixelated` for crisp pixel art
- `transform: scale(2)` for 2x magnification
- `transform-origin: center` for centered scaling
