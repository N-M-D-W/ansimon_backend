package com.nmdw.ansimon.elderly.domain;

import com.nmdw.ansimon.global.converter.EnumCode;

public enum ConsentStatus implements EnumCode {
    CONSENTED("consented"),
    NOT_CONSENTED("not_consented"),
    WITHDRAWN("withdrawn");

    private final String code;

    ConsentStatus(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
