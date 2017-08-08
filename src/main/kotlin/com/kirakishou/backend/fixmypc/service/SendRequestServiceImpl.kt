package com.kirakishou.backend.fixmypc.service

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.AsyncRestTemplate

@Component
class SendRequestServiceImpl<T> : SendRequestService<T> {

    @Autowired
    lateinit var restTemplate: AsyncRestTemplate

    val publishSubject = PublishSubject.create<T>()

    override fun sendPostRequest(url: T, request: HttpEntity<*>, responseType: Class<T>) {
        publishSubject.onNext(url)
    }

    override fun getObservable(): Observable<T> {
        return publishSubject.subscribeOn(Schedulers.io())
    }
}