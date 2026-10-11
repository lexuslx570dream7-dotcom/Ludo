package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tournament
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

@Composable
fun TournamentScreen(
    tournaments: List<Tournament>,
    onJoinTournament: (Tournament) -> Unit,
    onCreateTournament: (name: String, entry: Long, prize: Long, maxPlayers: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF030D18))
                )
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("🏆 Tournaments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("admin_create_tournament_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Host", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tournaments) { tournament ->
                TournamentCardItem(
                    tournament = tournament,
                    onJoin = { onJoinTournament(tournament) }
                )
            }
        }
    }

    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("Championship Cup") }
        var entryInput by remember { mutableStateOf("1000") }
        var prizeInput by remember { mutableStateOf("8000") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Host Tournament (Admin)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Tournament Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = entryInput,
                        onValueChange = { entryInput = it },
                        label = { Text("Entry Fee (Coins)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = prizeInput,
                        onValueChange = { prizeInput = it },
                        label = { Text("Prize Pool (Coins)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entry = entryInput.toLongOrNull() ?: 1000L
                        val prize = prizeInput.toLongOrNull() ?: 8000L
                        onCreateTournament(nameInput, entry, prize, 16)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Publish", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFF90A4AE))
                }
            },
            containerColor = Color(0xFF0D253B)
        )
    }
}

@Composable
private fun TournamentCardItem(tournament: Tournament, onJoin: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tournament.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .background(
                            if (tournament.status == "LIVE") Color(0xFFD32F2F) else Color(0xFF00C853),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(tournament.status, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Prize Pool", color = Color(0xFF90A4AE), fontSize = 11.sp)
                    Text("${tournament.prizePoolCoins} 🪙", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Column {
                    Text("Entry Fee", color = Color(0xFF90A4AE), fontSize = 11.sp)
                    Text("${tournament.entryFeeCoins} 🪙", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text("Players", color = Color(0xFF90A4AE), fontSize = 11.sp)
                    Text("${tournament.currentPlayers}/${tournament.maxPlayers}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                modifier = Modifier.fillMaxWidth().testTag("join_tournament_${tournament.id}"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("JOIN TOURNAMENT (${tournament.startTime})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
