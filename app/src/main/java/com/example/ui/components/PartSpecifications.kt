package com.example.ui.components

object PartSpecifications {
    fun getPartSpecDescription(part: String): String {
        return when (part) {
            "Wheels" -> "Fast rolling speed. Requires flat horizontal floors. Melts in lava/quicksand."
            "Tank Tracks" -> "Heaviest continuous grip treads. Dominates slippery ice slope and snowdrifts."
            "Spider Legs" -> "Articulated crawling pivots. Moves across quicksand dunes and uneven terrain."
            "Hover Engine" -> "Levitates chassis. Glides over rivers, boiling lava streams, and pits easily."
            "Jump Springs" -> "High kinetic propulsion coil. Bounce jumps over deep canyon gaps."
            "Jetpack" -> "Full rocket thruster bypass. Flies high over hazardous obstacles and eruptions."
            
            "Grabber" -> "Mechanical grasp claw. Essential to lift metal boxes, carry payloads, or rescue units."
            "Magnet" -> "Electromagnetic coil. Pulls gold coin gears or iron boxes from thorny ditches."
            "Hammer" -> "Dense impact hammer mallet. Breaks thick concrete walls and barrier pillars."
            "Drill" -> "Rotating core boring bit. Smashes bedrock barriers and tunnels through snow."
            "Welding Torch" -> "Superheated solder torch. Seals piping reactors and melts glaciers."
            "Shield Arm" -> "Energy deflection plate. Blocks solar security laser cutters safely."
            
            "Battery Pack" -> "Adds extra auxiliary duration cells for heavy mechanical parts."
            "Turbo Battery" -> "Overclocks motor frequency. Multiplies system traction and velocities."
            "Cooling System" -> "Cryo fluid heat-sink loops. Essential to survive lava reactor repairs."
            "Object Detector" -> "Thermal sonar scanner. Tracks targets hidden by sandstorms and mist."
            "Heat Sensor" -> "Detects reactor spikes and lava traps to auto calculate navigation vectors."
            
            "Empty" -> "Standard structural placeholder block. Zero battery drain."
            else -> "Robotic component socket."
        }
    }
}
