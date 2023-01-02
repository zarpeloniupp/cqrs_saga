package com.zarpelon.estore.productserver.core.errohandler;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorMessage {
    private final Date timestamp;
    private final String message;
}
