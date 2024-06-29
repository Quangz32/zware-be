package com.app.zware.Validation;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.Item;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.HttpEntities.InboundDetailsDTO;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
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

    @Autowired
    ProductRepository productRepository;

    @Autowired
    WarehouseRespository warehouseRespository;

    @Autowired
    WarehouseItemsRepository warehouseItemsRepository;

    public String checkPost(InboundTransactionDTO inboundTransactionDTO) {

        if (inboundTransactionDTO.getDate() == null) {
            return "Transaction date is not valid";
        }
        Integer source = inboundTransactionDTO.getSource();
        String exeternalSource = inboundTransactionDTO.getExternal_source();
        if(source!=null && exeternalSource!=null){
            return "Provide either source or external source,not both";
        }
        if(source==null && exeternalSource==null){
            return "Either source or external source must be provided";
        }
        if(source!=null && !outboundTransactionRepository.existsByIdAndIsDeletedFalse(source)){
            return "Source is not valid";
        }
        if (source != null && source.equals(inboundTransactionDTO.getWarehouse_id())) {
            return "Source and warehouseId cannot be the same";
        }

        if (inboundTransactionDTO.getWarehouse_id()==null||!warehouseRespository.existByIdAndIsDeletedFalse(inboundTransactionDTO.getWarehouse_id())){
            return "WarehouseId is not valid";
        }

        List<InboundDetailsDTO> detailsDTOS = inboundTransactionDTO.getDetails();
        for (InboundDetailsDTO detail : detailsDTOS){
            // Kiem tra id cua zone
            Integer zoneId = detail.getZone_id();
            LocalDate expireDate = detail.getExpire_date();
            LocalDate currentDate = LocalDate.now();
            Integer productId = detail.getProduct_id();
            Integer quantity = detail.getQuantity();

            if(zoneId==null||!zoneRespository.existsByIdAndIsDeletedFalse(zoneId)){
                return  "Zone Id is not valid";
            }
            if(zoneRespository.existByIdAndWarehouseId(zoneId, inboundTransactionDTO.getWarehouse_id())==0){
                return "Zone does not belong to the specified warehouse";
            }
            // kiem tra expireDate
            if (expireDate == null) {
                return "Date not null";
            }
            if (expireDate.isBefore(currentDate)) {
                return "The date was not received in the past";
            }

            // kiem tra productId
            if(productId==null||!productRepository.existsByIdAndIsDeletedFalse(productId)){
                return "Not found Id to add";
            }
            // neu la giao dich noi bo
            if (exeternalSource==null){
            Item item = itemRepository.findByProductIdAndExpiredDate(productId,expireDate);
            if(item==null){
                return "";
            }


                Integer availableQuantity = warehouseItemsRepository.findQuantityByItemIdAndZoneId(item.getId(),zoneId);
                if (availableQuantity == null || availableQuantity < quantity) {
                    return "Insufficient quantity in the source warehouse";
                }

            }

        }

        return "";
    }

}



