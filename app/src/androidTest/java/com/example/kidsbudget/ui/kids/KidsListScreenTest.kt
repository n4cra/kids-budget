package com.example.kidsbudget.ui.kids

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.kidsbudget.data.Child
import com.example.kidsbudget.data.ChildSummary
import com.example.kidsbudget.theme.KidsBudgetTheme
import org.junit.Rule
import org.junit.Test

class KidsListScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun emptyState_isDisplayed() {
    composeTestRule.setContent {
      KidsBudgetTheme(dynamicColor = false) {
        KidsListScreen(
          state = KidsListUiState(children = emptyList(), isLoading = false),
          selectedChildId = null,
          onChildClick = {},
          onAddChild = {},
        )
      }
    }
    composeTestRule.onNodeWithText("Add your first child").assertIsDisplayed()
  }

  @Test
  fun childName_isDisplayed() {
    composeTestRule.setContent {
      KidsBudgetTheme(dynamicColor = false) {
        KidsListScreen(
          state =
            KidsListUiState(
              children = listOf(ChildSummary(Child(1, "Emma", 0xFF1D9A6C.toInt()), 1250, emptyList())),
              isLoading = false,
            ),
          selectedChildId = null,
          onChildClick = {},
          onAddChild = {},
        )
      }
    }
    composeTestRule.onNodeWithText("Emma").assertIsDisplayed()
  }
}
