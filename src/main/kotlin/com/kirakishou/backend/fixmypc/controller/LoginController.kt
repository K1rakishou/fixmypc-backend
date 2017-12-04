package com.kirakishou.backend.fixmypc.controller

/**
 * Created by kirakishou on 7/9/2017.
 */

/*@Controller
@RequestMapping
class LoginController {

    @Autowired
    lateinit var loginService: LoginService

    @RequestMapping(path = arrayOf(Constant.Paths.LOGIN_CONTROLLER_PATH), method = arrayOf(RequestMethod.POST))
    fun login(@RequestBody request: LoginRequest): Single<ResponseEntity<LoginResponse>> {

        return Single.just(request)
                .map { (login, password) ->
                    val result = loginService.doLogin(login, password)

                    when (result) {
                        is LoginService.Result.Ok -> {
                            return@map ResponseEntity(LoginResponse(result.sessionId, result.accountType.value,
                                    ServerErrorCode.SEC_OK.value), HttpStatus.OK)
                        }

                        is LoginService.Result.WrongLoginOrPassword -> {
                            return@map ResponseEntity(LoginResponse("", AccountType.Guest.value,
                                    ServerErrorCode.SEC_WRONG_LOGIN_OR_PASSWORD.value), HttpStatus.UNPROCESSABLE_ENTITY)
                        }

                        else -> throw IllegalArgumentException("Unknown result")
                    }
                }
                .onErrorReturn {
                    return@onErrorReturn ResponseEntity(LoginResponse("", AccountType.Guest.value,
                            ServerErrorCode.SEC_UNKNOWN_SERVER_ERROR.value),
                            HttpStatus.INTERNAL_SERVER_ERROR)
                }
    }
}*/