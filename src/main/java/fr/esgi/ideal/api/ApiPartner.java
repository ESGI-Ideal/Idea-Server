package fr.esgi.ideal.api;

import fr.esgi.ideal.dto.Partner;

public class ApiPartner extends SubApiAdaptor<Partner> {
    public ApiPartner() {
        super(Partner::getId);
    }
}
