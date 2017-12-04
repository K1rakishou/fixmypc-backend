package com.kirakishou.backend.fixmypc.service.specialist

/*
@Component
class RespondedSpecialistsServiceImpl : RespondedSpecialistsService {

    @Autowired
    private lateinit var respondedSpecialistsStore: RespondedSpecialistsStore

    @Autowired
    private lateinit var damageClaimStore: DamageClaimStore

    @Autowired
    private lateinit var sessionCache: SessionCache

    @Autowired
    private lateinit var specialistProfilesStore: SpecialistProfileStore

    @Autowired
    private lateinit var assignedSpecialistsStore: AssignedSpecialistStore

    @Autowired
    private lateinit var log: FileLog

    override fun getRespondedSpecialistsPaged(sessionId: String, damageClaimId: Long, skip: Long, count: Long):
            Single<RespondedSpecialistsService.Get.Result> {

        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(RespondedSpecialistsService.Get.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(RespondedSpecialistsService.Get.Result.BadAccountType())
        }

        val assignedSpecialistFickle = assignedSpecialistsStore.findOne(damageClaimId)
        if (assignedSpecialistFickle.isPresent()) {
            log.d("DamageClaim already has assigned specialist")
            return Single.just(RespondedSpecialistsService.Get.Result.DamageClaimAlreadyHasAssignedSpecialist())
        }

        val damageClaimFickle = damageClaimStore.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("DamageClaim with id $damageClaimId does not exist")
            return Single.just(RespondedSpecialistsService.Get.Result.DamageClaimDoesNotExist())
        }

        val damageClaim = damageClaimFickle.get()
        if (!damageClaim.isActive) {
            log.d("DamageClaim with id $damageClaimId is not active")
            return Single.just(RespondedSpecialistsService.Get.Result.DamageClaimIsNotActive())
        }

        val respondedSpecialistsList = respondedSpecialistsStore.findManyForDamageClaimPaged(damageClaimId, skip, count)
        val specialistUserIdList = respondedSpecialistsList.map { it.userId }
        val profilesList = specialistProfilesStore.findMany(specialistUserIdList)

        return Single.just(RespondedSpecialistsService.Get.Result.Ok(profilesList))
    }

    override fun markResponseViewed(sessionId: String, damageClaimId: Long, userId: Long): Single<RespondedSpecialistsService.Put.Result> {
        val userFickle = sessionCache.findOne(sessionId)
        if (!userFickle.isPresent()) {
            log.d("SessionId $sessionId was not found in the sessionCache")
            return Single.just(RespondedSpecialistsService.Put.Result.SessionIdExpired())
        }

        val user = userFickle.get()
        if (user.accountType != AccountType.Client) {
            log.d("Bad accountType ${user.accountType}")
            return Single.just(RespondedSpecialistsService.Put.Result.BadAccountType())
        }

        val damageClaimFickle = damageClaimStore.findOne(damageClaimId)
        if (!damageClaimFickle.isPresent()) {
            log.d("Could not find damageClaim with id $damageClaimId")
            return Single.just(RespondedSpecialistsService.Put.Result.CouldNotFindDamageClaim())
        }

        if (user.id != damageClaimFickle.get().userId) {
            log.d("Attempt to modify RespondedSpecialist by user who didn't create it")
            return Single.just(RespondedSpecialistsService.Put.Result.EntityDoesNotBelongToUser())
        }

        val result = respondedSpecialistsStore.updateSetViewed(damageClaimId, userId)
        if (!result) {
            log.d("Could not update respondedSpecialist with damageClaimId $damageClaimId and userId $userId")
            return Single.just(RespondedSpecialistsService.Put.Result.CouldNotUpdateRespondedSpecialist())
        }

        val allEntries = respondedSpecialistsStore.findAll()
        for (entry in allEntries) {
            println(entry)
        }

        return Single.just(RespondedSpecialistsService.Put.Result.Ok())
    }
}




*/







































