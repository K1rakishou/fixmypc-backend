package com.kirakishou.backend.fixmypc.service

import io.reactivex.Observable
import org.springframework.http.HttpEntity

interface SendRequestService<T> {
    fun sendPostRequest(url: T, request: HttpEntity<*>, responseType: Class<T>)
    fun getObservable(): Observable<T>
}