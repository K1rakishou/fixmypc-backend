package com.kirakishou.backend.fixmypc.routers

import com.kirakishou.backend.fixmypc.handlers.*
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

class Router(
        private val getClientProfileHandler: GetClientProfileHandler,
        private val updateClientProfileHandler: UpdateClientProfileHandler,
        private val isClientProfileFilledInHandler: IsClientProfileFilledInHandler,
        private val createDamageClaimHandler: CreateDamageClaimHandler,
        private val getDamageClaimsWithinRadiusPagedHandler: GetDamageClaimsWithinRadiusPagedHandler,
        private val hasAlreadyRespondedHandler: HasAlreadyRespondedHandler,
        private val respondToDamageClaimHandler: RespondToDamageClaimHandler,
        private val getClientDamageClaimsPagedHandler: GetClientDamageClaimsPagedHandler,
        private val serveImageHandler: ServeImageHandler,
        private val loginHandler: LoginHandler,
        private val signupHandler: SignupHandler,
        private val getAllRespondedSpecialistsPagedHandler: GetAllRespondedSpecialistsPagedHandler,
        private val markResponseViewedHandler: MarkResponseViewedHandler,
        private val assignSpecialistHandler: AssignSpecialistHandler,
        private val getSpecialistProfileHandler: GetSpecialistProfileHandler,
        private val getSpecialistProfileByIdHandler: GetSpecialistProfileByIdHandler,
        private val updateSpecialistProfileHandler: UpdateSpecialistProfileHandler,
        private val isSpecialistProfileFilledInHandler: IsSpecialistProfileFilledInHandler,
        private val getAssignedSpecialistHandler: GetAssignedSpecialistHandler
) {

    fun setUpRouter() = router {
        "/v1/api/".nest {
            POST("/login", loginHandler::handle)
            POST("/signup", signupHandler::handle)

            "/client".nest {
                "/profile".nest {
                    GET("/get", getClientProfileHandler::handle)
                    GET("/is_filled_in", isClientProfileFilledInHandler::handle)

                    accept(MediaType.APPLICATION_JSON).nest {
                        POST("/update", updateClientProfileHandler::handle)
                    }
                }
            }

            "/specialist".nest {
                GET("/profile/{damage_claim_id}/{skip}/{count}", getAllRespondedSpecialistsPagedHandler::handle)
                PUT("/mark_response", markResponseViewedHandler::handle)
                POST("/assign_specialist", assignSpecialistHandler::handle)
                GET("/is_filled_in", getAssignedSpecialistHandler::handle)

                "/profile".nest {
                    GET("/get", getSpecialistProfileHandler::handle)
                    GET("/get/{specialist_user_id}", getSpecialistProfileByIdHandler::handle)
                    PUT("/update", updateSpecialistProfileHandler::handle)
                    GET("/is_filled_in", isSpecialistProfileFilledInHandler::handle)
                }
            }

            "/damage_claim".nest {
                accept(MediaType.MULTIPART_FORM_DATA).nest {
                    POST("/create", createDamageClaimHandler::handle)
                }

                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/get_within/{lat}/{lon}/{radius}/{skip}/{count}", getDamageClaimsWithinRadiusPagedHandler::handle)
                    GET("/has_responded/{damage_claim_id}", hasAlreadyRespondedHandler::handle)
                    POST("/respond", respondToDamageClaimHandler::handle)
                    GET("/get_client/{is_active}/{skip}/{count}", getClientDamageClaimsPagedHandler::handle)
                }
            }

            "/image".nest {
                GET("/get/{user_id}/{image_type}/{size}/{image_name:.+}", serveImageHandler::handle)
            }
        }
    }
}