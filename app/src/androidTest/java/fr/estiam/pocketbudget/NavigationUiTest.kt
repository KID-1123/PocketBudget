package fr.estiam.pocketbudget

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.estiam.pocketbudget.ui.SettingsActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationUiTest {
    @Test fun dashboardShowsPrimaryActions() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.addTransactionButton)).check(matches(isDisplayed()))
            onView(withId(R.id.categoriesButton)).check(matches(isDisplayed()))
            onView(withId(R.id.budgetsButton)).check(matches(isDisplayed()))
        }
    }
    @Test fun categoriesButtonOpensManagementScreen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.categoriesButton)).perform(click())
            onView(withText("Categories")).check(matches(isDisplayed()))
        }
    }
    @Test fun settingsScreenShowsPersistentOptions() {
        ActivityScenario.launch(SettingsActivity::class.java).use {
            onView(withId(R.id.currencyGroup)).check(matches(isDisplayed()))
            onView(withId(R.id.showIncomeSwitch)).check(matches(isDisplayed()))
            onView(withId(R.id.sortSpinner)).check(matches(isDisplayed()))
        }
    }
}
