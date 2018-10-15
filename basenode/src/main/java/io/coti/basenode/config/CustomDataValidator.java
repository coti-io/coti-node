package io.coti.basenode.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class CustomDataValidator {

    @InitBinder
    public void activateDirectFieldAccess(WebDataBinder dataBinder) {
        dataBinder.initDirectFieldAccess();
    }

}
