package com.example.recetify.ui.create

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.recetify.ui.create.Ingredient
import com.example.recetify.ui.create.InstructionStep

class CreateRecipeViewModel : ViewModel() {
    val recipeName = mutableStateOf("")
    val recipeDescription = mutableStateOf("")
    val portions = mutableStateOf("2")
    val time = mutableStateOf("2")

    val ingredients = mutableStateListOf<Ingredient>()
    val steps = mutableStateListOf<InstructionStep>()
    val selectedTags = mutableStateListOf<String>()

    fun addIngredient(ingredient: Ingredient) {
        ingredients.add(ingredient)
    }

    fun removeIngredient(index: Int) {
        if (index in ingredients.indices) {
            ingredients.removeAt(index)
        }
    }

    fun updateIngredientUnit(index: Int, unit: String) {
        if (index in ingredients.indices) {
            val current = ingredients[index]
            ingredients[index] = current.copy(unit = unit)
        }
    }

    fun addStep(step: InstructionStep = InstructionStep()) {
        steps.add(step)
    }

    fun updateStep(index: Int, title: String? = null, description: String? = null, imageUri: String? = null) {
        if (index in steps.indices) {
            val current = steps[index]
            steps[index] = current.copy(
                title = title ?: current.title,
                description = description ?: current.description,
                imageUri = imageUri ?: current.imageUri
            )
        }
    }

    fun removeStep(index: Int) {
        if (index in steps.indices) {
            steps.removeAt(index)
        }
    }

    fun toggleTag(tag: String) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
    }

    fun resetAll() {
        recipeName.value = ""
        recipeDescription.value = ""
        portions.value = "2"
        time.value = "2"
        ingredients.clear()
        steps.clear()
        selectedTags.clear()
    }
}
