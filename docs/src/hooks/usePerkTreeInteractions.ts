import { useState, useRef, useCallback, useEffect } from 'react'
import { School, PerkNode, ViewportState } from '../types/perkTree'
import { MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM, ZOOM_SPEED } from '../constants/perkTree'

export const usePerkTreeInteractions = () => {
    const [hoveredNode, setHoveredNode] = useState<{ node: PerkNode, school: School } | null>(null)
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })
    const [viewport, setViewport] = useState<ViewportState>({ x: 0, y: 0, zoom: DEFAULT_ZOOM })
    const [isDragging, setIsDragging] = useState(false)
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
    const containerRef = useRef<HTMLDivElement>(null)

    // Handle mouse wheel zoom towards mouse cursor
    const handleWheel = useCallback((e: WheelEvent) => {
        e.preventDefault()
        const delta = e.deltaY > 0 ? -ZOOM_SPEED : ZOOM_SPEED
        const newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, viewport.zoom + delta))
        
        if (newZoom === viewport.zoom) return // No change in zoom
        
        // Get mouse position relative to the container
        const rect = containerRef.current?.getBoundingClientRect()
        if (!rect) return
        
        const mouseX = e.clientX - rect.left
        const mouseY = e.clientY - rect.top
        
        // Convert mouse position to SVG coordinates
        const svgMouseX = (mouseX - viewport.x) / viewport.zoom
        const svgMouseY = (mouseY - viewport.y) / viewport.zoom
        
        // Adjust viewport to zoom towards mouse position
        const newX = mouseX - svgMouseX * newZoom
        const newY = mouseY - svgMouseY * newZoom
        
        setViewport(prev => ({
            ...prev,
            x: newX,
            y: newY,
            zoom: newZoom
        }))
    }, [viewport])

    // Add wheel event listener with passive: false
    useEffect(() => {
        const container = containerRef.current
        if (!container) return

        container.addEventListener('wheel', handleWheel, { passive: false })
        
        return () => {
            container.removeEventListener('wheel', handleWheel)
        }
    }, [handleWheel])

    // Handle mouse down for dragging
    const handleMouseDown = useCallback((e: React.MouseEvent) => {
        if (e.button === 0) { // Left mouse button
            setIsDragging(true)
            setDragStart({ x: e.clientX, y: e.clientY })
            e.preventDefault()
        }
    }, [])

    // Handle mouse move for dragging
    const handleMouseMove = useCallback((e: React.MouseEvent) => {
        setMousePosition({ x: e.clientX, y: e.clientY })
        
        if (isDragging) {
            const dragSensitivity = 1.5 // Increase this value to make dragging more sensitive
            setViewport(prev => ({
                ...prev,
                x: (e.clientX - dragStart.x) * dragSensitivity,
                y: (e.clientY - dragStart.y) * dragSensitivity
            }))
            e.preventDefault()
        }
    }, [isDragging, dragStart])

    // Handle mouse up
    const handleMouseUp = useCallback(() => {
        setIsDragging(false)
    }, [])

    // Handle node click
    const handleNodeClick = useCallback((node: PerkNode, school: School) => {
        // TODO: Implement perk allocation/deallocation
    }, [])

    return {
        hoveredNode,
        setHoveredNode,
        mousePosition,
        viewport,
        setViewport,
        isDragging,
        dragStart,
        containerRef,
        handleWheel,
        handleMouseDown,
        handleMouseMove,
        handleMouseUp,
        handleNodeClick
    }
}
