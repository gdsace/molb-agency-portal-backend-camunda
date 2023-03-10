package sg.gov.tech.molbagencyportalbackend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sg.gov.tech.molbagencyportalbackend.model.ActivityType
import sg.gov.tech.molbagencyportalbackend.model.ActivityValue

@Repository
interface ActivityTypeRepository : JpaRepository<ActivityType, Long> {

    fun findByType(type: ActivityValue): ActivityType?
}
