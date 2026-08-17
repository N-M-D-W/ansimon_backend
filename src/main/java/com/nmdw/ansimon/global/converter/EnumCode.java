package com.nmdw.ansimon.global.converter;

/**
 * Marks enum values whose stable code is exposed through API contracts or persisted explicitly.
 */
public interface EnumCode {

    String code();
}
