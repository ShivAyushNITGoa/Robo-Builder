package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Achievement
import com.example.ui.GameViewModel
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    achGrid: List<Achievement>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ACCOLADES & BADGES",
            color = CyberWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(achGrid) { ach ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSteel),
                    border = BorderStroke(1.dp, if (ach.unlocked) CyberGold.copy(alpha = 0.5f) else CyberIron),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Badge Hexagon icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (ach.unlocked) CyberGold.copy(alpha = 0.15f) else CyberIron),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (ach.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = "Badge",
                                tint = if (ach.unlocked) CyberGold else CyberGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Info & Progress bar
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                color = if (ach.unlocked) CyberGold else CyberWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = ach.description,
                                color = CyberGray,
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom progress bar
                            val ratio = ach.currentProgress.toFloat() / ach.targetProgress.coerceAtLeast(1)
                            LinearProgressIndicator(
                                progress = { ratio },
                                color = if (ach.unlocked) CyberGold else CyberBlue,
                                trackColor = CyberIron,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${ach.currentProgress}/${ach.targetProgress}",
                                    color = CyberGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (ach.unlocked) "UNLOCKED (+${ach.rewardCoins}🪙)" else "REWARD: ${ach.rewardCoins}🪙",
                                    color = if (ach.unlocked) CyberLime else CyberGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
