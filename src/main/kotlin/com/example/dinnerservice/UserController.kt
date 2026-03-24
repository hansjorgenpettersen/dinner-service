package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val currentUserService: CurrentUserService
) {
    private fun ShoppingList.toSummary() = ShoppingListSummaryDto(id, name, owner?.email)

    @GetMapping
    fun profile(): ResponseEntity<UserProfileDto> {
        val user = currentUserService.currentUser()
        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        return ResponseEntity.ok(
            UserProfileDto(
                email = user.email,
                defaultListId = user.defaultListId,
                allLists = (owned + shared).sortedBy { it.name.lowercase() }.map { it.toSummary() },
                sharedLists = shared.map { it.toSummary() }
            )
        )
    }

    @PostMapping("/set-default-list")
    fun setDefaultList(@Valid @RequestBody req: SetDefaultListRequest): ResponseEntity<UserProfileDto> {
        val user = currentUserService.currentUser()
        user.defaultListId = req.listId
        userRepository.save(user)
        return profile()
    }

    @PostMapping("/leave-list/{id}")
    fun leaveList(@PathVariable id: Long): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        list.sharedWith.remove(user)
        if (user.defaultListId == id) {
            user.defaultListId = null
            userRepository.save(user)
        }
        shoppingListRepository.save(list)
        return ResponseEntity.noContent().build()
    }
}
