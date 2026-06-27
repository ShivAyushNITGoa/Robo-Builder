package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@Composable
fun DailyChallengeScreen(
    viewModel: GameViewModel,
    profile: UserProfile?
) {
    // Generate a sandbox challenge
    var solved by remember { mutableStateOf(false) }

    val challengeLegs = listOf("Jetpack", "Spider Legs")
    val challengeArms = listOf("Hammer", "Welding Torch")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DAILY SANDBOX TESTPAD",
            color = CyberWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSteel),
            border = BorderStroke(1.5.dp, CyberBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ACTIVE CODE: #99A1C", color = CyberBlue, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("REWARD: 100 🪙", color = CyberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Lava Canyon Demolition",
                    color = CyberWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "A double hazard puzzle! Cross a boiling magma river AND break down a laser thermal brick. Standard wheels melt; grabbers won't drill.",
                    color = CyberGray,
                    fontSize = 13.sp
                )
            }
        }

        // Assembler buttons inside sandbox
        var selectedSandboxLegs by remember { mutableStateOf("Empty") }
        var selectedSandboxArm by remember { mutableStateOf("Empty") }

        Text("SANDBOX BAY CHASSIS", color = CyberGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Legs selection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSteel)
                    .border(1.dp, CyberBlue)
                    .clickable {
                        selectedSandboxLegs = if (selectedSandboxLegs == "Jetpack") "Spider Legs" else "Jetpack"
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = "Legs", tint = CyberBlue)
                    Text("MOBILITY", color = CyberGray, fontSize = 11.sp)
                    Text(selectedSandboxLegs, color = CyberWhite, fontWeight = FontWeight.Bold)
                }
            }

            // Arm selection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSteel)
                    .border(1.dp, CyberOrange)
                    .clickable {
                        selectedSandboxArm = if (selectedSandboxArm == "Hammer") "Welding Torch" else "Hammer"
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Hardware, contentDescription = "Arms", tint = CyberOrange)
                    Text("TOOLING", color = CyberGray, fontSize = 11.sp)
                    Text(selectedSandboxArm, color = CyberWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedSandboxLegs == "Jetpack" && selectedSandboxArm == "Hammer") {
                    solved = true
                    viewModel.claimDailyBonus() // Claims 100 coins total
                } else {
                    solved = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberLime),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("TRIGGER DIAGNOSTIC RUN", color = CyberObsidian, fontWeight = FontWeight.Bold)
        }

        if (solved) {
            Text(
                text = "🎉 SUCCESS! Combination matches sandbox diagnostics! +100 Coins credited to database.",
                color = CyberLime,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        } else if (selectedSandboxLegs != "Empty" && selectedSandboxArm != "Empty") {
            Text(
                text = "❌ FAILURE! Wrong combination! The robot melted in the lava core or bounced off the wall.",
                color = CyberRed,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
