package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.Friend
import com.example.model.FriendRequest
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

enum class SocialTab {
    FRIENDS,
    REQUESTS
}

@Composable
fun SocialScreen(
    friends: List<Friend>,
    friendRequests: List<FriendRequest>,
    selectedFriend: Friend?,
    chatMessages: List<ChatMessage>,
    isRecordingVoice: Boolean,
    recordingDuration: Int,
    onSelectFriend: (Friend) -> Unit,
    onCloseChat: () -> Unit,
    onSendMessage: (text: String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopVoiceRecord: (friendId: String) -> Unit,
    onSendImage: (friendId: String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onAddFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedFriend != null) {
        BackHandler { onCloseChat() }
        DirectChatView(
            friend = selectedFriend,
            messages = chatMessages,
            isRecordingVoice = isRecordingVoice,
            recordingDuration = recordingDuration,
            onClose = onCloseChat,
            onSendMessage = onSendMessage,
            onStartVoiceRecord = onStartVoiceRecord,
            onStopVoiceRecord = onStopVoiceRecord,
            onSendImage = onSendImage
        )
        return
    }

    var activeTab by remember { mutableStateOf(SocialTab.FRIENDS) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF030E19))
                )
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Social Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬 Social & Friends",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddFriendDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_friend_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Friend", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tab Selector: Friends vs Requests
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D253B), RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeTab = SocialTab.FRIENDS },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == SocialTab.FRIENDS) Color(0xFF00C853) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Friends (${friends.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = { activeTab = SocialTab.REQUESTS },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == SocialTab.REQUESTS) Color(0xFF00C853) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Requests (${friendRequests.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when (activeTab) {
            SocialTab.FRIENDS -> {
                if (friends.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No friends yet. Add one above!", color = Color(0xFF90A4AE))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(friends) { friend ->
                            FriendRowItem(
                                friend = friend,
                                onChatClick = { onSelectFriend(friend) },
                                onRemoveClick = { onRemoveFriend(friend.id) }
                            )
                        }
                    }
                }
            }
            SocialTab.REQUESTS -> {
                if (friendRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending friend requests.", color = Color(0xFF90A4AE))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(friendRequests) { req ->
                            FriendRequestRowItem(
                                request = req,
                                onAccept = { onAcceptRequest(req.id) },
                                onReject = { onRejectRequest(req.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddFriendDialog) {
        var friendNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter player username or user ID:", color = Color(0xFFB0BEC5), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = friendNameInput,
                        onValueChange = { friendNameInput = it },
                        placeholder = { Text("e.g. LeoStar") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF455A64),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendNameInput.isNotBlank()) {
                            onAddFriend(friendNameInput.trim())
                            showAddFriendDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Send Request", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel", color = Color(0xFF90A4AE))
                }
            },
            containerColor = Color(0xFF0D253B)
        )
    }
}

@Composable
private fun FriendRowItem(
    friend: Friend,
    onChatClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1B496E), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar with online dot
                Box(modifier = Modifier.size(44.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFF0277BD))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = friend.avatarInitial,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    // Online indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .background(
                                if (friend.isOnline) Color(0xFF00E676) else Color(0xFF78909C),
                                CircleShape
                            )
                            .border(1.5.dp, Color(0xFF0A2238), CircleShape)
                    )
                }

                Column {
                    Text(
                        text = friend.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Level ${friend.level} • ${friend.statusText}",
                        color = if (friend.isOnline) Color(0xFF00E676) else Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF0091EA), CircleShape)
                        .testTag("chat_friend_${friend.id}")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun FriendRequestRowItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1B496E), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = request.friend.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sent ${request.timeAgo}",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Accept", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Decline", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DirectChatView(
    friend: Friend,
    messages: List<ChatMessage>,
    isRecordingVoice: Boolean,
    recordingDuration: Int,
    onClose: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopVoiceRecord: (String) -> Unit,
    onSendImage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051320))
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A2238))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF0288D1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(friend.avatarInitial, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(friend.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(if (friend.isOnline) "Online" else "Offline", color = if (friend.isOnline) Color(0xFF00E676) else Color.Gray, fontSize = 11.sp)
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Voice Recording Indicator Bar
        if (isRecordingVoice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Mic, contentDescription = "Recording", tint = Color.White)
                    Text("Recording Voice: ${recordingDuration}s", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onStopVoiceRecord(friend.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SEND", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }

        // Message Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF091F33))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Image share button
            IconButton(
                onClick = { onSendImage(friend.id) },
                modifier = Modifier.size(38.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = "Share Image", tint = Color(0xFF00E5FF))
            }

            // Voice Record button
            IconButton(
                onClick = {
                    if (isRecordingVoice) {
                        onStopVoiceRecord(friend.id)
                    } else {
                        onStartVoiceRecord()
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(if (isRecordingVoice) Color.Red else Color(0xFF1E466A), CircleShape)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = Color.White)
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Type message...", color = Color(0xFF78909C), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF2E577C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput.trim())
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF00C853), CircleShape)
                    .testTag("send_chat_message_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val align = if (message.isMe) Alignment.End else Alignment.Start
    val bg = if (message.isMe) Color(0xFF00C853) else Color(0xFF1E3A52)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(bg, RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            Column {
                if (message.isVoice) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Voice", tint = Color.White)
                        Text(
                            text = "Voice message (${message.voiceDurationSeconds ?: 3}s)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (message.imageUrl != null) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color(0xFF081C2C), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🖼️ Shared Image", color = Color.White)
                    }
                } else {
                    Text(
                        text = message.text ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.timestamp,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
