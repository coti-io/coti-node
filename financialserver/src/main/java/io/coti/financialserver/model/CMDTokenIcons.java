package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.http.data.CMDTokenIconData;
import org.springframework.stereotype.Service;

@Service
public class CMDTokenIcons extends Collection<CMDTokenIconData> {
    public void init() {
        super.init();
    }

}
