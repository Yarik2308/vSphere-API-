package com.vsphere.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Exception class if someone is trying too add something with ID, but it should not have ID.
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "id must be absent")
public class IdentifiedModelException extends Exception{
}
