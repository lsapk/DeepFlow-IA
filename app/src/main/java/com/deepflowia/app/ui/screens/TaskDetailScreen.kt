package com.deepflowia.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deepflowia.app.models.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(task: Task) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task.title) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = task.description ?: "")
        }
    }
}
