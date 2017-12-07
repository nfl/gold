package com.nfl.dm.shield.dynamic.exception

import spock.lang.Specification

class UnauthorizedExceptionTest extends Specification {

    def 'UnauthorizedException shall preserve its cause'() {
        when:
        def cause = new Exception()
        def e = new UnauthorizedException(cause)

        then:
        e.getCause() == cause
    }

}
