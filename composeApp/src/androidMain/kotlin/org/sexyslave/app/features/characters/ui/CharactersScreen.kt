package org.sexyslave.app.features.characters.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import org.sexyslave.app.features.characters.mvi.CharactersViewModel

class CharactersScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<CharactersViewModel>()
        val state = viewModel.state

        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text("Error: ${state.error}")
        } else {
            LazyColumn {
                items(state.characters) { character ->
                    Text(character.name)
                }
            }
        }
    }
}