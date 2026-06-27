package com.example.data

data class LevelDefinition(
    val id: Int,
    val worldId: Int,
    val levelNumber: Int,
    val title: String,
    val worldName: String,
    val missionType: String, // "Reach Goal", "Carry Box", "Break Wall", "Cross River", "Cross Lava", "Collect Coins", "Avoid Lasers", "Rescue Animals"
    val description: String,
    val hazardType: String, // "lava", "river", "laser", "ice", "quicksand", "wall", "gap", "none"
    val promptText: String,
    val acceptedLegs: List<String>,
    val acceptedArms: List<String>,
    val acceptedUtilities: List<String>,
    val perfectLegs: String,
    val perfectArm: String,
    val successMessage: String,
    val failureMessages: Map<String, String> // Failure modes based on wrong parts
)

object LevelDefinitions {
    val worlds = listOf(
        "Factory" to "#E0E0E0",
        "Forest" to "#4CAF50",
        "Desert" to "#FFC107",
        "Snow" to "#00BCD4",
        "Volcano" to "#F44336",
        "Deep Space" to "#9C27B0",
        "Cyber City" to "#FF007F",
        "Ocean Depths" to "#3F51B5"
    )

    val levels = listOf(
        // World 1: Factory
        LevelDefinition(
            id = 1, worldId = 1, levelNumber = 1,
            title = "Simple Bridge Crossing",
            worldName = "Factory",
            missionType = "Reach Goal",
            description = "Travel across the flat factory floor to reach the exit gate.",
            hazardType = "none",
            promptText = "Assemble basic mobility parts to drive across the factory.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine", "Jump Springs", "Jetpack"),
            acceptedArms = listOf("Empty", "Grabber", "Magnet", "Hammer", "Drill", "Welding Torch", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Battery Pack", "Turbo Battery", "Cooling System", "Object Detector", "Heat Sensor"),
            perfectLegs = "Wheels",
            perfectArm = "Empty",
            successMessage = "Optimal setup! Your robot rolled smoothly across the factory floor and entered the gate!",
            failureMessages = mapOf(
                "no_legs" to "Without any wheels or legs, your robot sits on the floor, twitching its arms helplessly and shutting down.",
                "battery_empty" to "The heavy configuration drained the battery before reaching the gate!"
            )
        ),
        LevelDefinition(
            id = 2, worldId = 1, levelNumber = 2,
            title = "Crate Transporter",
            worldName = "Factory",
            missionType = "Carry Box",
            description = "Pick up the heavy lithium microchip box and carry it to the conveyor slot.",
            hazardType = "box",
            promptText = "You need a way to pick up the crate and wheels to move it.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Grabber", "Magnet"),
            acceptedUtilities = listOf("Empty", "Battery Pack", "Turbo Battery"),
            perfectLegs = "Wheels",
            perfectArm = "Grabber",
            successMessage = "Perfect pick! Your grabber lifted the box securely and carried it over to the receiver slot!",
            failureMessages = mapOf(
                "no_arms" to "Your robot drives up to the crate, stares at it blankly, scratches its head, and gets lost.",
                "wrong_arms" to "You tried to use a weapon! The tool smashes/melts the fragile box, creating a shower of sparks!",
                "quicksand" to "The robot sinks!"
            )
        ),
        LevelDefinition(
            id = 3, worldId = 1, levelNumber = 3,
            title = "Breaker of Walls",
            worldName = "Factory",
            missionType = "Break Wall",
            description = "A massive concrete pillar blocks the main server gateway. Knock it down!",
            hazardType = "wall",
            promptText = "Use heavy impact tools to break down the masonry.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs"),
            acceptedArms = listOf("Hammer", "Drill"),
            acceptedUtilities = listOf("Empty", "Turbo Battery"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Hammer",
            successMessage = "KA-BOOM! The heavy Hammer shattered the pillar into gravel! The path is clear!",
            failureMessages = mapOf(
                "no_arms" to "Your robot drives face-first into the concrete wall, bounces off, and powers down in confusion.",
                "wrong_arms" to "Your gentle grabber/welding torch just scratches the surface. The robot overheats trying to push the wall!",
                "wrong_legs" to "Your legs are too weak! The recoil of the impact knocks the robot flat on its back!"
            )
        ),

        // World 2: Forest
        LevelDefinition(
            id = 4, worldId = 2, levelNumber = 1,
            title = "Deep River Crossing",
            worldName = "Forest",
            missionType = "Cross River",
            description = "A swift forest river is blocking the path. Standard wheels will sink and short-circuit.",
            hazardType = "river",
            promptText = "Equip floating, leaping, or flying mobility modules.",
            acceptedLegs = listOf("Hover Engine", "Jetpack", "Jump Springs"),
            acceptedArms = listOf("Empty", "Grabber", "Magnet", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Cooling System"),
            perfectLegs = "Hover Engine",
            perfectArm = "Empty",
            successMessage = "Spectacular! Your robot glides smoothly above the rushing rapids like a high-tech speedboat!",
            failureMessages = mapOf(
                "wrong_legs" to "SPLASH! The heavy wheels sink straight to the bottom of the river. Bubbles rise as the circuits short out!",
                "no_legs" to "The robot falls into the water and floats away downriver, waving goodbye."
            )
        ),
        LevelDefinition(
            id = 5, worldId = 2, levelNumber = 2,
            title = "Forest Scrap Grab",
            worldName = "Forest",
            missionType = "Collect Coins",
            description = "A magnetic coin is resting deep in a thorny pit. Retrieve it!",
            hazardType = "magnetic_coin",
            promptText = "Use electromagnetic grab tools or spider legs to avoid thorns.",
            acceptedLegs = listOf("Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Magnet", "Grabber"),
            acceptedUtilities = listOf("Empty", "Battery Pack"),
            perfectLegs = "Spider Legs",
            perfectArm = "Magnet",
            successMessage = "Swoosh! The powerful electromagnet pulled the coin right out of the brambles easily!",
            failureMessages = mapOf(
                "wrong_legs" to "The wheels pop on the sharp forest thorns! Your robot gets stuck and sparks fly.",
                "no_arms" to "The robot looks down at the coin in the pit, tries to reach with its chassis, and tumbles in face first!"
            )
        ),
        LevelDefinition(
            id = 6, worldId = 2, levelNumber = 3,
            title = "Kangaroo Leap",
            worldName = "Forest",
            missionType = "Reach Goal",
            description = "Cross a wide, deep wooden ditch. There are no bridges.",
            hazardType = "gap",
            promptText = "Equip a propulsion mechanism that can leap or fly.",
            acceptedLegs = listOf("Jump Springs", "Jetpack"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Turbo Battery"),
            perfectLegs = "Jump Springs",
            perfectArm = "Empty",
            successMessage = "BOING! The spring launches your robot into a high arc, landing perfectly on the other side!",
            failureMessages = mapOf(
                "wrong_legs" to "The robot drives straight into the ditch with a heavy thud, its wheels spinning in the dirt.",
                "overweight" to "The robot is too heavy! The spring compresses but fails to launch, leaving the robot stranded."
            )
        ),

        // World 3: Desert
        LevelDefinition(
            id = 7, worldId = 3, levelNumber = 1,
            title = "Sinking Quicksand",
            worldName = "Desert",
            missionType = "Reach Goal",
            description = "A vast sand dune field of wet quicksand. Rolling parts will sink immediately.",
            hazardType = "quicksand",
            promptText = "Choose legs that walk on top, or levitate above.",
            acceptedLegs = listOf("Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Cooling System"),
            perfectLegs = "Spider Legs",
            perfectArm = "Empty",
            successMessage = "Like a desert arachnid, your spider legs scurry effortlessly across the shifting sand!",
            failureMessages = mapOf(
                "wrong_legs" to "GLUB... GLUB... The heavy wheels/tracks sink slowly into the quicksand, leaving only the antenna visible.",
                "no_legs" to "The robot faceplants into the sand dune and gets swallowed by the desert."
            )
        ),
        LevelDefinition(
            id = 8, worldId = 3, levelNumber = 2,
            title = "Desert Laser Shield",
            worldName = "Desert",
            missionType = "Avoid Lasers",
            description = "A high-intensity solar laser beam cuts across the desert security path.",
            hazardType = "laser",
            promptText = "Equip a protective arm that can block energy beams.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Shield Arm"),
            acceptedUtilities = listOf("Empty", "Cooling System", "Shield Generator"),
            perfectLegs = "Hover Engine",
            perfectArm = "Shield Arm",
            successMessage = "FZZT! The heavy composite shield deflects the laser beam, allowing safe passage!",
            failureMessages = mapOf(
                "no_arms" to "BZZZZT! The laser strikes your robot's main core. The robot melts into a puddle of shiny liquid metal!",
                "wrong_arms" to "Your tool melts under the high-intensity laser! The robot catches fire and powers down."
            )
        ),
        LevelDefinition(
            id = 9, worldId = 3, levelNumber = 3,
            title = "Sandstorm Search",
            worldName = "Desert",
            missionType = "Collect Coins",
            description = "A blinding sandstorm obscures the golden coin. A blind robot cannot locate it.",
            hazardType = "sandstorm",
            promptText = "Equip a sensor that can locate items through thick dust.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs"),
            acceptedArms = listOf("Grabber", "Magnet"),
            acceptedUtilities = listOf("Object Detector", "Heat Sensor"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Magnet",
            successMessage = "BEEP! The Object Detector pinged the coin's coordinates perfectly through the sandstorm! Retrieved!",
            failureMessages = mapOf(
                "no_utility" to "Blinded by the dust storm, the robot drives in circles, eventually crashes into a cactus, and sparks out.",
                "wrong_utility" to "The robot can't find anything in the storm, gets lost in the dunes, and its battery dies."
            )
        ),

        // World 4: Snow
        LevelDefinition(
            id = 10, worldId = 4, levelNumber = 1,
            title = "Slippery Ice Track",
            worldName = "Snow",
            missionType = "Reach Goal",
            description = "An icy arctic track leads to the weather station. Wheels will slide uncontrollably.",
            hazardType = "ice",
            promptText = "Use heavy continuous treads or spider grip legs to traverse the frost.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Turbo Battery"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Empty",
            successMessage = "Awesome! The heavy rubber treads on your Tank Tracks grip the ice perfectly and roll right on!",
            failureMessages = mapOf(
                "wrong_legs" to "Whoops! The wheels spin fruitlessly on the ice. The robot slips, slides backwards, and spins off a cliff!",
                "no_legs" to "The robot freezes solid into an ice cube and slides down the hill."
            )
        ),
        LevelDefinition(
            id = 11, worldId = 4, levelNumber = 2,
            title = "Frozen Wall Melt",
            worldName = "Snow",
            missionType = "Break Wall",
            description = "A 10-foot-thick solid glacier wall blocks the entry to the mountain base.",
            hazardType = "glacier",
            promptText = "Equip a thermal arm to melt the frozen barrier.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Welding Torch", "Drill"),
            acceptedUtilities = listOf("Empty", "Turbo Battery"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Welding Torch",
            successMessage = "Sizzle! The thermal Welding Torch melts a perfect circle through the glacier. Welcome to the base!",
            failureMessages = mapOf(
                "no_arms" to "The robot taps on the ice wall, freezes its hand, and powers down with a cold shiver.",
                "wrong_arms" to "Your hammer hits the ice and bounces back, hitting the robot in the head and knocking it out!"
            )
        ),
        LevelDefinition(
            id = 12, worldId = 4, levelNumber = 3,
            title = "Polar Explorer",
            worldName = "Snow",
            missionType = "Reach Goal",
            description = "Slippery glaciers and deep snowdrifts stand in your way.",
            hazardType = "ice_and_snow",
            promptText = "Tank tracks or spider legs paired with heat sensors or high power.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Jetpack"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Empty", "Turbo Battery", "Cooling System"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Empty",
            successMessage = "Successful trek! Your robot bulldozed through the snow drifts and completed the polar expedition!",
            failureMessages = mapOf(
                "wrong_legs" to "The wheels get buried in deep snow. The motor burns out with a puff of dark smoke.",
                "no_legs" to "The robot sinks into a snow drift, leaving only its little hat showing."
            )
        ),

        // World 5: Volcano
        LevelDefinition(
            id = 13, worldId = 5, levelNumber = 1,
            title = "Lava Lake Overflight",
            worldName = "Volcano",
            missionType = "Cross Lava",
            description = "A boiling lake of liquid magma blocks the reactor core. Ground units will incinerate.",
            hazardType = "lava",
            promptText = "Choose vertical thrust or high levitation mobility parts.",
            acceptedLegs = listOf("Jetpack", "Hover Engine"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Cooling System", "Turbo Battery"),
            perfectLegs = "Jetpack",
            perfectArm = "Empty",
            successMessage = "Incredible! Your rocket jetpack carries the robot high above the flaming volcanic eruptions to victory!",
            failureMessages = mapOf(
                "wrong_legs" to "SZZT-MELT! The legs touch the lava. The robot turns into a bright orange glowing puddle of molten steel!",
                "no_legs" to "The robot slips off the ledge, falling directly into the lava. A single puff of smoke rises."
            )
        ),
        LevelDefinition(
            id = 14, worldId = 5, levelNumber = 2,
            title = "Magma Reactor Repair",
            worldName = "Volcano",
            missionType = "Repair Machine",
            description = "The volcanic core reactor is leaking. Float above magma vents and weld the central pipe.",
            hazardType = "lava_repair",
            promptText = "Need a Welding Torch, hover/flying mobility, and cooling protection.",
            acceptedLegs = listOf("Hover Engine", "Jetpack"),
            acceptedArms = listOf("Welding Torch"),
            acceptedUtilities = listOf("Cooling System"),
            perfectLegs = "Hover Engine",
            perfectArm = "Welding Torch",
            successMessage = "Masterful repair! The hover engines kept the robot stable while the torch sealed the volcanic fissure!",
            failureMessages = mapOf(
                "no_utility" to "The intense heat from the reactor vents melts the robot's logic board! It explodes in a shower of sparks!",
                "wrong_arms" to "Your robot drives up, hits the reactor with a hammer/grabber, and causes a massive volcanic eruption!",
                "wrong_legs" to "The wheels melt on the hot volcanic rock. The robot falls over, exploding in flames."
            )
        ),
        LevelDefinition(
            id = 15, worldId = 5, levelNumber = 3,
            title = "Volcanic Escape",
            worldName = "Volcano",
            missionType = "Reach Goal",
            description = "The volcano is erupting! Flying fireballs and collapsing structures block the exit.",
            hazardType = "eruption",
            promptText = "Assemble jetpack propulsion, shield arm, and cooling unit for maximum escape survivability.",
            acceptedLegs = listOf("Jetpack"),
            acceptedArms = listOf("Shield Arm"),
            acceptedUtilities = listOf("Cooling System"),
            perfectLegs = "Jetpack",
            perfectArm = "Shield Arm",
            successMessage = "Mission Complete! Your armored rocket-bot dodged falling lava stones, blocked fireballs, and escaped!",
            failureMessages = mapOf(
                "no_arms" to "A falling volcanic rock smashes directly into the unprotected robot, flattening it into a metal pancake!",
                "no_utility" to "The intense volcanic heat burns out the power cells. The robot shuts down right before the exit!",
                "wrong_legs" to "Too slow! The collapsing ceiling traps the robot in a fiery cage."
            )
        ),

        // World 6: Deep Space
        LevelDefinition(
            id = 16, worldId = 6, levelNumber = 1,
            title = "Asteroid Belt Clearance",
            worldName = "Deep Space",
            missionType = "Break Wall",
            description = "A massive floating meteor blocks the star track. Demolition tools are required to clear the cosmic lane.",
            hazardType = "asteroid",
            promptText = "Equip strong mechanical arms (Drills or Welding Torches) and zero-G stabilizers.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine", "Jetpack", "Jump Springs"),
            acceptedArms = listOf("Drill", "Welding Torch", "Hammer"),
            acceptedUtilities = listOf("Empty", "Battery Pack", "Turbo Battery"),
            perfectLegs = "Jetpack",
            perfectArm = "Drill",
            successMessage = "Outstanding! Your diamond drill pulverized the asteroid into stardust, opening up a safe interstellar pathway!",
            failureMessages = mapOf(
                "wrong_arms" to "Your robot tried to grab or weld the giant space rock, but got crushed when it drifted into the hull!",
                "no_arms" to "Without demolition tools, your robot crashed directly into the space boulder and shattered into debris!"
            )
        ),
        LevelDefinition(
            id = 17, worldId = 6, levelNumber = 2,
            title = "Cosmic Radiation Storm",
            worldName = "Deep Space",
            missionType = "Avoid Lasers",
            description = "Severe stellar flares are radiating the navigation corridor. Equip high-grade shielding and sensor cells.",
            hazardType = "cosmic_radiation",
            promptText = "Use composite shields and heat sensors to navigate the high energy radiation zone safely.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Shield Arm"),
            acceptedUtilities = listOf("Heat Sensor", "Cooling System"),
            perfectLegs = "Hover Engine",
            perfectArm = "Shield Arm",
            successMessage = "Radiation deflected! Your heavy composite shields and sensor arrays mapped a safe bypass through the stellar flare!",
            failureMessages = mapOf(
                "no_arms" to "The intense cosmic flare fried your main CPU, leaving your robot floating inertly in the cosmic void.",
                "wrong_arms" to "Your mechanical grabbers offered zero shielding! The cosmic rays vaporized your primary circuits."
            )
        ),
        LevelDefinition(
            id = 18, worldId = 6, levelNumber = 3,
            title = "Zero-G Void Leap",
            worldName = "Deep Space",
            missionType = "Reach Goal",
            description = "A complete gravitational vacuum segment requires powerful jetpack propulsion and stabilizer batteries.",
            hazardType = "zero_g",
            promptText = "Use Jetpacks or Hover Engines to float across the deep gap, along with reliable backup batteries.",
            acceptedLegs = listOf("Jetpack", "Hover Engine"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Battery Pack", "Turbo Battery", "Empty"),
            perfectLegs = "Jetpack",
            perfectArm = "Empty",
            successMessage = "Excellent! Your thrusters kept the chassis stable in zero gravity, docking perfectly at the space station!",
            failureMessages = mapOf(
                "wrong_legs" to "Standard walking units have no grip in zero gravity! The robot floated away into deep space.",
                "no_legs" to "The robot tumbled off the platform, drifting helplessly among the stars."
            )
        ),

        // World 7: Cyber City
        LevelDefinition(
            id = 19, worldId = 7, levelNumber = 1,
            title = "Grid Lock Override",
            worldName = "Cyber City",
            missionType = "Avoid Lasers",
            description = "An high-voltage pink laser grid blocks the digital downtown core. We need a shield to pass through safely.",
            hazardType = "cyber_grid",
            promptText = "Equip defensive shield arms to reflect the scanning laser pulses.",
            acceptedLegs = listOf("Wheels", "Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Shield Arm"),
            acceptedUtilities = listOf("Object Detector", "Battery Pack", "Empty"),
            perfectLegs = "Wheels",
            perfectArm = "Shield Arm",
            successMessage = "Grid breached! Your shields absorbed the laser frequency, allowing clean passage into the mainframe!",
            failureMessages = mapOf(
                "no_arms" to "SZZZT! The pink security beam vaporized the chassis, triggering an emergency security lock.",
                "wrong_arms" to "The laser fried your tools instantly! The robot was disabled by the mainframe security systems."
            )
        ),
        LevelDefinition(
            id = 20, worldId = 7, levelNumber = 2,
            title = "Neon EMP Pulse Tower",
            worldName = "Cyber City",
            missionType = "Reach Goal",
            description = "An active EMP emitter is discharging electrical waves. High-power batteries are required to withstand the surge.",
            hazardType = "neon_emp",
            promptText = "Combine heavy-duty locomotion with high-capacity Turbo Batteries.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine", "Jetpack"),
            acceptedArms = listOf("Shield Arm", "Empty"),
            acceptedUtilities = listOf("Turbo Battery", "Battery Pack"),
            perfectLegs = "Spider Legs",
            perfectArm = "Shield Arm",
            successMessage = "Success! The auxiliary high-capacity cells absorbed the EMP surge and powered through the discharge zone!",
            failureMessages = mapOf(
                "no_utility" to "The EMP wave drained your circuits completely, leaving the robot fried and unresponsive in the neon alley.",
                "wrong_legs" to "The sudden electromagnetic shock caused the wheels to spin out of control, crashing into a power substation."
            )
        ),
        LevelDefinition(
            id = 21, worldId = 7, levelNumber = 3,
            title = "Nanite Swarm Zone",
            worldName = "Cyber City",
            missionType = "Break Wall",
            description = "A swarm of tiny scrap-devouring nanites occupies the alley. Incinerate them using a welding torch.",
            hazardType = "nano_swarm",
            promptText = "Use thermal arms (Welding Torches) to clear the path, backed by protective cooling systems.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Welding Torch"),
            acceptedUtilities = listOf("Cooling System", "Turbo Battery", "Empty"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Welding Torch",
            successMessage = "Nanites incinerated! The high-intensity plasma torch created a heat dome, melting the nanites!",
            failureMessages = mapOf(
                "no_arms" to "The nanite swarm engulfed the robot, stripping it down to simple wire frames and screws!",
                "wrong_arms" to "The metal hammers/drills only hit empty air! The nanites consumed the chassis in seconds."
            )
        ),

        // World 8: Ocean Depths
        LevelDefinition(
            id = 22, worldId = 8, levelNumber = 1,
            title = "Abyssal Pressure Trench",
            worldName = "Ocean Depths",
            missionType = "Reach Goal",
            description = "Sinking down to the Mariana trench floor. The extreme hydrostatic pressure will crush a normal robot.",
            hazardType = "abyssal_pressure",
            promptText = "Equip sturdy Tank Tracks and proper battery systems to withstand the dark ocean depths.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs"),
            acceptedArms = listOf("Empty", "Shield Arm"),
            acceptedUtilities = listOf("Cooling System", "Battery Pack", "Empty"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Shield Arm",
            successMessage = "Pressure survived! The reinforced continuous treads and heavy frame handled the abyss beautifully!",
            failureMessages = mapOf(
                "wrong_legs" to "POP! Under the crushing ocean pressure, your lightweight engines collapsed like soda cans!",
                "no_legs" to "The robot imploded from the intense ocean pressure before reaching the sandy floor."
            )
        ),
        LevelDefinition(
            id = 23, worldId = 8, levelNumber = 2,
            title = "Turbulent Tide Currents",
            worldName = "Ocean Depths",
            missionType = "Cross River",
            description = "Swirling tidal currents will push light units away. Scurrying spider legs are required to anchor to the bed.",
            hazardType = "water_current",
            promptText = "Equip multi-jointed Spider Legs to crawl against the strong undercurrents.",
            acceptedLegs = listOf("Spider Legs"),
            acceptedArms = listOf("Empty", "Grabber", "Magnet"),
            acceptedUtilities = listOf("Empty", "Turbo Battery"),
            perfectLegs = "Spider Legs",
            perfectArm = "Empty",
            successMessage = "Currents bypassed! Your multi-jointed spider legs locked into the seabed rocks, moving steadily forward!",
            failureMessages = mapOf(
                "wrong_legs" to "Whoosh! The ocean currents swept the wheels off the ground, washing the robot away into the deep dark sea!",
                "no_legs" to "Without locomotion, the robot got spun around by the tide and dashed against the coral reef."
            )
        ),
        LevelDefinition(
            id = 24, worldId = 8, levelNumber = 3,
            title = "Electric Eel Nest",
            worldName = "Ocean Depths",
            missionType = "Avoid Lasers",
            description = "High-voltage bio-electric fields are discharging. Equip a protective shielding arm to ground the currents.",
            hazardType = "electric_eel",
            promptText = "Equip defensive Shields and proper sensory grids to block the underwater shocks.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Shield Arm"),
            acceptedUtilities = listOf("Cooling System", "Object Detector", "Empty"),
            perfectLegs = "Hover Engine",
            perfectArm = "Shield Arm",
            successMessage = "Bio-electricity grounded! Your shield insulated the main chassis from the 800-volt eel discharge!",
            failureMessages = mapOf(
                "no_arms" to "SHOCKING! The high-voltage electric discharge fried your robot's logic boards into toasted plastic!",
                "wrong_arms" to "Your metal tools acted as lightning rods, magnifying the electric arc and melting the main battery!"
            )
        ),
        LevelDefinition(
            id = 25, worldId = 8, levelNumber = 4,
            title = "Deep Sea Core Retrieve",
            worldName = "Ocean Depths",
            missionType = "Collect Coins",
            description = "Retrieve the lost submarine titanium core resting in a deep fissure. Strong magnet arms are recommended.",
            hazardType = "deep_sea_trench",
            promptText = "Equip a high-grade Magnet or Grabber arm along with continuous treads or spider legs.",
            acceptedLegs = listOf("Tank Tracks", "Spider Legs", "Hover Engine"),
            acceptedArms = listOf("Magnet", "Grabber"),
            acceptedUtilities = listOf("Object Detector", "Battery Pack", "Empty"),
            perfectLegs = "Tank Tracks",
            perfectArm = "Magnet",
            successMessage = "Retrieved! The electromagnet latched onto the core, and your tracks crawled out of the sandy trench safely!",
            failureMessages = mapOf(
                "no_arms" to "The robot can only look down at the heavy metal core, eventually running out of power in the dark.",
                "wrong_arms" to "The tool cannot lift the magnetic core! The heavy container slips back down, lost forever."
            )
        )
    )
}
