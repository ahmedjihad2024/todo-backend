package org.personal.todo.account.scheduler

import jakarta.transaction.Transactional
import org.personal.todo.account.repository.PhoneChangeRequestRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class PhoneChangeCleanup(private val repo: PhoneChangeRequestRepository) {
    @Scheduled(fixedRate = 600_000)
    @Transactional
    fun clean() {
        repo.deleteExpired(Instant.now())
    }
}