package sg.gov.tech.molbagencyportalbackend.controller.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.CreateUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.EditUserRequestDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserDTO
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserListRequestParams
import sg.gov.tech.molbagencyportalbackend.dto.internal.user.UserListResponseDTO
import sg.gov.tech.molbagencyportalbackend.service.UserService
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class UserController(private val userService: UserService) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @PreAuthorize("hasAuthority('add_user') or hasAuthority('edit_user') or hasAuthority('delete_user')")
    @GetMapping("/users")
    fun getAgencyUsers(@Valid requestParams: UserListRequestParams): UserListResponseDTO {
        logger.info("Retrieving user list by agency")
        return userService.getAgencyUsers(requestParams)
    }

    @PreAuthorize("hasAuthority('edit_user')")
    @GetMapping("/user/{userId}")
    fun getAgencyUser(@PathVariable userId: Long): UserDTO {
        logger.info("Retrieving user: $userId")
        return userService.getAgencyUser(userId)
    }

    @PreAuthorize("hasAuthority('add_user')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/user")
    fun createAgencyUser(@RequestBody @Valid requestBody: CreateUserRequestDTO): UserDTO {
        logger.info("Adding new agency user: ${requestBody.email}")
        return userService.createAgencyUser(requestBody)
    }

    @PreAuthorize("hasAuthority('edit_user')")
    @PutMapping("/user/{userId}")
    fun updateAgencyUser(
        @PathVariable userId: Long,
        @RequestBody @Valid requestBody: EditUserRequestDTO
    ): UserDTO {
        logger.info("Updating agency user: $userId")
        return userService.updateAgencyUser(userId, requestBody)
    }

    @PreAuthorize("hasAuthority('delete_user')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/user/{userId}")
    fun deleteUser(@PathVariable userId: Long) {
        logger.info("Deleting agency user: $userId")
        userService.removeUser(userId)
    }
}
