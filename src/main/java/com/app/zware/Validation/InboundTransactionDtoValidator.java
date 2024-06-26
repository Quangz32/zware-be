package com.app.zware.Validation;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class InboundTransactionDtoValidator {
    @Autowired
    InboundTransactionRepository inboundTransactionRepository;

    @Autowired
    OutboundTransactionRepository outboundTransactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    WarehouseZoneRespository zoneRespository;

    public String checkPost(InboundTransactionDTO inboundTransactionDTO) {

        if (inboundTransactionDTO.getDate() == null) {
            return "Transaction date is not valid";
        }

        Integer source = inboundTransactionDTO.getSource();
        String externalSource = inboundTransactionDTO.getExternal_source();

        if (source != null && externalSource != null) {
            return "Provide either source or external source, not both";
        }

        if (source == null && externalSource == null) {
            return "Either source or external source must be provided";
        }

        if (source != null && !outboundTransactionRepository.existsById(source)) {
            return "Source is not valid";
        }
        if (inboundTransactionDTO.getDetails() != null) {
            for (InboundTransactionDetail detail : inboundTransactionDTO.getDetails()) {
                Integer itemId = detail.getItem_id();
                if (itemId == null || !itemRepository.existsById(itemId)) {
                    return "Item Id is not valid";
                }
                Integer zoneId = detail.getZone_id();
                if (zoneId == null || !zoneRespository.existsById(zoneId)) {
                    return "Zone id is not valid";
                }
            }
        }

        return "";
    }
}
