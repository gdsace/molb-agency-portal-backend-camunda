package sg.gov.tech.molbagencyportalbackend.service

import org.springframework.stereotype.Service
import sg.gov.tech.molbagencyportalbackend.model.ActivityType
import sg.gov.tech.molbagencyportalbackend.model.ActivityValue
import sg.gov.tech.molbagencyportalbackend.repository.ActivityTypeRepository

@Service
class ActivityTypeService(
    private val activityTypeRepository: ActivityTypeRepository
) {

    fun getActivityType(activityValue: ActivityValue): ActivityType? = activityValue.let {
        activityTypeRepository.findByType(it)
    }
}
