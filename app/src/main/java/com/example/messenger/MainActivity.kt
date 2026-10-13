package com.example.messenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val text: String,
    val fromMe: Boolean,
    val time: String,
    val status: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                ChatScreen()
            }
        }
    }
}

private fun now(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val messages = remember {
        mutableStateListOf(
            Message("Привет! Это демо-мессенджер.", fromMe = false, time = now())
        )
    }
    var input by remember { mutableStateOf(TextFieldValue()) }
    var typing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun send() {
        val text = input.text.trim()
        if (text.isEmpty()) return

        val outgoing = Message(text, fromMe = true, time = now(), status = "Отправлено")
        messages.add(outgoing)
        input = TextFieldValue()
        scope.launch {
            listState.animateScrollToItem(messages.lastIndex)
            typing = true
            delay(1200)
            typing = false
            messages.add(Message("Ответ на: $text", fromMe = false, time = now()))
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Чат") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(msg)
                }

                if (typing) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Сообщение") },
                    maxLines = 4,
                    singleLine = false
                )
                Button(
                    onClick = { send() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Отпр.")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: Message) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (msg.fromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (msg.fromMe) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(msg.text)

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = msg.time,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (msg.fromMe && msg.status.isNotBlank()) {
                        Text(
                            text = msg.status,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 120.dp)
        ) {
            Text(
                text = "Печатает...",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
