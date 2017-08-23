package com.kirakishou.backend.fixmypc.util

import org.junit.Before
import org.junit.Test

class TextUtilsTest {

    @Before
    fun setUp() {
    }

    @Test
    fun testParseImageNameFun() {
        val testStr = "n0_i0TWkyymLNCGmsgbQGtrpGwSqD98grbleawPPSk3OgEWtex9F6Bf1LSs1KWH4k053"
        val imageInfo = TextUtils.parseImageName(testStr)

        assert(imageInfo.serverId == 0)
        assert(imageInfo.name == "0TWkyymLNCGmsgbQGtrpGwSqD98grbleawPPSk3OgEWtex9F6Bf1LSs1KWH4k053")
    }

    @Test
    fun testParseImageNameFun2() {
        val testStr = "n123_i0TWkyymLNCGmsgbQGtrpGwSqD98grbleawPPSk3OgEWtex9F6Bf1LSs1KWH4k053"
        val imageInfo = TextUtils.parseImageName(testStr)

        assert(imageInfo.serverId == 123)
        assert(imageInfo.name == "0TWkyymLNCGmsgbQGtrpGwSqD98grbleawPPSk3OgEWtex9F6Bf1LSs1KWH4k053")
    }

    @Test
    fun testCreateStatementForList() {
        val statement = TextUtils.createStatementForList(10)

        assert(statement == "?, ?, ?, ?, ?, ?, ?, ?, ?, ?")
    }
}