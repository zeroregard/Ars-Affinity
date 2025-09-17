import React from 'react'
import { ViewportState, SchoolTreeData } from '../types/perkTree'
import { MAX_ZOOM, MIN_ZOOM, DEFAULT_ZOOM, SCHOOL_SPACING } from '../constants/perkTree'

interface ZoomControlsProps {
    viewport: ViewportState
    setViewport: React.Dispatch<React.SetStateAction<ViewportState>>
    schoolTrees: SchoolTreeData[]
}

const ZoomControls: React.FC<ZoomControlsProps> = ({ viewport, setViewport, schoolTrees }) => {
    const handleZoomIn = () => {
        const newZoom = Math.min(MAX_ZOOM, viewport.zoom + 0.2)
        if (newZoom === viewport.zoom) return
        
        // Zoom towards screen center
        const screenWidth = window.innerWidth
        const screenHeight = window.innerHeight
        const screenCenterX = screenWidth / 2
        const screenCenterY = screenHeight / 2
        
        // Convert screen center to SVG coordinates
        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
        
        // Adjust viewport to zoom towards screen center
        const newX = screenCenterX - svgCenterX * newZoom
        const newY = screenCenterY - svgCenterY * newZoom
        
        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
    }

    const handleZoomOut = () => {
        const newZoom = Math.max(MIN_ZOOM, viewport.zoom - 0.2)
        if (newZoom === viewport.zoom) return
        
        // Zoom towards screen center
        const screenWidth = window.innerWidth
        const screenHeight = window.innerHeight
        const screenCenterX = screenWidth / 2
        const screenCenterY = screenHeight / 2
        
        // Convert screen center to SVG coordinates
        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
        
        // Adjust viewport to zoom towards screen center
        const newX = screenCenterX - svgCenterX * newZoom
        const newY = screenCenterY - svgCenterY * newZoom
        
        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
    }

    const handleCenter = () => {
        const schoolCount = schoolTrees.length
        const cols = Math.min(4, schoolCount)
        const rows = Math.ceil(schoolCount / 4)
        const contentCenterX = -((cols - 1) * SCHOOL_SPACING) / 2
        const contentCenterY = -((rows - 1) * SCHOOL_SPACING) / 2
        
        // To center content on screen: translate so content center appears at screen center
        const screenWidth = window.innerWidth
        const screenHeight = window.innerHeight
        const screenCenterX = screenWidth / 2 / viewport.zoom
        const screenCenterY = screenHeight / 2 / viewport.zoom
        
        // Translate to put content center at screen center
        const centerX = screenCenterX - contentCenterX
        const centerY = screenCenterY - contentCenterY
        
        setViewport(prev => ({ ...prev, x: centerX, y: centerY }))
    }

    return (
        <div className="zoom-controls">
            <button onClick={handleZoomIn} className="zoom-button">
                +
            </button>
            <button onClick={handleZoomOut} className="zoom-button">
                -
            </button>
            <button onClick={handleCenter} className="zoom-button">
                Center
            </button>
            <div className="zoom-display">
                {Math.round((viewport.zoom / DEFAULT_ZOOM) * 100)}%
            </div>
        </div>
    )
}

export default ZoomControls
