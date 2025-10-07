import { SCHOOL_SPACING, MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM, schools } from './constants/perkTree'
import SchoolIcon from './components/SchoolIcon'
import { PerkTreeConnections } from './components/PerkTreeConnections'
import { PerkTreeNodes } from './components/PerkTreeNodes'
import { PerkTooltip } from './components/PerkTooltip'
import { usePerkTreeData } from './hooks/usePerkTreeData'
import { usePerkTreeInteractions } from './hooks/usePerkTreeInteractions'




function UnifiedPerkTreeViewer() {
    const schoolTrees = usePerkTreeData(schools)
    const {
        hoveredNode,
        setHoveredNode,
        mousePosition,
        viewport,
        setViewport,
        isDragging,
        containerRef,
        handleMouseDown,
        handleMouseMove,
        handleMouseUp,
        handleNodeClick
    } = usePerkTreeInteractions()


    if (schoolTrees.length === 0) {
        return (
            <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                height: '100vh',
                color: 'white'
            }}>
                <div>Loading all perk trees...</div>
            </div>
        )
    }

    return (
        <div className={`perk-tree-container ${isDragging ? 'dragging' : ''}`}>
            {/* Background */}
            <div className="perk-tree-background"></div>
            
            {/* Header */}
            <div style={{ position: 'absolute', top: '16px', left: '16px', zIndex: 10 }}>
                <div className="zoom-display">
                    Ars Affinity - Perk Trees
                </div>
            </div>

            {/* Zoom controls */}
            <div className="zoom-controls">
                <button
                    onClick={() => {
                        const newZoom = Math.min(MAX_ZOOM, viewport.zoom + 0.2)
                        if (newZoom === viewport.zoom) return
                        
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2
                        const screenCenterY = screenHeight / 2
                        
                        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
                        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
                        
                        const newX = screenCenterX - svgCenterX * newZoom
                        const newY = screenCenterY - svgCenterY * newZoom
                        
                        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
                    }}
                    className="zoom-button"
                >
                    +
                </button>
                <button
                    onClick={() => {
                        const newZoom = Math.max(MIN_ZOOM, viewport.zoom - 0.2)
                        if (newZoom === viewport.zoom) return
                        
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2
                        const screenCenterY = screenHeight / 2
                        
                        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
                        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
                        
                        const newX = screenCenterX - svgCenterX * newZoom
                        const newY = screenCenterY - svgCenterY * newZoom
                        
                        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
                    }}
                    className="zoom-button"
                >
                    -
                </button>
                <button
                    onClick={() => {
                        const schoolCount = schoolTrees.length
                        const cols = Math.min(4, schoolCount)
                        const rows = Math.ceil(schoolCount / 4)
                        const contentCenterX = -((cols - 1) * SCHOOL_SPACING) / 2
                        const contentCenterY = -((rows - 1) * SCHOOL_SPACING) / 2
                        
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2 / viewport.zoom
                        const screenCenterY = screenHeight / 2 / viewport.zoom
                        
                        const centerX = screenCenterX - contentCenterX
                        const centerY = screenCenterY - contentCenterY
                        
                        setViewport(prev => ({ ...prev, x: centerX, y: centerY }))
                    }}
                    className="zoom-button"
                >
                    Center
                </button>
                <div className="zoom-display">
                    {Math.round((viewport.zoom / DEFAULT_ZOOM) * 100)}%
                </div>
            </div>

            {/* Main viewport */}
            <div
                ref={containerRef}
                style={{ 
                    width: '100%', 
                    height: '100%',
                }}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
                onMouseEnter={handleMouseMove}
            >
                <svg
                    className="perk-tree-svg"
                    width="100%"
                    height="100%"
                    viewBox="-2000 -2000 4000 4000"
                    onMouseDown={handleMouseDown}
                    onMouseMove={handleMouseMove}
                    onMouseUp={handleMouseUp}
                    onMouseLeave={handleMouseUp}
                >
                    <g transform={`translate(${viewport.x / viewport.zoom}, ${viewport.y / viewport.zoom}) scale(${viewport.zoom})`}>
                    {/* Grid background and gradients */}
                    <defs>
                        <pattern id="grid" width="200" height="200" patternUnits="userSpaceOnUse">
                            <path d="M 200 0 L 0 0 0 200" fill="none" stroke="#374151" strokeWidth="1" opacity="0.1"/>
                        </pattern>
                        
                        {/* Necromancy (Anima) gradient */}
                        <linearGradient id="necromancy-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#E3E3E3"/>
                            <stop offset="100%" stopColor="#E3E3E3"/>
                        </linearGradient>
                        
                        {/* Necromancy (Anima) hover gradient */}
                        <linearGradient id="necromancy-gradient-hover" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#E3E3E3"/>
                            <stop offset="100%" stopColor="#E3E3E3"/>
                        </linearGradient>
                    </defs>
                    <rect x="-2000" y="-2000" width="4000" height="4000" fill="url(#grid)" />

                    {/* School icons */}
                    {schoolTrees.map(schoolTree => (
                        <SchoolIcon
                            key={`icon-${schoolTree.school}`}
                            school={schoolTree.school}
                            centerX={0}
                            centerY={0}
                            angle={schoolTree.angle}
                        />
                    ))}

                    {/* School trees - positioned at global coordinates */}
                    {schoolTrees.map(schoolTree => (
                        <g key={`tree-${schoolTree.school}`}>
                            {/* Connections */}
                            <PerkTreeConnections schoolTree={schoolTree} />

                            {/* Nodes */}
                            <PerkTreeNodes 
                                schoolTree={schoolTree}
                                hoveredNode={hoveredNode}
                                setHoveredNode={setHoveredNode}
                                handleNodeClick={handleNodeClick}
                            />
                        </g>
                    ))}
                    </g>
                </svg>
            </div>

            {/* Tooltip */}
            <PerkTooltip 
                hoveredNode={hoveredNode}
                mousePosition={mousePosition}
            />
        </div>
    )
}

export default UnifiedPerkTreeViewer
