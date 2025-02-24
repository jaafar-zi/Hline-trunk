package com.example.demo.email;

public enum EmailTemplateName {
    
    ACTIVATE_ACCOUNT("activation_account"),
    FORGOT_PASSCODE("forget_passcode");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }

}
