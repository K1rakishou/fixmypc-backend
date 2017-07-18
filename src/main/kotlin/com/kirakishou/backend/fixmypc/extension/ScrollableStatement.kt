package com.kirakishou.backend.fixmypc.extension

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

fun Connection.prepareStatementScrollable(statement: String): PreparedStatement {
    return this.prepareStatement(statement, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
}